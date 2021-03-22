package org.komapper.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal class EntityVisitor : KSEmptyVisitor<Unit, EntityVisitResult>() {

    override fun defaultHandler(node: KSNode, data: Unit): EntityVisitResult {
        error("The node must be KSClassDeclaration.")
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): EntityVisitResult {
        val factory = EntityFactory(classDeclaration)
        return try {
            val entity = factory.create()
            EntityVisitResult.Success(entity)
        } catch (e: Exit) {
            EntityVisitResult.Failure(classDeclaration, e)
        }
    }
}

internal class EntityFactory(private val classDeclaration: KSClassDeclaration) {
    init {
        val modifiers = classDeclaration.modifiers
        if (!modifiers.contains(Modifier.DATA)) {
            report("@KmEntity must be applied to data class.", classDeclaration)
        }
        if (modifiers.contains(Modifier.PRIVATE)) {
            report("@KmEntity cannot be applied to private data class.", classDeclaration)
        }
        if (classDeclaration.typeParameters.isNotEmpty()) {
            report("@KmEntity annotated class must not have type parameters.", classDeclaration)
        }
    }

    fun create(): Entity {
        val tableName = getTableName()
        val allProperties = createAllProperties()
        val idProperties = allProperties.filter { it.kind is PropertyKind.Id }
        val versionProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.Version }
        val createdAtProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.CreatedAt }
        val updatedAtProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.UpdatedAt }
        val ignoredProperties = allProperties.filter { it.kind is PropertyKind.Ignore }
        val idGenerator: IdGenerator? = createIdGenerator(allProperties)
        return Entity(
            classDeclaration,
            tableName,
            (allProperties - ignoredProperties).toList(),
            idProperties.toList(),
            versionProperty,
            createdAtProperty,
            updatedAtProperty,
            idGenerator
        )
    }

    private fun createAllProperties(): Sequence<Property> {
        val propertyDeclarationMap = classDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        return classDeclaration.primaryConstructor?.parameters
            ?.asSequence()
            ?.map { parameter ->
                val declaration = propertyDeclarationMap[parameter.name]
                    ?: report("The corresponding property is not found.", parameter)
                val columnName = getColumnName(parameter)
                val type = parameter.type.resolve()
                val typeName = (type.declaration.qualifiedName ?: type.declaration.simpleName).asString()
                val nullability = type.nullability
                val kind = createPropertyKind(parameter)
                val generatorKind = createIdGeneratorKind(parameter, kind)
                Property(parameter, declaration, columnName, typeName, nullability, kind, generatorKind).also {
                    validateProperty(it)
                }
            }?.also {
                validateProperties(it)
            } ?: emptySequence()
    }

    private fun validateProperty(property: Property) {
        if (property.isPrivate()) {
            report("The parameter must not be private.", property.parameter)
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
                        "@KmVersion cannot apply to ${parameter.type} type. " +
                            "Either Int or Long is available.",
                        parameter
                    )
                }
            }
            is PropertyKind.CreatedAt -> {
                when (property.typeName) {
                    "java.time.LocalDateTime" -> Unit
                    else -> report(
                        "@KmCreatedAt cannot apply to ${parameter.type} type. " +
                            "java.time.LocalDateTime is available.",
                        parameter
                    )
                }
            }
            is PropertyKind.UpdatedAt -> {
                when (property.typeName) {
                    "java.time.LocalDateTime" -> Unit
                    else -> report(
                        "@KmUpdatedAt cannot apply to ${parameter.type} type. " +
                            "java.time.LocalDateTime is available.",
                        parameter
                    )
                }
            }
            is PropertyKind.Ignore -> {
                if (!property.parameter.hasDefault) {
                    report("@KmIgnore annotated parameter must have default value.", parameter)
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
                        "@KmIdentityGenerator cannot apply to ${parameter.type} type. " +
                            "Either Int or Long is available.",
                        parameter
                    )
                }
            }
            is IdGeneratorKind.Sequence -> {
                when (property.typeName) {
                    "kotlin.Int" -> Unit
                    "kotlin.Long" -> Unit
                    else -> report(
                        "@KmSequenceGenerator cannot apply to ${parameter.type} type. " +
                            "Either Int or Long is available.",
                        parameter
                    )
                }
            }
        }
    }

    private fun validateProperties(properties: Sequence<Property>) {
        if (properties.anyDuplicates { it.kind is PropertyKind.Version }) {
            report("Multiple @KmVersion cannot coexist in a single class.", classDeclaration)
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.CreatedAt }) {
            report("Multiple @KmCreatedAt cannot coexist in a single class.", classDeclaration)
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.UpdatedAt }) {
            report("Multiple @KmUpdatedAt cannot coexist in a single class.", classDeclaration)
        }
        if (properties.all { it.kind is PropertyKind.Ignore }) {
            report("Any persistent properties are not found.", classDeclaration)
        }
    }

    private fun getTableName(): String {
        return classDeclaration.findAnnotation("KmTable")
            ?.findValue("name")?.toString()
            ?: classDeclaration.simpleName.asString().toUpperCase()
    }

    private fun getColumnName(parameter: KSValueParameter): String {
        return parameter.findAnnotation("KmColumn")
            ?.findValue("name")?.toString()
            ?: parameter.toString().toUpperCase()
    }

    private fun createPropertyKind(parameter: KSValueParameter): PropertyKind? {
        var id: PropertyKind.Id? = null
        var version: PropertyKind.Version? = null
        var createdAt: PropertyKind.CreatedAt? = null
        var updatedAt: PropertyKind.UpdatedAt? = null
        var ignore: PropertyKind.Ignore? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                "KmId" -> id = PropertyKind.Id(a)
                "KmVersion" -> version = PropertyKind.Version(a)
                "KmCreatedAt" -> createdAt = PropertyKind.CreatedAt(a)
                "KmUpdatedAt" -> updatedAt = PropertyKind.UpdatedAt(a)
                "KmIgnore" -> ignore = PropertyKind.Ignore(a)
            }
        }
        val kinds = listOfNotNull(id, version, createdAt, updatedAt, ignore)
        if (kinds.size > 1) {
            val iterator = kinds.iterator()
            val a1 = iterator.next().annotation
            val a2 = iterator.next().annotation
            report("$a1 and $a2 cannot coexist on the same parameter.", parameter)
        }
        return kinds.firstOrNull()
    }

    private fun createIdGeneratorKind(parameter: KSValueParameter, propertyKind: PropertyKind?): IdGeneratorKind? {
        var identity: IdGeneratorKind.Identity? = null
        var sequence: IdGeneratorKind.Sequence? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                "KmIdentityGenerator" -> identity = IdGeneratorKind.Identity(a)
                "KmSequenceGenerator" -> sequence = let {
                    val name = a.findValue("name")
                        ?: report("@KmSequenceGenerator.name is not found.", a)
                    val incrementBy = a.findValue("incrementBy")
                        ?: report("@KmSequenceGenerator.incrementBy is not found.", a)
                    IdGeneratorKind.Sequence(a, name, incrementBy)
                }
            }
        }
        val idGeneratorKinds = listOfNotNull(identity, sequence)
        if (idGeneratorKinds.size > 1) {
            val iterator = idGeneratorKinds.iterator()
            val a1 = iterator.next().annotation
            val a2 = iterator.next().annotation
            report("$a1 and $a2 cannot coexist on the same parameter.", parameter)
        }
        val idGeneratorKind = idGeneratorKinds.firstOrNull() ?: return null
        if (propertyKind !is PropertyKind.Id) {
            report("${idGeneratorKind.annotation} and @KmId must coexist on the same parameter.", parameter)
        }
        return idGeneratorKind
    }

    private fun createIdGenerator(properties: Sequence<Property>): IdGenerator? {
        val idGeneratorProperties = properties.filter { it.idGeneratorKind != null }.toList()
        if (idGeneratorProperties.size > 1) {
            report("Multiple Generators cannot coexist in a single class.", classDeclaration)
        }
        val property = idGeneratorProperties.firstOrNull() ?: return null
        return IdGenerator(property)
    }
}
