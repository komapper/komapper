package org.komapper.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
        val exit: Exit
    ) :
        EntityVisitResult()
}

data class Entity(
    val declaration: KSClassDeclaration,
    val tableName: String,
    val properties: List<Property>,
    val idProperties: List<Property>,
    val versionProperty: Property?,
    val createdAtProperty: Property?,
    val updatedAtProperty: Property?,
    val idGenerator: IdGenerator?
)

data class Property(
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

sealed class PropertyKind {
    abstract val annotation: KSAnnotation
    abstract fun check(parameter: KSValueParameter)
    data class Id(override val annotation: KSAnnotation) : PropertyKind() {
        override fun check(parameter: KSValueParameter) {}
    }

    data class Version(override val annotation: KSAnnotation) : PropertyKind() {
        override fun check(parameter: KSValueParameter) {
            when (parameter.type.resolve().declaration.qualifiedName?.asString()) {
                "kotlin.Int" -> Unit
                "kotlin.Long" -> Unit
                else -> report(
                    "@KmVersion cannot apply to ${parameter.type} type. " +
                        "Either Int or Long is available.",
                    parameter
                )
            }
        }
    }

    data class UpdatedAt(override val annotation: KSAnnotation) : PropertyKind() {
        override fun check(parameter: KSValueParameter) {
            return when (parameter.type.resolve().declaration.qualifiedName?.asString()) {
                "java.time.LocalDateTime" -> Unit
                else -> report(
                    "@KmUpdated cannot apply to ${parameter.type} type. " +
                        "java.time.LocalDateTime is available.",
                    parameter
                )
            }
        }
    }

    data class CreatedAt(override val annotation: KSAnnotation) : PropertyKind() {
        override fun check(parameter: KSValueParameter) {
            return when (parameter.type.resolve().declaration.qualifiedName?.asString()) {
                "java.time.LocalDateTime" -> Unit
                else -> report(
                    "@KmCreated cannot apply to ${parameter.type} type. " +
                        "java.time.LocalDateTime is available.",
                    parameter
                )
            }
        }
    }

    data class Ignore(override val annotation: KSAnnotation) : PropertyKind() {
        override fun check(parameter: KSValueParameter) {
            if (!parameter.hasDefault) {
                report("@KmIgnore annotated parameter must have default value.", parameter)
            }
        }
    }
}

sealed class IdGeneratorKind {
    abstract val annotation: KSAnnotation
    abstract fun check(parameter: KSValueParameter)

    data class Identity(override val annotation: KSAnnotation) : IdGeneratorKind() {
        override fun check(parameter: KSValueParameter) {
            return when (parameter.type.resolve().declaration.qualifiedName?.asString()) {
                "kotlin.Int" -> Unit
                "kotlin.Long" -> Unit
                else -> report(
                    "@KmIdentityGenerator cannot apply to ${parameter.type} type. " +
                        "Either Int or Long is available.",
                    parameter
                )
            }
        }
    }

    data class Sequence(override val annotation: KSAnnotation) :
        IdGeneratorKind() {
        val name = annotation.arguments
            .filter { it.name?.asString() == "name" }
            .map { it.value.toString() }
            .first()
        val incrementBy = annotation.arguments
            .filter { it.name?.asString() == "incrementBy" }
            .map { it.value as Int }
            .first()

        override fun check(parameter: KSValueParameter) {
            return when (parameter.type.resolve().declaration.qualifiedName?.asString()) {
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

data class IdGenerator(val property: Property) {
    val name = "${property}__generator"
    val kind = property.idGeneratorKind
}
