package org.komapper.core.dsl.metamodel

import org.komapper.core.dsl.expression.WhereDeclaration

fun <ENTITY : Any> EntityMetamodel<ENTITY, *, *>.getAutoIncrementProperty(): PropertyMetamodel<ENTITY, *, *>? {
    val idAssignment = this.idAssignment()
    return if (idAssignment is IdAssignment.AutoIncrement<ENTITY, *>) idAssignment.property else null
}

fun <ENTITY : Any> EntityMetamodel<ENTITY, *, *>.getNonAutoIncrementProperties(): List<PropertyMetamodel<ENTITY, *, *>> {
    val property = getAutoIncrementProperty()
    return this.properties().filter { it != property }
}

fun PropertyMetamodel<*, *, *>.isAutoIncrement(): Boolean {
    return this == this.owner.getAutoIncrementProperty()
}

fun <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> META.define(declaration: MetamodelDeclaration<ENTITY, ID, META>): META {
    return newMetamodel(
        table = tableName(),
        catalog = catalogName(),
        schema = schemaName(),
        alwaysQuote = alwaysQuote(),
        disableSequenceAssignment = false,
        declarations = declarations() + declaration
    )
}

val <ENTITY : Any, ID, META : EntityMetamodel<ENTITY, ID, META>> META.where: List<WhereDeclaration>
    get() {
        val metamodel = this
        val scope = MetamodelScope<ENTITY, ID, META>().apply {
            metamodel.declarations().forEach { it(metamodel) }
        }
        return scope.where
    }
