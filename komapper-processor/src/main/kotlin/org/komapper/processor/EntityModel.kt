package org.komapper.processor

import com.google.devtools.ksp.symbol.KSFile

internal class EntityModel(
    private val definitionSource: EntityDefinitionSource,
    val entity: Entity? = null,
) {
    val hasStubAnnotation: Boolean = definitionSource.stubAnnotation != null
    val entityDeclaration = definitionSource.entityDeclaration
    val aliases = definitionSource.names
    val typeName = definitionSource.typeName
    val unitTypeName = definitionSource.unitTypeName

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

    fun createMetamodelClassName(): Pair<String, String> {
        return definitionSource.packageName to definitionSource.metamodelSimpleName
    }
}
