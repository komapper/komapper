package org.komapper.processor

import com.google.devtools.ksp.symbol.KSFile

internal class EntityModel(
    private val definitionSource: EntityDefinitionSource,
    val entity: Entity? = null,
) {
    val hasStubAnnotation: Boolean = definitionSource.stubAnnotation != null

    val entityDeclaration = definitionSource.entityDeclaration

    val aliases = definitionSource.aliases.ifEmpty {
        val alias = toCamelCase(definitionSource.entityDeclaration.simpleName.asString())
        listOf(alias)
    }

    val typeName = definitionSource.entityDeclaration.qualifiedName?.asString() ?: ""

    val containingFiles: List<KSFile>
        get() {
            val defDeclaration = definitionSource.defDeclaration
            val entityDeclaration = definitionSource.entityDeclaration
            return if (defDeclaration == entityDeclaration) {
                listOfNotNull(defDeclaration.containingFile)
            } else {
                listOfNotNull(defDeclaration.containingFile, entityDeclaration.containingFile)
            }
        }

    fun createMetamodelClassName(prefix: String, suffix: String): Pair<String, String> {
        val defDeclaration = definitionSource.defDeclaration
        val packageName = defDeclaration.packageName.asString()
        val qualifiedName = defDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = prefix + packageRemovedQualifiedName.replace(".", "_") + suffix
        return packageName to simpleName
    }
}
