package org.komapper.core.dsl.metamodel

/**
 * A type alias for a function that takes an EntityMetamodelScope and a META instance, and returns Unit.
 *
 * @param META The type of the metamodel.
 */
typealias EntityMetamodelDeclaration<META> = EntityMetamodelScope.(META) -> Unit

/**
 * Combines two EntityMetamodelDeclaration functions into a single declaration.
 * This operator function allows chaining of two metamodel declarations.
 *
 * @param ENTITY The type of the entity.
 * @param ID The type of the entity's identifier.
 * @param META The type of the metamodel.
 * @param other Another EntityMetamodelDeclaration to be combined with the current one.
 * @return A new EntityMetamodelDeclaration that combines the current and the other declaration.
 */
infix operator fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> EntityMetamodelDeclaration<META>.plus(
    other: EntityMetamodelDeclaration<META>,
): EntityMetamodelDeclaration<META> {
    return { metamodel ->
        this@plus(this, metamodel)
        other(this, metamodel)
    }
}
