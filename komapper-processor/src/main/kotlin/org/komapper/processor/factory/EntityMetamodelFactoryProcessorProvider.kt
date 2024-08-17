package org.komapper.processor.factory

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.core.ThreadSafe
import org.komapper.processor.Config

@ThreadSafe
class EntityMetamodelFactoryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = Config.create(environment.options)
        return if (config.enableEntityMetamodelListing) {
            EntityMetamodelFactoryProcessor(environment)
        } else {
            EmptySymbolProcessor
        }
    }
}

internal object EmptySymbolProcessor : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> = emptyList()
}
