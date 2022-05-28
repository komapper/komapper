package org.komapper.processor

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
        fun createClassName(): Pair<String, String> {
            val defDeclaration = model.definitionSource.defDeclaration
            val packageName = defDeclaration.packageName.asString()
            val qualifiedName = defDeclaration.qualifiedName?.asString() ?: ""
            val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
            val simpleName = config.prefix + packageRemovedQualifiedName.replace(".", "_") + config.suffix
            return packageName to simpleName
        }

        val declaration = model.definitionSource.entityDeclaration
        val aliases = model.definitionSource.aliases.ifEmpty {
            val alias = toCamelCase(declaration.simpleName.asString())
            listOf(alias)
        }
        val (packageName, simpleName) = createClassName()
        val dependencies = Dependencies(false, *model.containingFiles.toTypedArray())
        environment.codeGenerator.createNewFile(dependencies, packageName, simpleName).use { out ->
            PrintWriter(out).use {
                val entityTypeName = model.definitionSource.entityDeclaration.qualifiedName?.asString() ?: ""
                val runnable = if (model.definitionSource.stubAnnotation != null || model.entity == null) {
                    EntityMetamodelStubGenerator(
                        declaration, config.metaObject, aliases, packageName, simpleName, entityTypeName, it
                    )
                } else {
                    EntityMetamodelGenerator(
                        model.entity, config.metaObject, aliases, packageName, simpleName, entityTypeName, it
                    )
                }
                runnable.run()
            }
        }
    }
}
