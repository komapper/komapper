package org.komapper.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.PrintWriter

internal class EntityModel(
    private val config: Config,
    definitionSource: EntityDefinitionSource,
    private val entity: Entity? = null,
) {

    private val declaration = definitionSource.entityDeclaration

    fun generateMetamodel(codeGenerator: CodeGenerator) {
        val packageName = declaration.packageName.asString()
        val entityQualifiedName = declaration.qualifiedName?.asString() ?: ""
        val entityTypeName = entityQualifiedName.removePrefix("$packageName.")
        val simpleName = config.prefix + entityTypeName.replace(".", "_") + config.suffix
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
}
