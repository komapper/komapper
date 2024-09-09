package org.komapper.processor.command

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.annotation.KomapperPartial
import org.komapper.core.ThreadSafe
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import org.komapper.processor.Exit

@ThreadSafe
internal class PartialProcessor(
    private val contextFactory: ContextFactory,
) : SymbolProcessor {

    private val annotationClass = KomapperPartial::class

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = contextFactory.create(resolver)
        val analyzer = PartialAnalyzer(context, annotationClass)
        val symbols = resolver.getSymbolsWithAnnotation(annotationClass.qualifiedName!!)
        for (symbol in symbols) {
            when (val result = analyzer.analyze(symbol)) {
                is PartialAnalysisResult.Success -> {}

                is PartialAnalysisResult.Failure -> {
                    log(context, result.exit)
                }

                is PartialAnalysisResult.Error ->
                    log(context, result.exit)
            }
        }
        return emptyList()
    }

    private fun log(context: Context, exit: Exit) {
        context.logger.error(exit.report.message, exit.report.node)
    }
}
