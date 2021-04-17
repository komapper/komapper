package org.komapper.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSValueParameter

internal class EntityFactory(config: Config, private val entityDef: EntityDef) {

    private val namingStrategy: NamingStrategy = config.namingStrategy

    fun create(): Entity {
        val allProperties = createAllProperties()
        val idProperties = allProperties.filter { it.kind is PropertyKind.Id }
        val versionProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.Version }
        val createdAtProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.CreatedAt }
        val updatedAtProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.UpdatedAt }
        val ignoredProperties = allProperties.filter { it.kind is PropertyKind.Ignore }
        val properties = allProperties - ignoredProperties
        if (properties.none()) {
            report("Any persistent properties are not found.", entityDef.definitionSource.entityDeclaration)
        }
        return Entity(
            entityDef.definitionSource.entityDeclaration,
            entityDef.table,
            properties.toList(),
            idProperties.toList(),
            versionProperty,
            createdAtProperty,
            updatedAtProperty
        ).also {
            validateEntity(it)
        }
    }

    private fun createAllProperties(): Sequence<Property> {
        val propertyDefMap = entityDef.properties.associateBy { it.declaration.simpleName }
        val (_, entityDeclaration) = entityDef.definitionSource
        val propertyDeclarationMap = entityDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        return entityDeclaration.primaryConstructor?.parameters
            ?.asSequence()
            ?.map { propertyDefMap[it.name!!] to it }
            ?.map { (propertyDef, parameter) ->
                val declaration = propertyDeclarationMap[parameter.name]
                    ?: report("The corresponding property declaration is not found.", parameter)
                val column = getColumn(propertyDef, parameter)
                val type = parameter.type.resolve()
                val typeName = (type.declaration.qualifiedName ?: type.declaration.simpleName).asString()
                val nullability = type.nullability
                val kind = propertyDef?.kind
                val generatorKind = propertyDef?.idGeneratorKind
                Property(parameter, declaration, column, typeName, nullability, kind, generatorKind).also {
                    validateProperty(it)
                }
            }?.also {
                validateAllProperties(it)
            } ?: emptySequence()
    }

    private fun getColumn(propertyDef: PropertyDef?, parameter: KSValueParameter): Column {
        return if (propertyDef == null) {
            val name = parameter.name?.asString() ?: report("The name is not found.", parameter)
            Column(namingStrategy.apply(name), false)
        } else {
            propertyDef.column
        }
    }

    private fun validateProperty(property: Property) {
        if (property.isPrivate()) {
            report("The property must not be private.", property.parameter)
        }
        validatePropertyKind(property)
        validateIdGeneratorKind(property)
    }

    private fun validatePropertyKind(property: Property) {
        val parameter = property.parameter
        when (property.kind) {
            is PropertyKind.Version -> {
                when (property.typeName) {
                    "kotlin.Int" -> Unit
                    "kotlin.Long" -> Unit
                    else -> report(
                        "The version property must be either Int or Long type.",
                        parameter
                    )
                }
            }
            is PropertyKind.CreatedAt -> {
                when (property.typeName) {
                    "java.time.LocalDateTime" -> Unit
                    "java.time.OffsetDateTime" -> Unit
                    else -> report(
                        "The createdAt property must be either LocalDateTime or OffsetDateTime type.",
                        parameter
                    )
                }
            }
            is PropertyKind.UpdatedAt -> {
                when (property.typeName) {
                    "java.time.LocalDateTime" -> Unit
                    "java.time.OffsetDateTime" -> Unit
                    else -> report(
                        "The updatedAt property must be either LocalDateTime or OffsetDateTime type.",
                        parameter
                    )
                }
            }
            is PropertyKind.Ignore -> {
                if (!property.parameter.hasDefault) {
                    report("The ignored property must have a default value.", parameter)
                }
            }
            else -> Unit
        }
    }

    private fun validateIdGeneratorKind(property: Property) {
        val parameter = property.parameter
        when (property.idGeneratorKind) {
            is IdGeneratorKind.Identity -> {
                when (property.typeName) {
                    "kotlin.Int" -> Unit
                    "kotlin.Long" -> Unit
                    else -> report(
                        "The identity generator property must be either Int or Long type.",
                        parameter
                    )
                }
            }
            is IdGeneratorKind.Sequence -> {
                when (property.typeName) {
                    "kotlin.Int" -> Unit
                    "kotlin.Long" -> Unit
                    else -> report(
                        "The sequence generator property must be either Int or Long type.",
                        parameter
                    )
                }
            }
        }
    }

    private fun validateAllProperties(properties: Sequence<Property>) {
        val propertyDefMap = entityDef.properties.associateBy { it.declaration.simpleName }
        val propertyMap = properties.associateBy { it.declaration.simpleName }
        for ((key, value) in propertyDefMap) {
            propertyMap[key]
                ?: report("The same name property is not found in the entity.", value.parameter)
        }
        val idGeneratorProperties = properties.filter { it.idGeneratorKind != null }.toList()
        if (idGeneratorProperties.size > 1) {
            report("Multiple generator properties cannot coexist in a single class.", entityDef.definitionSource.entityDeclaration)
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
    }
}
