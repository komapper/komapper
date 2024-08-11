package org.komapper.processor.command

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.annotation.KomapperCommand
import org.komapper.core.ThreadSafe
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import org.komapper.processor.Exit
import java.io.PrintWriter

@ThreadSafe
internal class CommandProcessor(
    private val contextFactory: ContextFactory,
) : SymbolProcessor {

    private val annotationClass = KomapperCommand::class

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = contextFactory.create(resolver)
        val analyzer = CommandAnalyzer(context, annotationClass)
        val symbols = resolver.getSymbolsWithAnnotation(annotationClass.qualifiedName!!)
        for (symbol in symbols) {
            when (val result = analyzer.analyze(symbol)) {
                is CommandAnalysisResult.Success ->
                    generateMetamodel(context, result.command)

                is CommandAnalysisResult.Failure -> {
                    log(context, result.exit)
                    generateMetamodel(context, result.command)
                }

                is CommandAnalysisResult.Error ->
                    log(context, result.exit)
            }
        }
        return emptyList()
    }

    private fun log(context: Context, exit: Exit) {
        context.logger.error(exit.report.message, exit.report.node)
    }

    private fun generateMetamodel(context: Context, command: Command) {
        val files = command.classDeclaration.containingFile?.let { listOf(it) } ?: emptyList()
        val dependencies = Dependencies(false, *files.toTypedArray())
        context.codeGenerator.createNewFile(dependencies, command.packageName, command.fileName).use { out ->
            PrintWriter(out).use { writer ->
                CommandCallGenerator(command, writer).run()
            }
        }
    }
}
