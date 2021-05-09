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
        val simpleName = createSimpleName()
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

    private fun createSimpleName(): String {
        val defDeclaration = definitionSource.defDeclaration
        val packageName = defDeclaration.packageName.asString()
        val qualifiedName = defDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        return config.prefix + packageRemovedQualifiedName.replace(".", "_") + config.suffix
    }
}
