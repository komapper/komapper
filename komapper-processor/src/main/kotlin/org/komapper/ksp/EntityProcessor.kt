package org.komapper.ksp

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import java.io.PrintWriter

internal class EntityProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val config: Config = Config.create(environment.options)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val pairs = listOf(
            KomapperEntityDef::class.qualifiedName!! to SeparateDefinitionSourceResolver(),
            KomapperEntity::class.qualifiedName!! to SelfDefinitionSourceResolver()
        )
        for ((annotation, definitionSourceResolver) in pairs) {
            val symbols = resolver.getSymbolsWithAnnotation(annotation)
            val analyzer = EntityAnalyzer(config, definitionSourceResolver)
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
        environment.logger.error(exit.report.message, exit.report.node)
    }

    private fun generateMetamodel(model: EntityModel) {
        fun createSimpleName(): String {
            val defDeclaration = model.definitionSource.defDeclaration
            val packageName = defDeclaration.packageName.asString()
            val qualifiedName = defDeclaration.qualifiedName?.asString() ?: ""
            val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
            return config.prefix + packageRemovedQualifiedName.replace(".", "_") + config.suffix
        }

        val declaration = model.definitionSource.entityDeclaration
        val packageName = declaration.packageName.asString()
        val entityQualifiedName = declaration.qualifiedName?.asString() ?: ""
        val entityTypeName = entityQualifiedName.removePrefix("$packageName.")
        val simpleName = createSimpleName()
        val file = declaration.containingFile!!
        environment.codeGenerator.createNewFile(Dependencies(false, file), packageName, simpleName).use { out ->
            PrintWriter(out).use {
                val runnable = if (model.entity != null) {
                    EntityMetamodelGenerator(
                        model.entity, packageName, entityTypeName, simpleName, it
                    )
                } else {
                    val defDeclaration = model.definitionSource.defDeclaration
                    EntityMetamodelStubGenerator(
                        defDeclaration, declaration, packageName, entityTypeName, simpleName, it
                    )
                }
                runnable.run()
            }
        }
    }
}
