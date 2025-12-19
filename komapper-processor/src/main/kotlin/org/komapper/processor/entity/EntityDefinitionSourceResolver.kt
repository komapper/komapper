package org.komapper.processor.entity

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperProjection
import org.komapper.annotation.KomapperProjectionDef
import org.komapper.processor.Context
import org.komapper.processor.Symbols.KomapperStub
import org.komapper.processor.Symbols.ProjectionMeta
import org.komapper.processor.Symbols.Void
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue
import org.komapper.processor.report
import org.komapper.processor.validateContainerClass

internal interface EntityDefinitionSourceResolver {
    fun resolve(symbol: KSAnnotated): EntityDefinitionSource?
}

internal class SeparateDefinitionSourceResolver(private val context: Context) : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSAnnotated): EntityDefinitionSource {
        val defDeclaration = symbol as? KSClassDeclaration
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
        val unitTypeName = createUnitTypeName(context, unitDeclaration)
        val entityDeclaration = entity.declaration as? KSClassDeclaration
            ?: report("The entity value of @${KomapperEntityDef::class.simpleName} is not found.", defDeclaration)
        validateContainerClass(entityDeclaration, defAnnotation)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(context, defDeclaration)
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

internal class SelfDefinitionSourceResolver(private val context: Context) : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSAnnotated): EntityDefinitionSource {
        val entityDeclaration = symbol as? KSClassDeclaration
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
        val unitTypeName = createUnitTypeName(context, unitDeclaration)
        validateContainerClass(entityDeclaration, entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(context, entityDeclaration)
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

internal class SeparateProjectionDefinitionSourceResolver(private val context: Context) : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSAnnotated): EntityDefinitionSource {
        val defDeclaration = symbol as? KSClassDeclaration
            ?: report("@${KomapperProjectionDef::class.simpleName} cannot be applied to this element.", symbol)

        val defAnnotation = defDeclaration.findAnnotation(KomapperProjectionDef::class)
        val projectionType = defAnnotation?.findValue("projection")
        if (projectionType !is KSType) {
            report("The projection value of @${KomapperProjectionDef::class.simpleName} is not found.", defDeclaration)
        }
        val unitName = context.resolver.getKSNameFromString(ProjectionMeta)
        val unitDeclaration = context.resolver.getClassDeclarationByName(unitName) ?: error("$ProjectionMeta not found.")
        val unitTypeName = createUnitTypeName(context, unitDeclaration)
        val entityDeclaration = projectionType.declaration as? KSClassDeclaration
            ?: report("The projection value of @${KomapperProjectionDef::class.simpleName} is not found.", defDeclaration)
        validateContainerClass(entityDeclaration, defAnnotation)
        val projection = AnnotationSupport.createProjection(defAnnotation, entityDeclaration)
        val stubAnnotation = defDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(context, defDeclaration)
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

internal class SelfProjectionDefinitionSourceResolver(private val context: Context) : EntityDefinitionSourceResolver {
    override fun resolve(symbol: KSAnnotated): EntityDefinitionSource {
        val entityDeclaration = symbol as? KSClassDeclaration
            ?: report("@${KomapperProjection::class.simpleName} cannot be applied to this element.", symbol)

        val unitName = context.resolver.getKSNameFromString(ProjectionMeta)
        val unitDeclaration = context.resolver.getClassDeclarationByName(unitName) ?: error("$ProjectionMeta not found.")
        val unitTypeName = createUnitTypeName(context, unitDeclaration)
        validateContainerClass(entityDeclaration, entityDeclaration)
        val annotation = symbol.findAnnotation(KomapperProjection::class)
        val projection = AnnotationSupport.createProjection(annotation, entityDeclaration)
        val stubAnnotation = entityDeclaration.findAnnotation(KomapperStub)
        val (packageName, simpleName) = createMetamodelClassName(context, entityDeclaration)
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
        is KSType -> (symbol.declaration as? KSClassDeclaration)?.let {
            when {
                it.qualifiedName?.asString() == Void -> null
                it.classKind == ClassKind.OBJECT -> it
                else -> errorHandler()
            }
        }

        else -> null
    }
}

private fun createMetamodelClassName(context: Context, defDeclaration: KSClassDeclaration): Pair<String, String> {
    val packageName = defDeclaration.packageName.asString()
    val qualifiedName = defDeclaration.qualifiedName?.asString() ?: ""
    val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
    val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
    return packageName to simpleName
}

private fun createUnitTypeName(context: Context, unitDeclaration: KSClassDeclaration?): String {
    return unitDeclaration?.qualifiedName?.asString() ?: context.config.metaObject
}
