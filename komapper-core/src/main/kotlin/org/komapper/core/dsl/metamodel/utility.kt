package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.WhereDeclaration

fun <ENTITY : Any> EntityMetamodel<ENTITY, *, *>.getAutoIncrementProperty(): PropertyMetamodel<ENTITY, *, *>? {
    val idGenerator = this.idGenerator()
    return if (idGenerator is IdGenerator.AutoIncrement<ENTITY, *>) idGenerator.property else null
}

fun <ENTITY : Any> EntityMetamodel<ENTITY, *, *>.getNonAutoIncrementProperties(): List<PropertyMetamodel<ENTITY, *, *>> {
    val property = getAutoIncrementProperty()
    return this.properties().filter { it != property }
}

fun PropertyMetamodel<*, *, *>.isAutoIncrement(): Boolean {
    return this == this.owner.getAutoIncrementProperty()
}

/**
 * Creates a new entity metamodel.
 * @param declaration the entity metamodel declaration
 * @return the new entity metamodel
 */
fun <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> META.define(declaration: EntityMetamodelDeclaration<META>): META {
    return newMetamodel(
        table = tableName(),
        catalog = catalogName(),
        schema = schemaName(),
        alwaysQuote = alwaysQuote(),
        disableSequenceAssignment = disableSequenceAssignment(),
        declaration = declaration() + declaration
    )
}

/**
 * Returns the where declaration.
 */
val <ENTITY : Any, ID : Any, META : EntityMetamodel<ENTITY, ID, META>> META.where: WhereDeclaration
    get() {
        val metamodel = this
        val scope = EntityMetamodelScope().apply {
            val declaration = metamodel.declaration()
            declaration(metamodel)
        }
        return scope.where
    }
