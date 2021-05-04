package org.komapper.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated

internal class EntityProcessor(
    options: Map<String, String>,
    @Suppress("unused")
    private val kotlinVersion: KotlinVersion,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private var config: Config = Config.create(options)
    private var invoked = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        val pairs = listOf(
            "org.komapper.annotation.KmEntityDef" to SeparateDefinitionSourceResolver(),
            "org.komapper.annotation.KmEntity" to SelfDefinitionSourceResolver()
        )
        for ((annotation, definitionSourceResolver) in pairs) {
            val symbols = resolver.getSymbolsWithAnnotation(annotation)
            val analyzer = EntityAnalyzer(config, definitionSourceResolver)
            for (symbol in symbols) {
                val model = when (val result = analyzer.analyze(symbol)) {
                    is EntityAnalyzerResult.Success -> result.model
                    is EntityAnalyzerResult.Failure -> {
                        log(result.exit)
                        result.model
                    }
                    is EntityAnalyzerResult.Error -> {
                        log(result.exit)
                        continue
                    }
                }
                model.generateMetamodel(codeGenerator)
            }
        }
        invoked = true
        return emptyList()
    }

    private fun log(exit: Exit) {
        logger.error(exit.report.message, exit.report.node)
    }
}
