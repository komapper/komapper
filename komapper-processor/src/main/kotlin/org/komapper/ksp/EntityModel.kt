package org.komapper.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.PrintWriter

internal class EntityModel(
    private val config: Config,
    private val definitionSource: EntityDefinitionSource,
    private val entity: Entity? = null,
) {

    fun generateMetamodel(codeGenerator: CodeGenerator) {
        val declaration = definitionSource.entityDeclaration
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
                    val defDeclaration = definitionSource.defDeclaration
                    EntityMetamodelStubGenerator(
                        defDeclaration, declaration, packageName, entityTypeName, simpleName, it
                    )
                }
                runnable.run()
            }
        }
    }
}
