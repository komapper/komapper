package org.komapper.processor

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.processor.Symbols.DefaultUnit
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
        val unit = defAnnotation?.findValue("unit")
        if (entity !is KSType) {
            report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        }
        if (aliases !is List<*>) {
            report("The aliases value of @${KomapperEntityDef::class.simpleName} is invalid.", defDeclaration)
        }
        val unitDeclaration = toUnitDeclaration(unit) {
            report(
                "The unit value of @${KomapperEntityDef::class.simpleName} must be an object.",
                defDeclaration
            )
        }
        val entityDeclaration = entity.declaration.accept(ClassDeclarationVisitor(), Unit)
            ?: report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        validateContainerClass(entityDeclaration, defAnnotation)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        return EntityDefinitionSource(
            defDeclaration = defDeclaration,
            entityDeclaration = entityDeclaration,
            aliases = aliases.map { it.toString() },
            unitDeclaration = unitDeclaration,
            stubAnnotation = stubAnnotation
        )
    }
}

internal class SelfDefinitionSourceResolver : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSNode): EntityDefinitionSource {
        val entityDeclaration = symbol.accept(ClassDeclarationVisitor(), Unit)
            ?: report("@${KomapperEntity::class.simpleName} cannot be applied to this element.", symbol)
        val annotation = entityDeclaration.findAnnotation(KomapperEntity::class)
        val aliases = annotation?.findValue("aliases")
        val unit = annotation?.findValue("unit")
        if (aliases !is List<*>) {
            report("The aliases value of @${KomapperEntity::class.simpleName} is invalid.", entityDeclaration)
        }
        val unitDeclaration = toUnitDeclaration(unit) {
            report(
                "The unit value of @${KomapperEntity::class.simpleName} must be an object.",
                entityDeclaration
            )
        }
        validateContainerClass(entityDeclaration, entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        return EntityDefinitionSource(
            defDeclaration = entityDeclaration,
            entityDeclaration = entityDeclaration,
            aliases = aliases.map { it.toString() },
            unitDeclaration = unitDeclaration,
            stubAnnotation = stubAnnotation
        )
    }
}

private fun toUnitDeclaration(symbol: Any?, errorHandler: () -> Nothing): KSClassDeclaration? {
    return when (symbol) {
        is KSType -> symbol.declaration.accept(ClassDeclarationVisitor(), Unit)?.let {
            when {
                it.qualifiedName?.asString() == DefaultUnit -> null
                it.classKind == ClassKind.OBJECT -> it
                else -> errorHandler()
            }
        }
        else -> null
    }
}
