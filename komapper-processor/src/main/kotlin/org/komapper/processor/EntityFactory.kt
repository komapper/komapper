package org.komapper.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperEnumOverride
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

    private val annotationSupport = AnnotationSupport(config)

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
                    is CompositePropertyDef -> createCompositeProperty(
                        propertyDef = propertyDef,
                        parameter = parameter,
                        declaration = declaration
                    )
                    is LeafPropertyDef -> createLeafProperty(
                        parameter = parameter,
                        declaration = declaration,
                        kind = propertyDef.kind,
                        typeReference = propertyDef.type,
                        column = propertyDef.column,
                        enumStrategy = propertyDef.enumStrategy,
                        parent = null,
                    )
                    else -> createLeafProperty(
                        parameter = parameter,
                        declaration = declaration,
                        kind = null,
                        typeReference = null,
                        column = null,
                        enumStrategy = null,
                        parent = null,
                    )
                }
            }
            .toList()
    }

    private fun createCompositeProperty(
        propertyDef: CompositePropertyDef,
        parameter: KSValueParameter,
        declaration: KSPropertyDeclaration,
    ): CompositeProperty {
        val type = parameter.type.resolve()
        val (normalizedType, typeArgumentResolver) = type.normalize()
        val embeddableDeclaration = normalizedType.declaration.accept(ClassDeclarationVisitor(), Unit) ?: report(
            "@${propertyDef.kind.annotation.shortName} cannot be applied to this element. " +
                "${normalizedType.name} must be a data class.",
            declaration
        )
        val embeddable = createEmbeddable(
            parent = parameter,
            embeddableDeclaration = embeddableDeclaration,
            typeArgumentResolver = typeArgumentResolver,
            columns = annotationSupport.getColumns(propertyDef.parameter),
            enumStrategies = annotationSupport.getEnumStrategies(propertyDef.parameter),
        )
        val nullability =
            if (type.nullability == Nullability.NULLABLE || normalizedType.nullability == Nullability.NULLABLE) {
                Nullability.NULLABLE
            } else {
                normalizedType.nullability
            }
        return CompositeProperty(
            propertyDef.parameter,
            propertyDef.declaration,
            propertyDef.kind,
            nullability,
            embeddable
        ).also {
            validateContainerClass(embeddableDeclaration, propertyDef.kind.annotation, allowTypeParameters = true)
        }
    }

    private fun createEmbeddable(
        parent: KSValueParameter,
        embeddableDeclaration: KSClassDeclaration,
        typeArgumentResolver: TypeArgumentResolver,
        columns: List<Triple<String, Column, KSAnnotation>>,
        enumStrategies: List<Triple<String, EnumStrategy, KSAnnotation>>,
    ): Embeddable {
        val columnMap = columns.associate { it.first to it.second }
        val enumStrategyMap = enumStrategies.associate { it.first to it.second }
        val propertyDeclarationMap = embeddableDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        val parameters = embeddableDeclaration.primaryConstructor?.parameters ?: emptyList()
        val (properties, typeArguments) = parameters.map { parameter ->
            val declaration = propertyDeclarationMap[parameter.name]
                ?: report("The corresponding property declaration is not found.", parameter)
            val typeArgument = typeArgumentResolver.resolve(parameter.type.resolve().declaration)
            val name = parameter.toString()
            createLeafProperty(
                parameter = parameter,
                declaration = declaration,
                kind = null,
                typeReference = typeArgument?.type,
                column = columnMap[name] ?: annotationSupport.getColumn(null, name),
                enumStrategy = enumStrategyMap[name],
                parent = parent,
            ) to typeArgument
        }.unzip()
        val type = embeddableDeclaration.asType(typeArguments.filterNotNull())
        return Embeddable(type, properties).also {
            validateEmbeddable(it, columns, enumStrategies)
        }
    }

    private fun validateEmbeddable(
        embeddable: Embeddable,
        columns: List<Triple<String, Column, KSAnnotation>>,
        enumStrategies: List<Triple<String, EnumStrategy, KSAnnotation>>,
    ) {
        val propertyNames = embeddable.properties.map { it.parameter.toString() }.toSet()
        fun checkPropertyName(name: String, annotation: KSAnnotation) {
            if (name !in propertyNames) {
                report(
                    "The property \"$name\" is not found in the class \"${embeddable.type.declaration.simpleName.asString()}\".",
                    annotation
                )
            }
        }
        columns.forEach { checkPropertyName(it.first, it.third) }
        enumStrategies.forEach { checkPropertyName(it.first, it.third) }
    }

    private fun createLeafProperty(
        parent: KSValueParameter?,
        parameter: KSValueParameter,
        declaration: KSPropertyDeclaration,
        kind: PropertyKind?,
        typeReference: KSTypeReference?,
        column: Column?,
        enumStrategy: EnumStrategy?
    ): LeafProperty {
        val (type) = (typeReference ?: parameter.type).resolve().normalize()
        val kotlinClass = createEnumClass(enumStrategy, type) ?: createValueClass(type) ?: PlainClass(type)
        return LeafProperty(
            parameter = parameter,
            declaration = declaration,
            nullability = type.nullability,
            column = getColumn(column, parameter),
            kotlinClass = kotlinClass,
            literalTag = resolveLiteralTag(kotlinClass.exteriorTypeName),
            kind = kind,
            parent = parent
        ).also { validateLeafProperty(it) }
    }

    private fun createEnumClass(enumStrategy: EnumStrategy?, type: KSType): EnumClass? {
        val classDeclaration = type.declaration.accept(ClassDeclarationVisitor(), Unit)
        return if (classDeclaration != null && classDeclaration.classKind == ClassKind.ENUM_CLASS) {
            EnumClass(type, enumStrategy ?: config.enumStrategy)
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
                val typeName = nonNullableInteriorType.name
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

    private fun getColumn(column: Column?, parameter: KSValueParameter): Column {
        return if (column == null) {
            val name = parameter.name?.asString() ?: report("The name is not found.", parameter)
            Column(config.namingStrategy.apply(name), alwaysQuote = false, masking = false)
        } else {
            column
        }
    }

    private fun validateLeafProperty(property: LeafProperty) {
        if (property.kind !is PropertyKind.Ignore) {
            if (property.isPrivate()) {
                report("The property must not be private.", property.node)
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
                "The property \"${property.path}\" is invalid. The value class's own property '$valueClassProperty' must not be private.",
                property.node
            )
        }
        if (valueClassProperty.nullability == Nullability.NULLABLE) {
            report(
                "The property \"${property.path}\" is invalid. The value class's own property '$valueClassProperty' must not be nullable.",
                property.node
            )
        }
        checkEnumAnnotation(property)
    }

    private fun validatePlainClassProperty(property: LeafProperty, plainClass: PlainClass) {
        if (!plainClass.isArray && plainClass.declaration.typeParameters.isNotEmpty()) {
            report(
                "The property \"${property.path}\" must not have any type parameters.",
                property.node
            )
        }
        checkEnumAnnotation(property)
    }

    private fun checkEnumAnnotation(property: LeafProperty) {
        if (property.declaration.hasAnnotation(KomapperEnum::class)) {
            report("@KomapperEnum is valid only for enum property types.", property.node)
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
                                    property.node
                                )
                            }
                        }
                        else -> report(
                            "The type of $annotationName annotated property must be either Int, Long, UInt or value class.",
                            property.node
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
                                property.node
                            )
                        }
                    }
                    else -> report(
                        "The type of @${KomapperVersion::class.simpleName} annotated property must be either Int, Long, UInt or value class.",
                        property.node
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
                                property.node
                            )
                        }
                    }
                    else -> report(
                        "The type of $annotationName annotated property must be either Instant, LocalDateTime or OffsetDateTime.",
                        property.node
                    )
                }
            }
        }
    }

    private fun validateIgnoreProperty(property: LeafProperty) {
        if (!property.parameter.hasDefault) {
            report("The ignored property must have a default value.", property.node)
        }
    }

    private fun validateAllProperties(properties: List<Property>) {
        val propertyDefMap = entityDef.properties.associateBy { it.declaration.simpleName }
        val propertyMap = properties.associateBy { it.declaration.simpleName }
        for ((key, value) in propertyDefMap) {
            propertyMap[key]
                ?: report("The same name property is not found in the entity.", value.parameter)
        }

        for (annotation in listOf(KomapperColumnOverride::class, KomapperEnumOverride::class)) {
            for (p in properties.filterIsInstance<LeafProperty>()) {
                if (p.parameter.hasAnnotation(annotation)) {
                    report(
                        "@${annotation.simpleName} must be used with either @${KomapperEmbedded::class.simpleName} or @${KomapperEmbeddedId::class.simpleName}.",
                        p.parameter
                    )
                }
            }
        }
    }

    private fun validateEntity(entity: Entity) {
        if (entity.declaration.simpleName.asString().startsWith("__")) {
            report("The class name cannot start with '__'.", entity.declaration)
        }
        for (p in entity.properties) {
            val name = (p.declaration.simpleName).asString()
            if (name.startsWith("__")) {
                report("The property name cannot start with '__'.", p.node)
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
