package org.komapper.processor.entity

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperProjection
import org.komapper.annotation.KomapperProjectionDef
import org.komapper.core.ThreadSafe
import org.komapper.processor.Config
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory

@ThreadSafe
class EntityProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = Config.create(environment.options)
        val factory = ContextFactory { resolver -> Context(environment, config, resolver) }
        val processingAnnotations = listOf(
            ProcessingAnnotation(
                KomapperEntityDef::class,
                ::SeparateDefinitionSourceResolver,
                true,
            ),
            ProcessingAnnotation(
                KomapperEntity::class,
                ::SelfDefinitionSourceResolver,
                true,
            ),
            ProcessingAnnotation(
                KomapperProjectionDef::class,
                ::SeparateProjectionDefinitionSourceResolver,
                false,
            ),
            ProcessingAnnotation(
                KomapperProjection::class,
                ::SelfProjectionDefinitionSourceResolver,
                false,
            ),
        )
        return EntityProcessor(factory, processingAnnotations)
    }
}
