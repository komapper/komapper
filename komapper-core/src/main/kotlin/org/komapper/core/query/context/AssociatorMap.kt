package org.komapper.core.query.context

import org.komapper.core.query.Associator

internal class AssociatorMap(private val map: MutableMap<Association, Associator<Any, Any>> = mutableMapOf()) :
    Map<Association, Associator<Any, Any>> by map {

    operator fun set(key: Association, value: Associator<Any, Any>) {
        map[key] = value
    }
}
