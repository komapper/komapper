package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSAnnotated

internal class EntityAnalyzer(
    private val config: Config,
    private val definitionSourceResolver: EntityDefinitionSourceResolver
) {

    fun analyze(symbol: KSAnnotated): EntityAnalysisResult {
        val definitionSource = try {
            definitionSourceResolver.resolve(symbol)
        } catch (e: Exit) {
            return EntityAnalysisResult.Error(e)
        }
        return try {
            val entityDef = EntityDefFactory(config, definitionSource).create()
            val entity = EntityFactory(config, entityDef).create()
            val model = EntityModel(config, definitionSource, entity)
            EntityAnalysisResult.Success(model)
        } catch (e: Exit) {
            val model = EntityModel(config, definitionSource)
            EntityAnalysisResult.Failure(model, e)
        }
    }
}

internal sealed class EntityAnalysisResult {
    data class Success(val model: EntityModel) : EntityAnalysisResult()
    data class Failure(val model: EntityModel, val exit: Exit) : EntityAnalysisResult()
    data class Error(val exit: Exit) : EntityAnalysisResult()
}
