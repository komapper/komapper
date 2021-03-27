package org.komapper.ksp

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
    private var invoked = false

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
            val (entity, declaration) = when (result) {
                is EntityVisitorResult.Success ->
                    result.entity to result.entity.declaration
                is EntityVisitorResult.Error -> {
                    logger.error(result.message, result.node)
                    null to result.declaration
                }
                is EntityVisitorResult.Fatal -> {
                    logger.error(result.message, result.node)
                    continue
                }
            }
            val packageName = declaration.packageName.asString()
            val entityQualifiedName = declaration.qualifiedName?.asString() ?: ""
            val entityTypeName = entityQualifiedName.removePrefix("$packageName.")
            val simpleName = entityTypeName.replace(".", "_") + "_"
            val file = declaration.containingFile!!
            codeGenerator.createNewFile(Dependencies(false, file), packageName, simpleName).use { out ->
                PrintWriter(out).use {
                    val runnable = if (entity != null) {
                        EntityMetamodelGenerator(
                            entity, packageName, entityTypeName, simpleName, it
                        )
                    } else {
                        EmptyEntityMetamodelGenerator(
                            declaration, packageName, entityTypeName, simpleName, it
                        )
                    }
                    runnable.run()
                }
            }
        }
        invoked = true
        return emptyList()
    }
}
