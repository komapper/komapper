package org.komapper.processor

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.komapper.processor.Symbols.EntityMetamodelFactory
import org.komapper.processor.Symbols.EntityMetamodelFactorySpi
import java.io.PrintWriter

internal class EntityMetamodelFactoryProcessor(
    private val environment: SymbolProcessorEnvironment,
) :
    SymbolProcessor {

    private val classDeclarations: MutableList<KSClassDeclaration> = mutableListOf()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(EntityMetamodelFactory)
        val declarations = symbols.map { it.accept(ClassDeclarationVisitor(), Unit) }.filterNotNull().toList()
        classDeclarations.addAll(declarations)
        return emptyList()
    }

    override fun finish() {
        val files = classDeclarations.mapNotNull { it.containingFile }
        val dependencies = Dependencies(true, *files.toTypedArray())
        environment.codeGenerator.createNewFile(
            dependencies,
            "META-INF/services",
            EntityMetamodelFactorySpi,
            "",
        ).use { out ->
            PrintWriter(out).use { writer ->
                for (classDeclaration in classDeclarations) {
                    val name = classDeclaration.qualifiedName?.asString()
                    if (name != null) {
                        writer.println(name)
                    }
                }
            }
        }
    }
}
