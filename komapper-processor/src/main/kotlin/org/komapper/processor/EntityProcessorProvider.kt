package org.komapper.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperProjection
import org.komapper.annotation.KomapperProjectionDef
import org.komapper.core.ThreadSafe

@ThreadSafe
class EntityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = Config.create(environment.options)
        val processingAnnotations = listOf(
            ProcessingAnnotation(
                KomapperEntityDef::class,
                SeparateDefinitionSourceResolver(config),
                true,
            ),
            ProcessingAnnotation(
                KomapperEntity::class,
                SelfDefinitionSourceResolver(config),
                true,
            ),
            ProcessingAnnotation(
                KomapperProjectionDef::class,
                SeparateProjectionDefinitionSourceResolver(config),
                false,
            ),
            ProcessingAnnotation(
                KomapperProjection::class,
                SelfProjectionDefinitionSourceResolver(config),
                false,
            ),
        )
        return EntityProcessor(environment, config, processingAnnotations)
    }
}
