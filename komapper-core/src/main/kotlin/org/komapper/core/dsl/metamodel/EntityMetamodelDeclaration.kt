package org.komapper.core.dsl.metamodel

typealias EntityMetamodelDeclaration<META> = EntityMetamodelScope.(META) -> Unit

infix operator fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityMetamodelDeclaration<META>.plus(
    other: EntityMetamodelDeclaration<META>,
): EntityMetamodelDeclaration<META> {
    return { metamodel ->
        this@plus(this, metamodel)
        other(this, metamodel)
    }
}
