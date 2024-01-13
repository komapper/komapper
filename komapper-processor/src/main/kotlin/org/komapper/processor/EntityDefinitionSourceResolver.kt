package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperProjection
import org.komapper.annotation.KomapperProjectionDef
import org.komapper.processor.Symbols.KomapperStub
import org.komapper.processor.Symbols.ProjectionMeta
import org.komapper.processor.Symbols.Void

internal interface EntityDefinitionSourceResolver {
    fun resolve(resolver: Resolver, symbol: KSAnnotated): EntityDefinitionSource?
}

internal class SeparateDefinitionSourceResolver(val config: Config) : EntityDefinitionSourceResolver {
    override fun resolve(resolver: Resolver, symbol: KSAnnotated): EntityDefinitionSource {
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
                defDeclaration,
            )
        }
        val unitTypeName = createUnitTypeName(config, unitDeclaration)
        val entityDeclaration = entity.declaration.accept(ClassDeclarationVisitor(), Unit)
            ?: report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        validateContainerClass(entityDeclaration, defAnnotation)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(config, defDeclaration)
        return EntityDefinitionSource(
            defDeclaration = defDeclaration,
            entityDeclaration = entityDeclaration,
            packageName = packageName,
            metamodelSimpleName = simpleName,
            aliases = aliases.map { it.toString() },
            unitDeclaration = unitDeclaration,
            unitTypeName = unitTypeName,
            projection = null,
            stubAnnotation = stubAnnotation,
        )
    }
}

internal class SelfDefinitionSourceResolver(val config: Config) : EntityDefinitionSourceResolver {
    override fun resolve(resolver: Resolver, symbol: KSAnnotated): EntityDefinitionSource {
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
                entityDeclaration,
            )
        }
        val unitTypeName = createUnitTypeName(config, unitDeclaration)
        validateContainerClass(entityDeclaration, entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(config, entityDeclaration)
        return EntityDefinitionSource(
            defDeclaration = entityDeclaration,
            entityDeclaration = entityDeclaration,
            packageName = packageName,
            metamodelSimpleName = simpleName,
            aliases = aliases.map { it.toString() },
            unitDeclaration = unitDeclaration,
            unitTypeName = unitTypeName,
            projection = null,
            stubAnnotation = stubAnnotation,
        )
    }
}

internal class SeparateProjectionDefinitionSourceResolver(val config: Config) : EntityDefinitionSourceResolver {

    override fun resolve(resolver: Resolver, symbol: KSAnnotated): EntityDefinitionSource? {
        val defDeclaration = symbol.accept(ClassDeclarationVisitor(), Unit)
            ?: report("@${KomapperProjectionDef::class.simpleName} cannot be applied to this element.", symbol)

        val defAnnotation = defDeclaration.findAnnotation(KomapperProjectionDef::class)
        val projectionType = defAnnotation?.findValue("projection")
        if (projectionType !is KSType) {
            report("The projection value of @${KomapperProjectionDef::class.simpleName} is not found.", defDeclaration)
        }
        val unitName = resolver.getKSNameFromString(ProjectionMeta)
        val unitDeclaration = resolver.getClassDeclarationByName(unitName) ?: error("$ProjectionMeta not found.")
        val unitTypeName = createUnitTypeName(config, unitDeclaration)
        val entityDeclaration = projectionType.declaration.accept(ClassDeclarationVisitor(), Unit)
            ?: report("The projection value of @${KomapperProjectionDef::class.simpleName} is not found.", defDeclaration)
        validateContainerClass(entityDeclaration, defAnnotation)
        val projection = AnnotationSupport.createProjection(defAnnotation, entityDeclaration)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(config, defDeclaration)
        return EntityDefinitionSource(
            defDeclaration = defDeclaration,
            entityDeclaration = entityDeclaration,
            packageName = packageName,
            metamodelSimpleName = simpleName,
            aliases = emptyList(),
            unitDeclaration = unitDeclaration,
            unitTypeName = unitTypeName,
            projection = projection,
            stubAnnotation = stubAnnotation,
        )
    }
}

internal class SelfProjectionDefinitionSourceResolver(val config: Config) : EntityDefinitionSourceResolver {
    override fun resolve(resolver: Resolver, symbol: KSAnnotated): EntityDefinitionSource? {
        val entityDeclaration = symbol.accept(ClassDeclarationVisitor(), Unit)
            ?: report("@${KomapperProjection::class.simpleName} cannot be applied to this element.", symbol)

        val unitName = resolver.getKSNameFromString(ProjectionMeta)
        val unitDeclaration = resolver.getClassDeclarationByName(unitName) ?: error("$ProjectionMeta not found.")
        val unitTypeName = createUnitTypeName(config, unitDeclaration)
        validateContainerClass(entityDeclaration, entityDeclaration)
        val annotation = symbol.findAnnotation(KomapperProjection::class)
        val projection = AnnotationSupport.createProjection(annotation, entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(config, entityDeclaration)
        return EntityDefinitionSource(
            defDeclaration = entityDeclaration,
            entityDeclaration = entityDeclaration,
            packageName = packageName,
            metamodelSimpleName = simpleName,
            aliases = emptyList(),
            unitDeclaration = unitDeclaration,
            unitTypeName = unitTypeName,
            projection = projection,
            stubAnnotation = stubAnnotation,
        )
    }
}

private fun toUnitDeclaration(symbol: Any?, errorHandler: () -> Nothing): KSClassDeclaration? {
    return when (symbol) {
        is KSType -> symbol.declaration.accept(ClassDeclarationVisitor(), Unit)?.let {
            when {
                it.qualifiedName?.asString() == Void -> null
                it.classKind == ClassKind.OBJECT -> it
                else -> errorHandler()
            }
        }
        else -> null
    }
}

private fun createMetamodelClassName(config: Config, defDeclaration: KSClassDeclaration): Pair<String, String> {
    val packageName = defDeclaration.packageName.asString()
    val qualifiedName = defDeclaration.qualifiedName?.asString() ?: ""
    val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
    val simpleName = config.prefix + packageRemovedQualifiedName.replace(".", "_") + config.suffix
    return packageName to simpleName
}

private fun createUnitTypeName(config: Config, unitDeclaration: KSClassDeclaration?): String {
    return unitDeclaration?.qualifiedName?.asString() ?: config.metaObject
}
