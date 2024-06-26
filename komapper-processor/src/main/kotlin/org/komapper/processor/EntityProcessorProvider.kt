package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver
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
        val factory = object : ContextFactory {
            override fun create(resolver: Resolver): Context {
                return Context(environment, config, resolver)
            }
        }
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
