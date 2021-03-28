package org.komapper.core.dsl.context

import org.komapper.core.dsl.data.Operand

internal class ValuesContext(
    internal val values: MutableList<Pair<Operand.Column, Operand.Parameter>> = mutableListOf()
) : Collection<Pair<Operand.Column, Operand.Parameter>> by values {

    fun add(pair: Pair<Operand.Column, Operand.Parameter>) {
        values.add(pair)
    }
}
