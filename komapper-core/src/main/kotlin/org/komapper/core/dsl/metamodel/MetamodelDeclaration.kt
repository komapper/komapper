package org.komapper.core.dsl.metamodel

typealias MetamodelDeclaration<ENTITY, ID, META> = MetamodelScope<ENTITY, ID, META>.(META) -> Unit

infix operator fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> MetamodelDeclaration<ENTITY, ID, META>.plus(
    other: MetamodelDeclaration<ENTITY, ID, META>
): MetamodelDeclaration<ENTITY, ID, META> {
    return { metamodel ->
        this@plus(this, metamodel)
        other(this, metamodel)
    }
}
