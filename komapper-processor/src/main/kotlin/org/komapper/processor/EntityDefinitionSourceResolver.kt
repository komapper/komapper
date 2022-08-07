package org.komapper.processor

import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.processor.Symbols.KomapperStub

internal interface EntityDefinitionSourceResolver {
    fun resolve(symbol: KSNode): EntityDefinitionSource
}

internal class SeparateDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val defDeclaration = symbol.accept(ClassDeclarationVisitor(), Unit)
            ?: report("@${KomapperEntityDef::class.simpleName} cannot be applied to this element.", symbol)
        val defAnnotation = defDeclaration.findAnnotation(KomapperEntityDef::class)
        val entity = defAnnotation?.findValue("entity")
        val aliases = defAnnotation?.findValue("aliases")
        if (entity !is KSType) {
            report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        }
        if (aliases !is List<*>) {
            report("The aliases value of @${KomapperEntityDef::class.simpleName} is invalid.", defDeclaration)
        }
        val entityDeclaration = entity.declaration.accept(ClassDeclarationVisitor(), Unit)
            ?: report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        validateContainerClass(entityDeclaration, defAnnotation)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        return EntityDefinitionSource(defDeclaration, entityDeclaration, aliases.map { it.toString() }, stubAnnotation)
    }
}

internal class SelfDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val entityDeclaration = symbol.accept(ClassDeclarationVisitor(), Unit)
            ?: report("@${KomapperEntity::class.simpleName} cannot be applied to this element.", symbol)
        val annotation = entityDeclaration.findAnnotation(KomapperEntity::class)
        val aliases = annotation?.findValue("aliases")
        if (aliases !is List<*>) {
            report("The aliases value of @${KomapperEntity::class.simpleName} is invalid.", entityDeclaration)
        }
        validateContainerClass(entityDeclaration, entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        return EntityDefinitionSource(
            entityDeclaration,
            entityDeclaration,
            aliases.map { it.toString() },
            stubAnnotation
        )
    }
}
