package org.komapper.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.processor.ClassNames.KomapperStub

internal interface EntityDefinitionSourceResolver {
    fun resolve(symbol: KSNode): EntityDefinitionSource
}

internal class SeparateDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val defDeclaration = symbol.accept(
            object : ClassDeclarationVisitor() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration {
                    report("@${KomapperEntityDef::class.simpleName} cannot be applied to this element.", node)
                }
            },
            Unit
        )
        val defAnnotation = defDeclaration.findAnnotation(KomapperEntityDef::class)
        val entity = defAnnotation?.findValue("entity")
        val aliases = defAnnotation?.findValue("aliases")
        if (entity !is KSType) {
            report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        }
        if (aliases !is List<*>) {
            report("The aliases value of @${KomapperEntityDef::class.simpleName} is invalid.", defDeclaration)
        }
        val entityDeclaration = entity.declaration.accept(
            object : ClassDeclarationVisitor() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration {
                    report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
                }
            },
            Unit
        )
        validateEntityDeclaration(entityDeclaration)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        return EntityDefinitionSource(defDeclaration, entityDeclaration, aliases.map { it.toString() }, stubAnnotation)
    }
}

internal class SelfDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val entityDeclaration = symbol.accept(
            object : ClassDeclarationVisitor() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration {
                    report("@${KomapperEntity::class.simpleName} cannot be applied to this element.", node)
                }
            },
            Unit
        )
        val annotation = entityDeclaration.findAnnotation(KomapperEntity::class)
        val aliases = annotation?.findValue("aliases")
        if (aliases !is List<*>) {
            report("The aliases value of @${KomapperEntity::class.simpleName} is invalid.", entityDeclaration)
        }
        validateEntityDeclaration(entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        return EntityDefinitionSource(entityDeclaration, entityDeclaration, aliases.map { it.toString() }, stubAnnotation)
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
    validateParentDeclaration(entityDeclaration.parentDeclaration)
}

private fun validateParentDeclaration(declaration: KSDeclaration?) {
    if (declaration == null) return
    if (!declaration.isPublic()) {
        report("The parent declaration of the entity class must be public.", declaration)
    }
    validateParentDeclaration(declaration.parentDeclaration)
}
