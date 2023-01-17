package org.komapper.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability

internal data class EntityDefinitionSource(
    val defDeclaration: KSClassDeclaration,
    val entityDeclaration: KSClassDeclaration,
    val aliases: List<String>,
    val unitDeclaration: KSClassDeclaration?,
    val stubAnnotation: KSAnnotation?,
)

internal data class EntityDef(
    val definitionSource: EntityDefinitionSource,
    val table: Table,
    val properties: List<PropertyDef>,
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
    val enumStrategy: EnumStrategy?,
) : PropertyDef

internal data class CompositePropertyDef(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val kind: PropertyKind,
) : PropertyDef

internal data class Entity(
    val declaration: KSClassDeclaration,
    val table: Table,
    val properties: List<Property>,
    val embeddedIdProperty: CompositeProperty?,
    val virtualEmbeddedIdProperty: CompositeProperty?,
    val idProperties: List<LeafProperty>,
    val virtualIdProperties: List<LeafProperty>,
    val versionProperty: LeafProperty?,
    val createdAtProperty: LeafProperty?,
    val updatedAtProperty: LeafProperty?,
)

internal data class Embeddable(
    val type: KSType,
    val properties: List<LeafProperty>,
) {
    val typeName = type.name
    val simpleName get() = type.declaration.simpleName.asString()
}

internal sealed interface Property {
    val parameter: KSValueParameter
    val declaration: KSPropertyDeclaration
    val nullability: Nullability
    val kind: PropertyKind?
    val node: KSNode
}

internal data class LeafProperty(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val nullability: Nullability,
    override val kind: PropertyKind?,
    val typeArgument: KSTypeArgument?,
    val column: Column,
    val kotlinClass: KotlinClass,
    val literalTag: String,
    val parent: KSValueParameter? = null,
) : Property {
    val typeName get() = kotlinClass.exteriorTypeName
    val exteriorTypeName get() = kotlinClass.exteriorTypeName
    val interiorTypeName get() = kotlinClass.interiorTypeName
    val name get() = parameter.toString()
    val path get() = if (parent == null) name else "$parent.$name"
    override val node get() = parent ?: parameter

    fun isPrivate() = declaration.isPrivate()

    override fun toString(): String {
        return parameter.toString()
    }
}

internal data class CompositeProperty(
    override val parameter: KSValueParameter,
    override val declaration: KSPropertyDeclaration,
    override val kind: PropertyKind?,
    override val nullability: Nullability,
    val embeddable: Embeddable,
) : Property {
    override val node = parameter

    override fun toString(): String {
        return parameter.toString()
    }
}

internal sealed interface KotlinClass {
    val type: KSType
    val declaration: KSDeclaration get() = type.declaration
    val exteriorTypeName: String get() = type.name
    val interiorTypeName: String
}

internal data class EnumClass(
    override val type: KSType,
    override val interiorTypeName: String,
    val strategy: EnumStrategy,
) : KotlinClass {
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
    val column: Column?,
) : KotlinClass {
    val isArray: Boolean = declaration.qualifiedName?.asString() == "kotlin.Array"

    override val exteriorTypeName: String
        get() {
            return if (isArray) {
                val nonNullableType =
                    if (type.isMarkedNullable) {
                        type.makeNotNullable()
                    } else {
                        type
                    }
                nonNullableType.name
            } else {
                super.exteriorTypeName
            }
        }

    override val interiorTypeName: String get() {
        return when (val strategy = column?.mappingStrategy) {
            is MappingStrategy.ClobString -> strategy.interiorTypeName
            else -> exteriorTypeName
        }
    }
    override fun toString(): String = exteriorTypeName
}

sealed interface EnumStrategy {
    val propertyName: String

    object Name : EnumStrategy {
        override val propertyName: String = "name"
        const val typeName: String = "String"
    }

    object Ordinal : EnumStrategy {
        override val propertyName: String = "ordinal"
        const val typeName: String = "Int"
    }

    data class Property(
        override val propertyName: String,
        val annotation: KSAnnotation,
    ) : EnumStrategy
}

sealed interface MappingStrategy {
    object Default : MappingStrategy
    object ClobString : MappingStrategy {
        val interiorTypeName = Symbols.ClobString
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

    data class Embedded(override val annotation: KSAnnotation) : PropertyKind()
    data class EmbeddedId(override val annotation: KSAnnotation, val virtual: Boolean) : PropertyKind()
    data class Id(override val annotation: KSAnnotation, val idKind: IdKind?, val virtual: Boolean) : PropertyKind()
    data class Version(override val annotation: KSAnnotation) : PropertyKind()
    data class UpdatedAt(override val annotation: KSAnnotation) : PropertyKind()
    data class CreatedAt(override val annotation: KSAnnotation) : PropertyKind()
    data class Ignore(override val annotation: KSAnnotation) : PropertyKind()
}

internal sealed class IdKind {
    abstract val annotation: KSAnnotation

    data class AutoIncrement(
        override val annotation: KSAnnotation,
    ) : IdKind()

    data class Sequence(
        override val annotation: KSAnnotation,
        val name: String,
        val startWith: Any,
        val incrementBy: Any,
        val catalog: String,
        val schema: String,
        val alwaysQuote: Boolean,
    ) :
        IdKind()
}

internal data class Table(
    val name: String,
    val catalog: String,
    val schema: String,
    val alwaysQuote: Boolean,
)

internal data class Column(
    val name: String,
    val alwaysQuote: Boolean,
    val masking: Boolean,
    val mappingStrategy: MappingStrategy,
)
