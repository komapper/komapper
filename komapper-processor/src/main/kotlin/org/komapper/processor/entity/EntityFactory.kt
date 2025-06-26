package org.komapper.processor.entity

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperEmbedded
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperEnumOverride
import org.komapper.annotation.KomapperVersion
import org.komapper.processor.Context
import org.komapper.processor.EnumStrategy
import org.komapper.processor.Symbols.Instant
import org.komapper.processor.Symbols.KotlinInstant
import org.komapper.processor.Symbols.KotlinLocalDateTime
import org.komapper.processor.Symbols.KotlinTimeInstant
import org.komapper.processor.Symbols.LocalDateTime
import org.komapper.processor.Symbols.OffsetDateTime
import org.komapper.processor.TypeArgumentResolver
import org.komapper.processor.backquotedName
import org.komapper.processor.hasAnnotation
import org.komapper.processor.isValueClass
import org.komapper.processor.name
import org.komapper.processor.normalize
import org.komapper.processor.report
import org.komapper.processor.resolveLiteralTag
import org.komapper.processor.validateContainerClass

internal class EntityFactory(
    private val context: Context,
    private val entityDef: EntityDef,
) {
    private val annotationSupport = AnnotationSupport(context)

    fun create(): Entity {
        val allProperties = createAllProperties()
        validateAllProperties(allProperties)
        val topLevelLeafProperties = allProperties.filterIsInstance<LeafProperty>()
        val compositeProperties = allProperties.filterIsInstance<CompositeProperty>()
        val embeddedIdProperty = compositeProperties.firstOrNull { it.kind is PropertyKind.EmbeddedId }
        val virtualEmbeddedIdProperty =
            embeddedIdProperty?.let { if (it.kind is PropertyKind.EmbeddedId && it.kind.virtual) it else null }
        val idProperties = topLevelLeafProperties.filter { it.kind is PropertyKind.Id }
        val virtualIdProperties = idProperties.filter { it.kind is PropertyKind.Id && it.kind.virtual }
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
            entityDef.projection,
            entityDef.aggregateRoot,
            entityDef.associations,
            properties,
            embeddedIdProperty,
            virtualEmbeddedIdProperty,
            idProperties,
            virtualIdProperties,
            versionProperty,
            createdAtProperty,
            updatedAtProperty,
        )
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
                        declaration = declaration,
                    )
                    is LeafPropertyDef -> createLeafProperty(
                        parameter = parameter,
                        declaration = declaration,
                        kind = propertyDef.kind,
                        typeArgument = null,
                        column = propertyDef.column,
                        enumStrategy = propertyDef.enumStrategy,
                        parent = null,
                    )
                    else -> createLeafProperty(
                        parameter = parameter,
                        declaration = declaration,
                        kind = null,
                        typeArgument = null,
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
        @Suppress("UNUSED_PARAMETER") declaration: KSPropertyDeclaration,
    ): CompositeProperty {
        val parameterType = parameter.type.resolve()
        val (type, typeArgumentResolver) = parameterType.normalize()
        val embeddableDeclaration = type.declaration as? KSClassDeclaration
            ?: report("${type.name} must be a data class.", parameter)
        val embeddable = createEmbeddable(
            parent = parameter,
            embeddableDeclaration = embeddableDeclaration,
            typeArgumentResolver = typeArgumentResolver,
            columns = annotationSupport.getColumns(propertyDef.parameter),
            enumStrategies = annotationSupport.getEnumStrategies(propertyDef.parameter),
        )
        val nullability =
            if (parameterType.nullability == Nullability.NULLABLE || type.nullability == Nullability.NULLABLE) {
                Nullability.NULLABLE
            } else {
                type.nullability
            }
        return CompositeProperty(
            propertyDef.parameter,
            propertyDef.declaration,
            propertyDef.kind,
            nullability,
            embeddable,
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
                typeArgument = typeArgument,
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
                    annotation,
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
        typeArgument: KSTypeArgument?,
        column: Column?,
        enumStrategy: EnumStrategy?,
    ): LeafProperty {
        val (type) = (typeArgument?.type ?: parameter.type).resolve().normalize()
        val alternateType = column?.alternateType
        val kotlinClass =
            createEnumClass(enumStrategy, type) ?: createValueClass(type, alternateType) ?: PlainClass(type, alternateType)
        return LeafProperty(
            parameter = parameter,
            declaration = declaration,
            nullability = type.nullability,
            typeArgument = typeArgument,
            column = getColumn(column, parameter),
            kotlinClass = kotlinClass,
            literalTag = resolveLiteralTag(kotlinClass.typeName),
            kind = kind,
            parent = parent,
        ).also { validateLeafProperty(it) }
    }

    private fun createEnumClass(enumStrategy: EnumStrategy?, type: KSType): EnumClass? {
        val classDeclaration = type.declaration as? KSClassDeclaration
        return if (classDeclaration != null && classDeclaration.classKind == ClassKind.ENUM_CLASS) {
            when (val strategy = enumStrategy ?: context.config.enumStrategy) {
                EnumStrategy.Name -> EnumClass(type, EnumStrategy.Name.typeName, strategy)
                EnumStrategy.Type -> EnumClass(type, type.backquotedName, strategy)
                EnumStrategy.Ordinal -> EnumClass(type, EnumStrategy.Ordinal.typeName, strategy)
                is EnumStrategy.Property -> {
                    val propertyName = strategy.propertyName
                    val propertyType = classDeclaration.getDeclaredProperties()
                        .filter { it.simpleName.asString() == propertyName }
                        .map { it.type.resolve() }
                        .firstOrNull()
                        ?: report(
                            "The property \"$propertyName\" is not found in the ${classDeclaration.qualifiedName?.asString()}. " +
                                "KomapperEnum's hint property is incorrect.",
                            strategy.annotation,
                        )
                    EnumClass(type, propertyType.backquotedName, strategy)
                }
            }
        } else {
            null
        }
    }

    private fun createValueClass(type: KSType, alternateType: ValueClass?): ValueClass? {
        val classDeclaration = type.declaration as? KSClassDeclaration
        return if (classDeclaration != null) {
            val constructor = classDeclaration.primaryConstructor
            val isPublic = constructor?.isPublic() ?: false
            val parameter = constructor?.parameters?.firstOrNull()
            val declaration = classDeclaration.getDeclaredProperties().firstOrNull()
            if (classDeclaration.isValueClass() && isPublic && parameter != null && declaration != null) {
                val propertyType = declaration.type.resolve()
                val nonNullableInteriorType =
                    if (propertyType.isMarkedNullable) {
                        propertyType.makeNotNullable()
                    } else {
                        propertyType
                    }
                val typeName = nonNullableInteriorType.name
                val backquotedTypeName = nonNullableInteriorType.backquotedName
                val literalTag = resolveLiteralTag(typeName)
                val nullability = propertyType.nullability
                val property =
                    ValueClassProperty(propertyType, parameter, declaration, typeName, backquotedTypeName, literalTag, nullability)
                ValueClass(type, property, alternateType)
            } else {
                null
            }
        } else {
            null
        }
    }

    private fun getColumn(column: Column?, parameter: KSValueParameter): Column {
        return if (column == null) {
            val name = parameter.toString()
            Column(
                context.config.namingStrategy.apply(name),
                alwaysQuote = false,
                masking = false,
                updatable = true,
                insertable = true,
                alternateType = null,
            )
        } else {
            column
        }
    }

    private fun validateLeafProperty(property: LeafProperty) {
        if (property.typeArgument?.variance == Variance.STAR) {
            report("The property \"${property.path}\" must not be a star-projected type.", property.node)
        }
        if (property.kind !is PropertyKind.Ignore) {
            if (property.isPrivate()) {
                report("The property must not be private.", property.node)
            }
            when (val kotlinClass = property.kotlinClass) {
                is EnumClass -> validateEnumClassProperty(property, kotlinClass)
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

    private fun validateEnumClassProperty(
        property: LeafProperty,
        @Suppress("UNUSED_PARAMETER") enumClass: EnumClass,
    ) {
        if (property.column.alternateType != null) {
            report("@KomapperColumn.alternateType is invalid for enum property types.", property.node)
        }
    }

    private fun validateValueClassProperty(property: LeafProperty, valueClass: ValueClass) {
        val valueClassProperty = valueClass.property
        if (valueClassProperty.isPrivate()) {
            report(
                "The property \"${property.path}\" is invalid. The value class's own property '$valueClassProperty' must not be private.",
                property.node,
            )
        }
        if (valueClassProperty.nullability == Nullability.NULLABLE) {
            report(
                "The property \"${property.path}\" is invalid. The value class's own property '$valueClassProperty' must not be nullable.",
                property.node,
            )
        }
        if (valueClass.alternateType != null) {
            if (valueClassProperty.type.declaration != valueClass.alternateType.property.type.declaration) {
                report(
                    "The property \"${property.path}\" is invalid. The parameter property type does not match between \"${valueClass.type.name}\" and \"${valueClass.alternateType.type.name}\".",
                    property.node,
                )
            }
        }
        checkEnumAnnotation(property)
    }

    private fun validatePlainClassProperty(property: LeafProperty, plainClass: PlainClass) {
        if (plainClass.alternateType != null) {
            if (plainClass.declaration != plainClass.alternateType.property.type.declaration) {
                report(
                    "The property \"${property.path}\" is invalid. The property type does not match the parameter property type in \"${plainClass.alternateType.type.name}\".",
                    property.node,
                )
            }
        }
        checkEnumAnnotation(property)
    }

    private fun checkEnumAnnotation(property: LeafProperty) {
        if (property.parameter.hasAnnotation(KomapperEnum::class)) {
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
                                    property.node,
                                )
                            }
                        }
                        else -> report(
                            "The type of $annotationName annotated property must be either Int, Long, UInt or value class.",
                            property.node,
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
                                property.node,
                            )
                        }
                    }
                    else -> report(
                        "The type of @${KomapperVersion::class.simpleName} annotated property must be either Int, Long, UInt or value class.",
                        property.node,
                    )
                }
            }
        }
    }

    private fun validateTimestampProperty(property: LeafProperty, annotationName: String) {
        when (property.typeName) {
            Instant, LocalDateTime, OffsetDateTime, KotlinInstant, KotlinLocalDateTime, KotlinTimeInstant -> Unit
            else -> {
                when (val kotlinClass = property.kotlinClass) {
                    is ValueClass -> {
                        when (kotlinClass.property.typeName) {
                            Instant, LocalDateTime, OffsetDateTime, KotlinInstant, KotlinLocalDateTime, KotlinTimeInstant -> Unit
                            else -> report(
                                "When the type of $annotationName annotated property is value class, the type of the value class's own property must be either Instant, LocalDateTime or OffsetDateTime.",
                                property.node,
                            )
                        }
                    }
                    else -> report(
                        "The type of $annotationName annotated property must be either Instant, LocalDateTime or OffsetDateTime.",
                        property.node,
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
                        p.parameter,
                    )
                }
            }
        }
    }
}
