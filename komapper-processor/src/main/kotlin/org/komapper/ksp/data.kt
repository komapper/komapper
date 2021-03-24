package org.komapper.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Nullability

internal sealed class EntityVisitResult {
    abstract val declaration: KSClassDeclaration

    data class Success(val entity: Entity) : EntityVisitResult() {
        override val declaration = entity.declaration
    }

    data class Failure(
        override val declaration: KSClassDeclaration,
        val message: String,
        val node: KSNode?
    ) :
        EntityVisitResult()
}

internal data class Entity(
    val declaration: KSClassDeclaration,
    val tableName: String,
    val properties: List<Property>,
    val idProperties: List<Property>,
    val versionProperty: Property?,
    val createdAtProperty: Property?,
    val updatedAtProperty: Property?,
    val idGenerator: IdGenerator?
)

internal data class Property(
    val parameter: KSValueParameter,
    val declaration: KSPropertyDeclaration,
    val columnName: String,
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
    data class Sequence(override val annotation: KSAnnotation, val name: Any, val incrementBy: Any) :
        IdGeneratorKind()
}

internal data class IdGenerator(val property: Property) {
    val name = "${property}__generator"
    val kind = property.idGeneratorKind
}
