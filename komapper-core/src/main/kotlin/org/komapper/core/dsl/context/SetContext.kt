package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Operand

internal class SetContext(
    internal val set: MutableList<Pair<Operand.Column, Operand>> = mutableListOf()
) : Collection<Pair<Operand.Column, Operand>> by set {

    fun add(pair: Pair<Operand.Column, Operand>) {
        set.add(pair)
    }
}
