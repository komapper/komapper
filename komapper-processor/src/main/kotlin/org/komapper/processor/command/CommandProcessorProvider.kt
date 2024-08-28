package org.komapper.processor.command

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import org.komapper.core.ThreadSafe
import org.komapper.processor.Config
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory

@ThreadSafe
internal class CommandProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val config = Config.create(environment.options)
        val factory = ContextFactory { resolver -> Context(environment, config, resolver) }
        return CommandProcessor(factory)
    }
}
