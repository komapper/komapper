package org.komapper.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.PrintWriter

internal class EntityProcessor(
    private val environment: SymbolProcessorEnvironment,
    private val config: Config,
    private val processingAnnotations: List<ProcessingAnnotation>,
) : SymbolProcessor {

    private val logger: KSPLogger = environment.logger
    private val processedSymbols = mutableSetOf<KSAnnotated>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        for (annotation in processingAnnotations) {
            val annotationName = annotation.annotationClass.qualifiedName!!
            val symbols = resolver.getSymbolsWithAnnotation(annotationName)
            val analyzer = EntityAnalyzer(logger, resolver, config, annotation.definitionSourceResolver, annotation.requiresIdValidation)
            for (symbol in (symbols - processedSymbols)) {
                val model = when (val result = analyzer.analyze(symbol)) {
                    is EntityAnalysisResult.Success -> result.model
                    is EntityAnalysisResult.Failure -> {
                        log(result.exit)
                        result.model
                    }

                    is EntityAnalysisResult.Error -> {
                        log(result.exit)
                        continue
                    }

                    is EntityAnalysisResult.Skip -> {
                        continue
                    }
                }
                generateMetamodel(model)
            }
            processedSymbols.addAll(symbols)
        }
        return emptyList()
    }

    private fun log(exit: Exit) {
        logger.error(exit.report.message, exit.report.node)
    }

    private fun generateMetamodel(model: EntityModel) {
        val dependencies = Dependencies(false, *model.containingFiles.toTypedArray())
        val (packageName, simpleName) = model.createMetamodelClassName()
        environment.codeGenerator.createNewFile(dependencies, packageName, simpleName).use { out ->
            PrintWriter(out).use { writer ->
                val runnable = if (model.hasStubAnnotation || model.entity == null) {
                    EntityMetamodelStubGenerator(
                        logger,
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
                        logger,
                        config,
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
