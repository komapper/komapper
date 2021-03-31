package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSAnnotated

internal class EntityAnalyzer(
    private val config: Config,
    private val definitionSourceResolver: EntityDefinitionSourceResolver
) {

    fun analyze(symbol: KSAnnotated): EntityAnalyzerResult {
        val definitionSource = try {
            definitionSourceResolver.resolve(symbol)
        } catch (e: Exit) {
            return EntityAnalyzerResult.Error(e)
        }
        return try {
            val entityDef = EntityDefFactory(config, definitionSource).create()
            val entity = EntityFactory(config, entityDef).create()
            val model = EntityModel(config, definitionSource, entity)
            EntityAnalyzerResult.Success(model)
        } catch (e: Exit) {
            when (e.level) {
                Level.FAILURE -> {
                    val model = EntityModel(config, definitionSource)
                    EntityAnalyzerResult.Failure(model, e)
                }
                Level.ERROR ->
                    EntityAnalyzerResult.Error(e)
            }
        }
    }
}

internal sealed class EntityAnalyzerResult {
    data class Success(val model: EntityModel) : EntityAnalyzerResult()
    data class Failure(val model: EntityModel, val exit: Exit) : EntityAnalyzerResult()
    data class Error(val exit: Exit) : EntityAnalyzerResult()
}
