package org.komapper.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.komapper.core.ThreadSafe

@ThreadSafe
class EntityMetamodelFactoryProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = Config.create(environment.options)
        return if (config.enableEntityMetamodelListing) {
            EntityMetamodelFactoryProcessor(environment, config)
        } else {
            EmptySymbolProcessor
        }
    }
}
