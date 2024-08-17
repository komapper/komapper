package org.komapper.processor.entity

import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.annotation.KomapperEmbeddedId
import org.komapper.annotation.KomapperId
import org.komapper.processor.Context
import org.komapper.processor.Exit
import org.komapper.processor.report

internal class EntityAnalyzer(
    private val context: Context,
    private val definitionSourceResolver: EntityDefinitionSourceResolver,
    private val requiresIdValidation: Boolean,
) {

    fun analyze(symbol: KSAnnotated): EntityAnalysisResult {
        val definitionSource = try {
            definitionSourceResolver.resolve(symbol)
        } catch (e: Exit) {
            return EntityAnalysisResult.Error(e)
        }
        return if (definitionSource == null) {
            EntityAnalysisResult.Skip
        } else {
            try {
                val entityDef = EntityDefFactory(context, definitionSource).create()
                val entity = EntityFactory(context, entityDef).create()
                validateEntity(entity)
                val model = EntityModel(definitionSource, entity)
                EntityAnalysisResult.Success(model)
            } catch (e: Exit) {
                val model = EntityModel(definitionSource)
                EntityAnalysisResult.Failure(model, e)
            }
        }
    }

    private fun validateEntity(entity: Entity) {
        if (entity.declaration.simpleName.asString().startsWith("__")) {
            report("The class name cannot start with '__'.", entity.declaration)
        }
        for (p in entity.properties) {
            val name = (p.declaration.simpleName).asString()
            if (name.startsWith("__")) {
                report("The property name cannot start with '__'.", p.node)
            }
        }
        if (requiresIdValidation) {
            if (entity.embeddedIdProperty == null && entity.idProperties.isEmpty()) {
                report("The entity class must have at least one id property.", entity.declaration)
            }
            if (entity.embeddedIdProperty != null && entity.idProperties.isNotEmpty()) {
                report(
                    "The entity class can have either @${KomapperEmbeddedId::class.simpleName} or @${KomapperId::class.simpleName}.",
                    entity.declaration,
                )
            }
        }
    }
}

internal sealed class EntityAnalysisResult {
    data class Success(val model: EntityModel) : EntityAnalysisResult()
    data class Failure(val model: EntityModel, val exit: Exit) : EntityAnalysisResult()
    data class Error(val exit: Exit) : EntityAnalysisResult()
    object Skip : EntityAnalysisResult()
}
