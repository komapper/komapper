package org.komapper.ksp

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import java.io.PrintWriter

class EntityProcessor : SymbolProcessor {
    private lateinit var codeGenerator: CodeGenerator
    private lateinit var logger: KSPLogger
    var invoked = false

    override fun init(
        options: Map<String, String>,
        kotlinVersion: KotlinVersion,
        codeGenerator: CodeGenerator,
        logger: KSPLogger
    ) {
        this.codeGenerator = codeGenerator
        this.logger = logger
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (invoked) {
            return emptyList()
        }
        val symbols = resolver.getSymbolsWithAnnotation("org.komapper.core.KmEntity")
        for (symbol in symbols) {
            val result = symbol.accept(EntityVisitor(), Unit)
            if (result is EntityVisitResult.Failure) {
                val exit = result.exit
                // log before continue
                logger.error(exit.message, exit.node)
            }
            if (result.declaration.isPrivate()) {
                continue
            }
            val packageName = result.declaration.packageName.asString()
            val entityQualifiedName = result.declaration.qualifiedName?.asString() ?: ""
            val entityTypeName = entityQualifiedName.removePrefix("$packageName.")
            val simpleName = entityTypeName.replace(".", "_") + "_"
            val file = result.declaration.containingFile!!
            codeGenerator.createNewFile(Dependencies(false, file), packageName, simpleName).use { out ->
                PrintWriter(out).use {
                    val runnable = when (result) {
                        is EntityVisitResult.Success -> {
                            EntityMetamodelGenerator(
                                result.entity, packageName, entityTypeName, simpleName, it
                            )
                        }
                        is EntityVisitResult.Failure -> {
                            EmptyMetamodelGenerator(
                                result.declaration, packageName, entityTypeName, simpleName, it
                            )
                        }
                    }
                    runnable.run()
                }
            }
        }
        invoked = true
        return emptyList()
    }
}
