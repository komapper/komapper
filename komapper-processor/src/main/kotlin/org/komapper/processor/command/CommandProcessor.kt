package org.komapper.processor.command

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.annotation.KomapperCommand
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import org.komapper.processor.Exit
import java.io.PrintWriter

internal class CommandProcessor(
    private val contextFactory: ContextFactory,
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = contextFactory.create(resolver)
        val annotationName = KomapperCommand::class.qualifiedName!!
        val analyzer = CommandAnalyzer(context)
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        for (symbol in symbols) {
            analyzer.analyze(symbol).let { result ->
                when (result) {
                    is CommandAnalysisResult.Success -> {
                        generateMetamodel(context, result.model)
                    }

                    is CommandAnalysisResult.Failure -> {
                        log(context, result.exit)
                        generateMetamodel(context, result.model)
                    }

                    is CommandAnalysisResult.Error -> {
                        log(context, result.exit)
                    }

                    is CommandAnalysisResult.Skip -> {
                    }
                }
            }
        }
        return emptyList()
    }

    private fun log(context: Context, exit: Exit) {
        context.logger.error(exit.report.message, exit.report.node)
    }

    private fun generateMetamodel(context: Context, model: CommandModel) {
        val dependencies = Dependencies(false, *model.containingFiles.toTypedArray())
        val (packageName, fileName) = model.createFileName()
        context.codeGenerator.createNewFile(dependencies, packageName, fileName).use { out ->
            PrintWriter(out).use { writer ->
                CommandCallGenerator(
                    context,
                    model.command,
                    packageName,
                    writer,
                ).run()
            }
        }
    }
}
