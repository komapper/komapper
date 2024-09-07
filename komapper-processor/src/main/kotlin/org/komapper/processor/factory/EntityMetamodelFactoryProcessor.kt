package org.komapper.processor.factory

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import org.komapper.processor.Symbols.EntityMetamodelFactory
import org.komapper.processor.Symbols.EntityMetamodelFactorySpi
import java.io.PrintWriter

internal class EntityMetamodelFactoryProcessor(
    private val environment: SymbolProcessorEnvironment,
) :
    SymbolProcessor {

    private val fileMap: MutableMap<String, KSFile> = mutableMapOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        environment.logger.warn("Running org.komapper.processor.factory.EntityMetamodelFactoryProcessor")

        val symbols = resolver.getSymbolsWithAnnotation(EntityMetamodelFactory)
        
        environment.logger.warn("Symbols: ${symbols.toList()}")
        
        val declarations = symbols.map { it as? KSClassDeclaration }.filterNotNull().toList()
        declarations
            .map { it.qualifiedName to it.containingFile }
            .filter { it.first != null && it.second != null }
            .associateTo(fileMap) { it.first!!.asString() to it.second!! }
        return emptyList()
    }

    override fun finish() {
        val dependencies = Dependencies(true, *fileMap.values.toTypedArray())
        environment.codeGenerator.createNewFile(
            dependencies,
            "META-INF/services",
            EntityMetamodelFactorySpi,
            "",
        ).use { out ->
            PrintWriter(out).use { writer ->
                for (name in fileMap.keys) {
                    writer.println(name)
                }
            }
        }
    }
}
