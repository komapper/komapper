package org.komapper.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier

internal interface EntityDefinitionSourceResolver {
    fun resolve(symbol: KSNode): EntityDefinitionSource
}

internal class SeparateDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val defDeclaration = symbol.accept(
            object : ClassDeclarationVisitor() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration {
                    report("@KmEntityDef cannot be applied to this element.", node)
                }
            },
            Unit
        )
        val annotation = defDeclaration.findAnnotation("KmEntityDef")
        val value = annotation?.findValue("entity")
        if (value !is KSType) {
            report("The entity value of @KmEntityDef is not found.", defDeclaration)
        }
        val entityDeclaration = value.declaration.accept(
            object : ClassDeclarationVisitor() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration {
                    report("The entity value of @KmEntityDef is not found.", defDeclaration)
                }
            },
            Unit
        )
        if (entityDeclaration.findAnnotation("KmEntity") != null) {
            report(
                "Duplicated definitions are found. " +
                    "The referenced entity class is already annotated with @KmEntity.",
                annotation
            )
        }
        validateEntityDeclaration(entityDeclaration)
        return EntityDefinitionSource(defDeclaration, entityDeclaration)
    }
}

internal class SelfDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val entityDeclaration = symbol.accept(
            object : ClassDeclarationVisitor() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration {
                    report("@KmEntity cannot be applied to this element.", node)
                }
            },
            Unit
        )
        validateEntityDeclaration(entityDeclaration)
        return EntityDefinitionSource(entityDeclaration, entityDeclaration)
    }
}

private fun validateEntityDeclaration(entityDeclaration: KSClassDeclaration) {
    val modifiers = entityDeclaration.modifiers
    if (!modifiers.contains(Modifier.DATA)) {
        report("The entity class must be a data class.", entityDeclaration)
    }
    if (entityDeclaration.typeParameters.isNotEmpty()) {
        report("The entity class must not have type parameters.", entityDeclaration)
    }
    if (entityDeclaration.isPrivate()) {
        report("The entity class must not be private.", entityDeclaration)
    }
}
