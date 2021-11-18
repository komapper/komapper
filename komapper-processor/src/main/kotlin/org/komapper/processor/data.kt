package org.komapper.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability

internal data class EntityDefinitionSource(
    val defDeclaration: KSClassDeclaration,
    val entityDeclaration: KSClassDeclaration
)

internal data class EntityDef(
    val definitionSource: EntityDefinitionSource,
    val table: Table,
    val properties: List<PropertyDef>
)

internal data class PropertyDef(
    val parameter: KSValueParameter,
    val declaration: KSPropertyDeclaration,
    val column: Column,
    val kind: PropertyKind?,
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
    val literalTag: String,
    val valueClass: ValueClass?,
    val nullability: Nullability,
    val kind: PropertyKind?,
) {
    fun isPrivate() = declaration.isPrivate()

    override fun toString(): String {
        return parameter.toString()
    }
}

internal data class ValueClass(
    val declaration: KSClassDeclaration,
    val property: ValueClassProperty,
) {
    override fun toString(): String {
        return declaration.qualifiedName?.asString() ?: ""
    }
}

internal data class ValueClassProperty(
    val parameter: KSValueParameter,
    val declaration: KSPropertyDeclaration,
    val typeName: String,
    val literalTag: String,
    val nullability: Nullability,
) {
    fun isPrivate() = declaration.isPrivate()

    override fun toString(): String {
        return parameter.toString()
    }
}

internal sealed class PropertyKind {
    abstract val annotation: KSAnnotation

    data class Id(override val annotation: KSAnnotation, val idKind: IdKind?) : PropertyKind()
    data class Version(override val annotation: KSAnnotation) : PropertyKind()
    data class UpdatedAt(override val annotation: KSAnnotation) : PropertyKind()
    data class CreatedAt(override val annotation: KSAnnotation) : PropertyKind()
    data class Ignore(override val annotation: KSAnnotation) : PropertyKind()
}

internal sealed class IdKind {
    abstract val annotation: KSAnnotation

    data class AutoIncrement(
        override val annotation: KSAnnotation
    ) : IdKind()

    data class Sequence(
        override val annotation: KSAnnotation,
        val name: String,
        val startWith: Any,
        val incrementBy: Any,
        val catalog: String,
        val schema: String,
        val alwaysQuote: Boolean
    ) :
        IdKind()
}

internal data class Table(
    val name: String,
    val catalog: String,
    val schema: String,
    val alwaysQuote: Boolean
)

internal data class Column(val name: String, val alwaysQuote: Boolean)
