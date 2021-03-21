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

class EntityFactory(private val classDeclaration: KSClassDeclaration) {
    fun create(): Entity {
        validateKSClassDeclaration(classDeclaration)
        val tableName = tableName(classDeclaration)
        val map = classDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        val allProperties = classDeclaration.primaryConstructor?.parameters
            ?.asSequence()
            ?.map { parameter ->
                val declaration = map[parameter.name]
                    ?: report("The corresponding property is not found.", parameter)
                val columnName = columnName(parameter)
                val type = parameter.type.resolve()
                val typeName = (type.declaration.qualifiedName ?: type.declaration.simpleName).asString()
                val nullability = type.nullability
                val kind = propertyKind(parameter)
                val generatorKind = idGeneratorKind(parameter, kind)
                Property(parameter, declaration, columnName, typeName, nullability, kind, generatorKind)
            }?.also {
                validateProperties(it)
            } ?: emptySequence()
        val idProperties = allProperties.filter { it.kind is PropertyKind.Id }
        val versionProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.Version }
        val createdAtProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.CreatedAt }
        val updatedAtProperty: Property? = allProperties.firstOrNull { it.kind is PropertyKind.UpdatedAt }
        val ignoredProperties = allProperties.filter { it.kind is PropertyKind.Ignore }
        val properties = allProperties - ignoredProperties
        val idGenerator: IdGenerator? = idGenerator(allProperties)
        return Entity(
            classDeclaration,
            tableName,
            properties.toList(),
            idProperties.toList(),
            versionProperty,
            createdAtProperty,
            updatedAtProperty,
            idGenerator
        )
    }

    private fun validateKSClassDeclaration(classDeclaration: KSClassDeclaration) {
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

    private fun validateProperties(properties: Sequence<Property>) {
        if (properties.anyDuplicates { it.kind is PropertyKind.Version }) {
            report("Multiple @KmVersion cannot coexist in a single class.")
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.CreatedAt }) {
            report("Multiple @KmCreatedAt cannot coexist in a single class.")
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.UpdatedAt }) {
            report("Multiple @KmUpdatedAt cannot coexist in a single class.")
        }
        val persistentProperties = properties.filter { it.kind !is PropertyKind.Ignore }
        if (persistentProperties.none()) {
            report("Any persistent properties are not found.", classDeclaration)
        }
        persistentProperties.firstOrNull { it.isPrivate() }?.let {
            report("The parameter must not be private.", it.parameter)
        }
    }

    private fun tableName(declaration: KSClassDeclaration): String {
        return declaration.annotations
            .asSequence()
            .filter { it.shortName.asString() == "KmTable" }
            .map { annotation ->
                annotation.arguments
                    .filter { it.name?.asString() == "name" }
                    .map { it.value?.toString() }
                    .firstOrNull()
            }.firstOrNull() ?: declaration.simpleName.asString().toUpperCase()
    }

    private fun columnName(parameter: KSValueParameter): String {
        return parameter.annotations
            .asSequence()
            .filter { it.shortName.asString() == "KmColumn" }
            .map { annotation ->
                annotation.arguments
                    .filter { it.name?.asString() == "name" }
                    .map { it.value?.toString() }
                    .firstOrNull()
            }.firstOrNull() ?: parameter.toString().toUpperCase()
    }

    private fun propertyKind(parameter: KSValueParameter): PropertyKind? {
        var id: PropertyKind.Id? = null
        var version: PropertyKind.Version? = null
        var createdAt: PropertyKind.CreatedAt? = null
        var updatedAt: PropertyKind.UpdatedAt? = null
        var ignore: PropertyKind.Ignore? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                "KmId" -> id = PropertyKind.Id(a)
                "KmVersion" -> {
                    version = PropertyKind.Version(a)
                }
                "KmCreatedAt" -> {
                    createdAt = PropertyKind.CreatedAt(a)
                }
                "KmUpdatedAt" -> {
                    updatedAt = PropertyKind.UpdatedAt(a)
                }
                "KmIgnore" -> {
                    ignore = PropertyKind.Ignore(a)
                }
            }
        }
        val kinds = listOfNotNull(id, version, createdAt, updatedAt, ignore)
        if (kinds.isEmpty()) {
            return null
        }
        if (kinds.size == 1) {
            return kinds.first().also { it.check(parameter) }
        }
        val a1 = kinds[0].annotation
        val a2 = kinds[1].annotation
        report("$a1 and $a2 cannot coexist on the same parameter.", parameter)
    }

    private fun idGeneratorKind(parameter: KSValueParameter, propertyKind: PropertyKind?): IdGeneratorKind? {
        var identity: IdGeneratorKind.Identity? = null
        var sequence: IdGeneratorKind.Sequence? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                "KmIdentityGenerator" -> identity = IdGeneratorKind.Identity(a)
                "KmSequenceGenerator" -> sequence = IdGeneratorKind.Sequence(a)
            }
        }
        val idGeneratorKinds = listOfNotNull(identity, sequence)
        if (idGeneratorKinds.isEmpty()) {
            return null
        }
        if (idGeneratorKinds.size == 1) {
            val kind = idGeneratorKinds.first().also { it.check(parameter) }
            if (propertyKind !is PropertyKind.Id) {
                report("${kind.annotation} and @KmId must coexist on the same parameter.", parameter)
            }
            return kind
        }
        val a1 = idGeneratorKinds[0].annotation
        val a2 = idGeneratorKinds[1].annotation
        report("$a1 and $a2 cannot coexist on the same parameter.", parameter)
    }

    private fun idGenerator(properties: Sequence<Property>): IdGenerator? {
        val idGeneratorProperties = properties.filter { it.idGeneratorKind != null }.toList()
        if (idGeneratorProperties.isEmpty()) {
            return null
        }
        val property = idGeneratorProperties.first()
        if (idGeneratorProperties.size > 1) {
            report("Multiple Generators cannot coexist in a single class.", property.parameter)
        }
        return IdGenerator(property)
    }
}
