package org.komapper.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability

internal data class EntityDefinitionSource(
    val defDeclaration: KSClassDeclaration,
    val entityDeclaration: KSClassDeclaration,
    val aliases: List<String>,
    val stubAnnotation: KSAnnotation?,
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
    val kotlinClass: KotlinClass,
    val literalTag: String,
    val nullability: Nullability,
    val kind: PropertyKind?,
) {
    val typeName get() = kotlinClass.exteriorTypeName
    val exteriorTypeName get() = kotlinClass.exteriorTypeName
    val interiorTypeName get() = kotlinClass.interiorTypeName
    fun isPrivate() = declaration.isPrivate()

    override fun toString(): String {
        return parameter.toString()
    }
}

internal sealed class KotlinClass {
    abstract val declaration: KSDeclaration
    val exteriorTypeName: String get() = (declaration.qualifiedName ?: declaration.simpleName).asString()
    abstract val interiorTypeName: String
    override fun toString(): String {
        return exteriorTypeName
    }
}

internal data class EnumClass(
    override val declaration: KSClassDeclaration
) : KotlinClass() {
    override val interiorTypeName: String = "String"
    override fun toString(): String {
        return super.toString()
    }
}

internal data class ValueClass(
    override val declaration: KSClassDeclaration,
    val property: ValueClassProperty,
) : KotlinClass() {
    override val interiorTypeName: String get() = property.typeName
    override fun toString(): String {
        return super.toString()
    }
}

internal data class PlainClass(
    override val declaration: KSDeclaration
) : KotlinClass() {
    override val interiorTypeName: String get() = exteriorTypeName
    override fun toString(): String {
        return super.toString()
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

internal data class Column(
    val name: String,
    val alwaysQuote: Boolean,
    val masking: Boolean
)
