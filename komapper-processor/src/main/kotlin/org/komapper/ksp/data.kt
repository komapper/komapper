package org.komapper.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability

data class EntityDefinitionSource(
    val defDeclaration: KSClassDeclaration,
    val entityDeclaration: KSClassDeclaration
)

internal data class EntityDef(
    val definitionSource: EntityDefinitionSource,
    val table: Table,
    val properties: List<PropertyDef>,
)

internal data class PropertyDef(
    val parameter: KSValueParameter,
    val declaration: KSPropertyDeclaration,
    val column: Column,
    val kind: PropertyKind?,
    val idGeneratorKind: IdGeneratorKind?
)

internal data class Entity(
    val declaration: KSClassDeclaration,
    val table: Table,
    val properties: List<Property>,
    val idProperties: List<Property>,
    val versionProperty: Property?,
    val createdAtProperty: Property?,
    val updatedAtProperty: Property?,
)

internal data class Property(
    val parameter: KSValueParameter,
    val declaration: KSPropertyDeclaration,
    val column: Column,
    val typeName: String,
    val nullability: Nullability,
    val kind: PropertyKind?,
    val idGeneratorKind: IdGeneratorKind?
) {
    fun isPrivate() = declaration.isPrivate()

    override fun toString(): String {
        return parameter.toString()
    }
}

internal sealed class PropertyKind {
    abstract val annotation: KSAnnotation

    data class Id(override val annotation: KSAnnotation) : PropertyKind()
    data class Version(override val annotation: KSAnnotation) : PropertyKind()
    data class UpdatedAt(override val annotation: KSAnnotation) : PropertyKind()
    data class CreatedAt(override val annotation: KSAnnotation) : PropertyKind()
    data class Ignore(override val annotation: KSAnnotation) : PropertyKind()
}

internal sealed class IdGeneratorKind {
    abstract val annotation: KSAnnotation

    data class Identity(override val annotation: KSAnnotation) : IdGeneratorKind()
    data class Sequence(
        override val annotation: KSAnnotation,
        val name: String,
        val incrementBy: Any,
        val catalog: String,
        val schema: String
    ) :
        IdGeneratorKind()
}

internal data class IdGenerator(val property: Property) {
    val name = "__${property}Generator"
    val kind = property.idGeneratorKind
}

internal data class Table(
    val name: String,
    val catalog: String,
    val schema: String,
)

internal data class Column(val name: String)
