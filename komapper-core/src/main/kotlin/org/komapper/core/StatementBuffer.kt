package org.komapper.core

class StatementBuffer {
    val fragments = mutableListOf<CharSequence>()
    val args = ArrayList<Value>()

    fun append(s: CharSequence): StatementBuffer {
        fragments.add(s)
        return this
    }

    fun append(statement: Statement): StatementBuffer {
        fragments.addAll(statement.fragments)
        args.addAll(statement.values)
        return this
    }

    fun bind(value: Value): StatementBuffer {
        fragments.add(PlaceHolder)
        args.add(value)
        return this
    }

    fun cutBack(length: Int): StatementBuffer {
        val last = fragments.removeLast()
        if (last is PlaceHolder || last.length < length) error("Cannot cutBack.")
        val newLast = last.dropLast(length)
        if (newLast.isNotEmpty()) fragments.add(newLast)
        return this
    }

    fun toStatement(): Statement {
        if (fragments.isEmpty() && args.isEmpty()) {
            return Statement.EMPTY
        }
        return Statement(fragments, args)
    }

    override fun toString(): String {
        return toStatement().asSql()
    }
}
