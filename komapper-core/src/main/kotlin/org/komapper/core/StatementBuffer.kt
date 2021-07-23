package org.komapper.core

class StatementBuffer {
    val parts = mutableListOf<StatementPart>()

    fun append(text: CharSequence): StatementBuffer {
        parts.add(StatementPart.Text(text))
        return this
    }

    fun append(statement: Statement): StatementBuffer {
        parts.addAll(statement.parts)
        return this
    }

    fun bind(value: Value): StatementBuffer {
        parts.add(StatementPart.PlaceHolder(value))
        return this
    }

    fun cutBack(length: Int): StatementBuffer {
        val last = parts.removeLast()
        if (last is StatementPart.PlaceHolder || last.length < length) error("Cannot cutBack.")
        val newLast = last.dropLast(length)
        if (newLast.isNotEmpty()) parts.add(StatementPart.Text(newLast))
        return this
    }

    fun toStatement(): Statement {
        if (parts.isEmpty()) {
            return Statement.EMPTY
        }
        return Statement(parts)
    }

    override fun toString(): String {
        return toStatement().toSql()
    }
}
