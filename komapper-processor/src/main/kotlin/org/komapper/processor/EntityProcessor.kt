package org.komapper.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import java.io.PrintWriter

internal class EntityProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val logger: KSPLogger = environment.logger
    private val config: Config = Config.create(environment.options)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val pairs = listOf(
            KomapperEntityDef::class.qualifiedName!! to SeparateDefinitionSourceResolver(),
            KomapperEntity::class.qualifiedName!! to SelfDefinitionSourceResolver(),
        )
        for ((annotation, definitionSourceResolver) in pairs) {
            val symbols = resolver.getSymbolsWithAnnotation(annotation)
            val analyzer = EntityAnalyzer(logger, config, definitionSourceResolver)
            for (symbol in symbols) {
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
                }
                generateMetamodel(model)
            }
        }
        return emptyList()
    }

    private fun log(exit: Exit) {
        logger.error(exit.report.message, exit.report.node)
    }

    private fun generateMetamodel(model: EntityModel) {
        val dependencies = Dependencies(false, *model.containingFiles.toTypedArray())
        val (packageName, simpleName) = model.createMetamodelClassName(config.prefix, config.suffix)
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
