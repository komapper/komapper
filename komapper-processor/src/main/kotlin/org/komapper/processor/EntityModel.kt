package org.komapper.processor

import com.google.devtools.ksp.symbol.KSFile

internal class EntityModel(
    val definitionSource: EntityDefinitionSource,
    val entity: Entity? = null,
) {
    val containingFiles: List<KSFile> get() {
        val defDeclaration = definitionSource.defDeclaration
        val entityDeclaration = definitionSource.entityDeclaration
        return if (defDeclaration == entityDeclaration) {
            listOfNotNull(defDeclaration.containingFile)
        } else {
            listOfNotNull(defDeclaration.containingFile, entityDeclaration.containingFile)
        }
    }
}
