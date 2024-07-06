package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

internal data class Context(
    private val environment: SymbolProcessorEnvironment,
    val config: Config,
    val resolver: Resolver,
) {
    val logger = KomapperKSPLogger(environment.logger)
    val codeGenerator get() = environment.codeGenerator
}
