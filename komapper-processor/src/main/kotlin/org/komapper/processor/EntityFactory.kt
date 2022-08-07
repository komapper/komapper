package org.komapper.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperVersion
import org.komapper.processor.Symbols.Instant
import org.komapper.processor.Symbols.KotlinInstant
import org.komapper.processor.Symbols.KotlinLocalDateTime
import org.komapper.processor.Symbols.LocalDateTime
import org.komapper.processor.Symbols.OffsetDateTime

internal class EntityFactory(
    @Suppress("unused")
    private val logger: KSPLogger,
    private val config: Config,
    private val entityDef: EntityDef
) {

    fun create(): Entity {
        val allProperties = createAllProperties()
        validateAllProperties(allProperties)
        val topLevelLeafProperties = allProperties.filterIsInstance<LeafProperty>()
        val compositeProperties = allProperties.filterIsInstance<CompositeProperty>()
        val embeddedIdProperty = compositeProperties.firstOrNull { it.kind is PropertyKind.EmbeddedId }
        val idProperties = topLevelLeafProperties.filter { it.kind is PropertyKind.Id }
        val versionProperty: LeafProperty? = topLevelLeafProperties.firstOrNull { it.kind is PropertyKind.Version }
        val createdAtProperty: LeafProperty? = topLevelLeafProperties.firstOrNull { it.kind is PropertyKind.CreatedAt }
        val updatedAtProperty: LeafProperty? = topLevelLeafProperties.firstOrNull { it.kind is PropertyKind.UpdatedAt }
        val ignoredProperties = topLevelLeafProperties.filter { it.kind is PropertyKind.Ignore }
        val properties = allProperties - ignoredProperties.toSet()
        if (properties.none()) {
            report("Any persistent properties are not found.", entityDef.definitionSource.entityDeclaration)
        }
        return Entity(
            entityDef.definitionSource.entityDeclaration,
            entityDef.table,
            properties,
            embeddedIdProperty,
            idProperties,
            versionProperty,
            createdAtProperty,
            updatedAtProperty,
        ).also {
            validateEntity(it)
        }
    }

    private fun createAllProperties(): List<Property> {
        val propertyDefMap = entityDef.properties.associateBy { it.declaration.simpleName }
        val (_, entityDeclaration) = entityDef.definitionSource
        val propertyDeclarationMap = entityDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        val parameters = entityDeclaration.primaryConstructor?.parameters ?: return emptyList()
        return parameters.asSequence()
            .map { propertyDefMap[it.name!!] to it }
            .map { (propertyDef, parameter) ->
                val declaration = propertyDeclarationMap[parameter.name]
                    ?: report("The corresponding property declaration is not found.", parameter)
                when (propertyDef) {
                    is CompositePropertyDef -> createCompositeProperty(propertyDef, parameter)
                    is LeafPropertyDef -> createLeafProperty(propertyDef, parameter, declaration)
                    else -> createLeafProperty(null, parameter, declaration)
                }
            }
            .toList()
    }

    private fun createCompositeProperty(
        propertyDef: CompositePropertyDef,
        parameter: KSValueParameter
    ): CompositeProperty {
        val type = parameter.type.resolve().normalize()
        val embeddableDef = propertyDef.embeddableDef
        val properties = embeddableDef.properties.map {
            createLeafProperty(it, it.parameter, it.declaration)
        }
        val embeddable = Embeddable(embeddableDef.declaration, properties)
        return CompositeProperty(
            propertyDef.parameter,
            propertyDef.declaration,
            type.nullability,
            propertyDef.kind,
            embeddable
        )
    }

    private fun createLeafProperty(
        propertyDef: LeafPropertyDef?,
        parameter: KSValueParameter,
        declaration: KSPropertyDeclaration
    ): LeafProperty {
        val column = getColumn(propertyDef, parameter)
        val type = parameter.type.resolve().normalize()
        val kotlinClass = createEnumClass(propertyDef, type) ?: createValueClass(type) ?: PlainClass(type)
        val literalTag = resolveLiteralTag(kotlinClass.exteriorTypeName)
        val nullability = type.nullability
        val kind = propertyDef?.kind
        return LeafProperty(
            parameter = parameter,
            declaration = declaration,
            nullability = nullability,
            column = column,
            kotlinClass = kotlinClass,
            literalTag = literalTag,
            kind = kind
        ).also { validateLeafProperty(it) }
    }

    private fun createEnumClass(propertyDef: LeafPropertyDef?, type: KSType): EnumClass? {
        val classDeclaration = type.declaration.accept(ClassDeclarationVisitor(), Unit)
        return if (classDeclaration != null && classDeclaration.classKind == ClassKind.ENUM_CLASS) {
            val enumStrategy = propertyDef?.enumStrategy ?: config.enumStrategy
            EnumClass(type, enumStrategy)
        } else null
    }

    private fun createValueClass(type: KSType): ValueClass? {
        val classDeclaration = type.declaration.accept(ClassDeclarationVisitor(), Unit)
        return if (classDeclaration != null) {
            val constructor = classDeclaration.primaryConstructor
            val isPublic = constructor?.isPublic() ?: false
            val parameter = constructor?.parameters?.firstOrNull()
            val declaration = classDeclaration.getDeclaredProperties().firstOrNull()
            if (classDeclaration.isValueClass() && isPublic && parameter != null && declaration != null) {
                val interiorType = parameter.type.resolve()
                val nonNullableInteriorType =
                    if (interiorType.isMarkedNullable) {
                        interiorType.makeNotNullable()
                    } else interiorType
                val typeName = nonNullableInteriorType.buildQualifiedName()
                val literalTag = resolveLiteralTag(typeName)
                val nullability = interiorType.nullability
                val property = ValueClassProperty(parameter, declaration, typeName, literalTag, nullability)
                ValueClass(type, property)
            } else null
        } else null
    }

    private fun resolveLiteralTag(typeName: String): String {
        return when (typeName) {
            "kotlin.Long" -> "L"
            "kotlin.UInt" -> "u"
            else -> ""
        }
    }

    private fun getColumn(propertyDef: LeafPropertyDef?, parameter: KSValueParameter): Column {
        return if (propertyDef == null) {
            val name = parameter.name?.asString() ?: report("The name is not found.", parameter)
            Column(config.namingStrategy.apply(name), alwaysQuote = false, masking = false)
        } else {
            propertyDef.column
        }
    }

    private fun validateLeafProperty(property: LeafProperty) {
        if (property.kind !is PropertyKind.Ignore) {
            if (property.isPrivate()) {
                report("The property must not be private.", property.parameter)
            }
            when (val kotlinClass = property.kotlinClass) {
                is EnumClass -> Unit
                is ValueClass -> validateValueClassProperty(property, kotlinClass)
                is PlainClass -> validatePlainClassProperty(property, kotlinClass)
            }
        }
        when (val kind = property.kind) {
            is PropertyKind.Id -> validateIdProperty(property, kind.idKind)
            is PropertyKind.Version -> validateVersionProperty(property)
            is PropertyKind.CreatedAt -> validateTimestampProperty(property, "@KomapperCreatedAt")
            is PropertyKind.UpdatedAt -> validateTimestampProperty(property, "@KomapperUpdatedAt")
            is PropertyKind.Ignore -> validateIgnoreProperty(property)
            else -> Unit
        }
    }

    private fun validateValueClassProperty(property: LeafProperty, valueClass: ValueClass) {
        val valueClassProperty = valueClass.property
        if (valueClassProperty.isPrivate()) {
            report(
                "The value class's own property '$valueClassProperty' must not be private.",
                property.parameter
            )
        }
        if (valueClassProperty.nullability == Nullability.NULLABLE) {
            report(
                "The value class's own property '$valueClassProperty' must not be nullable.",
                property.parameter
            )
        }
        checkEnumAnnotation(property)
    }

    private fun validatePlainClassProperty(property: LeafProperty, plainClass: PlainClass) {
        if (!plainClass.isArray && plainClass.declaration.typeParameters.isNotEmpty()) {
            report("The non-array property type must not have any type parameters.", property.parameter)
        }
        checkEnumAnnotation(property)
    }

    private fun checkEnumAnnotation(property: LeafProperty) {
        if (property.declaration.hasAnnotation(KomapperEnum::class)) {
            report("@KomapperEnum is valid only for enum property types.", property.parameter)
        }
    }

    private fun validateIdProperty(property: LeafProperty, idKind: IdKind?) {
        if (idKind == null) return
        fun validate(annotationName: String) {
            when (property.typeName) {
                "kotlin.Int", "kotlin.Long", "kotlin.UInt" -> Unit
                else -> {
                    when (val kotlinClass = property.kotlinClass) {
                        is ValueClass -> {
                            when (kotlinClass.property.typeName) {
                                "kotlin.Int", "kotlin.Long", "kotlin.UInt" -> Unit
                                else -> report(
                                    "When the type of $annotationName annotated property is value class, the type of the value class's own property must be either Int, Long or UInt.",
                                    property.parameter
                                )
                            }
                        }
                        else -> report(
                            "The type of $annotationName annotated property must be either Int, Long, UInt or value class.",
                            property.parameter
                        )
                    }
                }
            }
        }
        when (idKind) {
            is IdKind.AutoIncrement -> validate("@KomapperAutoIncrement")
            is IdKind.Sequence -> validate("@KomapperSequence")
        }
    }

    private fun validateVersionProperty(property: LeafProperty) {
        when (property.typeName) {
            "kotlin.Int", "kotlin.Long", "kotlin.UInt" -> Unit
            else -> {
                when (val kotlinClass = property.kotlinClass) {
                    is ValueClass -> {
                        when (kotlinClass.property.typeName) {
                            "kotlin.Int", "kotlin.Long", "kotlin.UInt" -> Unit
                            else -> report(
                                "When the type of @${KomapperVersion::class.simpleName} annotated property is value class, the type of the value class's own property must be either Int, Long or UInt.",
                                property.parameter
                            )
                        }
                    }
                    else -> report(
                        "The type of @${KomapperVersion::class.simpleName} annotated property must be either Int, Long, UInt or value class.",
                        property.parameter
                    )
                }
            }
        }
    }

    private fun validateTimestampProperty(property: LeafProperty, annotationName: String) {
        when (property.typeName) {
            Instant, LocalDateTime, OffsetDateTime, KotlinInstant, KotlinLocalDateTime -> Unit
            else -> {
                when (val kotlinClass = property.kotlinClass) {
                    is ValueClass -> {
                        when (kotlinClass.property.typeName) {
                            Instant, LocalDateTime, OffsetDateTime, KotlinInstant, KotlinLocalDateTime -> Unit
                            else -> report(
                                "When the type of $annotationName annotated property is value class, the type of the value class's own property must be either Instant, LocalDateTime or OffsetDateTime.",
                                property.parameter
                            )
                        }
                    }
                    else -> report(
                        "The type of $annotationName annotated property must be either Instant, LocalDateTime or OffsetDateTime.",
                        property.parameter
                    )
                }
            }
        }
    }

    private fun validateIgnoreProperty(property: LeafProperty) {
        if (!property.parameter.hasDefault) {
            report("The ignored property must have a default value.", property.parameter)
        }
    }

    private fun validateAllProperties(properties: List<Property>) {
        val propertyDefMap = entityDef.properties.associateBy { it.declaration.simpleName }
        val propertyMap = properties.associateBy { it.declaration.simpleName }
        for ((key, value) in propertyDefMap) {
            propertyMap[key]
                ?: report("The same name property is not found in the entity.", value.parameter)
        }
    }

    private fun validateEntity(entity: Entity) {
        if (entity.declaration.simpleName.asString().startsWith("__")) {
            report("The class name cannot start with '__'.", entity.declaration)
        }
        for (p in entity.properties) {
            val name = (p.declaration.simpleName).asString()
            if (name.startsWith("__")) {
                report("The property name cannot start with '__'.", p.parameter)
            }
        }
        if (entity.embeddedIdProperty == null && entity.idProperties.isEmpty()) {
            report("The entity class must have at least one id property.", entity.declaration)
        }
        if (entity.embeddedIdProperty != null && entity.idProperties.isNotEmpty()) {
            report(
                "The entity class can have either @${KomapperEmbeddedId::class.simpleName} or @${KomapperId::class.simpleName}.",
                entity.declaration
            )
        }
    }
}
