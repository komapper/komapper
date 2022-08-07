package org.komapper.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
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

internal data class EmbeddableDef(
    val declaration: KSClassDeclaration,
    val properties: List<LeafPropertyDef>
)

internal sealed interface PropertyDef {
    val parameter: KSValueParameter
    val declaration: KSPropertyDeclaration
    val kind: PropertyKind?
}

internal data class LeafPropertyDef(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val kind: PropertyKind?,
    val column: Column,
    val enumStrategy: EnumStrategy?
) : PropertyDef

internal data class CompositePropertyDef(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val kind: PropertyKind?,
    val embeddableDef: EmbeddableDef,
) : PropertyDef

internal data class Entity(
    val declaration: KSClassDeclaration,
    val table: Table,
    val properties: List<Property>,
    val embeddedIdProperty: CompositeProperty?,
    val idProperties: List<LeafProperty>,
    val versionProperty: LeafProperty?,
    val createdAtProperty: LeafProperty?,
    val updatedAtProperty: LeafProperty?,
)

internal data class Embeddable(
    val declaration: KSClassDeclaration,
    val properties: List<LeafProperty>,
) {
    val qualifiedName get() = declaration.qualifiedName?.asString() ?: simpleName
    val simpleName get() = declaration.simpleName.asString()
}

internal sealed interface Property {
    val parameter: KSValueParameter
    val declaration: KSPropertyDeclaration
    val nullability: Nullability
    val kind: PropertyKind?
}
internal data class LeafProperty(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val nullability: Nullability,
    override val kind: PropertyKind?,
    val column: Column,
    val kotlinClass: KotlinClass,
    val literalTag: String,
) : Property {
    val typeName get() = kotlinClass.exteriorTypeName
    val exteriorTypeName get() = kotlinClass.exteriorTypeName
    val interiorTypeName get() = kotlinClass.interiorTypeName
    fun isPrivate() = declaration.isPrivate()

    override fun toString(): String {
        return parameter.toString()
    }
}

internal data class CompositeProperty(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val nullability: Nullability,
    override val kind: PropertyKind?,
    val embeddable: Embeddable,
) : Property {
    override fun toString(): String {
        return parameter.toString()
    }
}

internal sealed interface KotlinClass {
    val type: KSType
    val declaration: KSDeclaration get() = type.declaration
    val exteriorTypeName: String get() = (declaration.qualifiedName ?: declaration.simpleName).asString()
    val interiorTypeName: String
}

internal data class EnumClass(
    override val type: KSType,
    val strategy: EnumStrategy
) : KotlinClass {
    override val interiorTypeName: String get() = strategy.typeName
    override fun toString(): String = exteriorTypeName
}

internal data class ValueClass(
    override val type: KSType,
    val property: ValueClassProperty,
) : KotlinClass {
    override val interiorTypeName: String get() = property.typeName
    override fun toString(): String = exteriorTypeName
}

internal data class PlainClass(
    override val type: KSType,
) : KotlinClass {
    val isArray: Boolean = declaration.qualifiedName?.asString() == "kotlin.Array"

    override val exteriorTypeName: String
        get() {
            return if (isArray) {
                val nonNullableType =
                    if (type.isMarkedNullable) { type.makeNotNullable() } else type
                nonNullableType.buildQualifiedName()
            } else {
                super.exteriorTypeName
            }
        }

    override val interiorTypeName: String get() = exteriorTypeName
    override fun toString(): String = exteriorTypeName
}

enum class EnumStrategy(val propertyName: String, val typeName: String) {
    NAME("name", "String"),
    ORDINAL("ordinal", "Int")
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
    data class Embedded(override val annotation: KSAnnotation) : PropertyKind()
    data class EmbeddedId(override val annotation: KSAnnotation) : PropertyKind()
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
