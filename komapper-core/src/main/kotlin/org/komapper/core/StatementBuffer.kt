package org.komapper.core

class StatementBuffer {
    val charSequences = mutableListOf<CharSequence>()
    val args = mutableListOf<Value>()

    fun append(charSequence: CharSequence): StatementBuffer {
        charSequences.add(charSequence)
        return this
    }

    fun append(statement: Statement): StatementBuffer {
        charSequences.addAll(statement.charSequences)
        args.addAll(statement.args)
        return this
    }

    fun bind(value: Value): StatementBuffer {
        charSequences.add(PlaceHolder)
        args.add(value)
        return this
    }

    fun cutBack(length: Int): StatementBuffer {
        val last = charSequences.removeLast()
        if (last is PlaceHolder || last.length < length) error("Cannot cutBack.")
        val newLast = last.dropLast(length)
        if (newLast.isNotEmpty()) charSequences.add(newLast)
        return this
    }

    fun toStatement(): Statement {
        if (charSequences.isEmpty() && args.isEmpty()) {
            return Statement.EMPTY
        }
        return Statement(charSequences, args)
    }

    override fun toString(): String {
        return toStatement().toSql()
    }
}
