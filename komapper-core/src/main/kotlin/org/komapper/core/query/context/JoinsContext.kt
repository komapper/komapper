package org.komapper.core.query.context

internal class JoinsContext(
    private val joins: MutableList<JoinContext<*>> = mutableListOf()
) :
    Collection<JoinContext<*>> by joins {

    fun add(join: JoinContext<*>) {
        joins.add(join)
    }
}
