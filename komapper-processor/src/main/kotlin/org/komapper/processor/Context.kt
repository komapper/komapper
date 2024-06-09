package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

internal data class Context(
    val environment: SymbolProcessorEnvironment,
    val config: Config,
    val resolver: Resolver,
) {
    val logger get() = environment.logger
    val codeGenerator get() = environment.codeGenerator
}
