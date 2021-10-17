package org.komapper.core.dsl.metamodel

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
