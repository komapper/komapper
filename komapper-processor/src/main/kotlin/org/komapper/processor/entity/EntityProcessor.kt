package org.komapper.processor.entity

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import org.komapper.processor.Exit
import java.io.PrintWriter

internal class EntityProcessor(
    private val contextFactory: ContextFactory,
    private val processingAnnotations: List<ProcessingAnnotation>,
) : SymbolProcessor {

    private val processedSymbols = mutableSetOf<KSAnnotated>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val context = contextFactory.create(resolver)
        for (annotation in processingAnnotations) {
            val annotationName = annotation.annotationClass.qualifiedName!!
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            val analyzer = EntityAnalyzer(
                context,
                annotation.createEntityDefinitionSourceResolver(context),
                annotation.requiresIdValidation,
            )
            for (symbol in (symbols - processedSymbols)) {
                val model = when (val result = analyzer.analyze(symbol)) {
                    is EntityAnalysisResult.Success -> result.model
                    is EntityAnalysisResult.Failure -> {
                        log(context, result.exit)
                        result.model
                    }

                    is EntityAnalysisResult.Error -> {
                        log(context, result.exit)
                        continue
                    }

                    is EntityAnalysisResult.Skip -> {
                        continue
                    }
                }
                generateMetamodel(context, model)
            }
            processedSymbols.addAll(symbols)
        }
        return emptyList()
    }

    private fun log(context: Context, exit: Exit) {
        context.logger.error(exit.report.message, exit.report.node)
    }

    private fun generateMetamodel(context: Context, model: EntityModel) {
        val dependencies = Dependencies(false, *model.containingFiles.toTypedArray())
        val (packageName, simpleName) = model.createMetamodelClassName()
        context.codeGenerator.createNewFile(dependencies, packageName, simpleName).use { out ->
            PrintWriter(out).use { writer ->
                val runnable = if (model.hasStubAnnotation || model.entity == null) {
                    EntityMetamodelStubGenerator(
                        context,
                        model.entityDeclaration,
                        model.unitTypeName,
                        model.aliases,
                        packageName,
                        simpleName,
                        model.typeName,
                        writer,
                    )
                } else {
                    EntityMetamodelGenerator(
                        context,
                        model.entity,
                        model.unitTypeName,
                        model.aliases,
                        packageName,
                        simpleName,
                        model.typeName,
                        writer,
                    )
                }
                runnable.run()
            }
        }
    }
}
