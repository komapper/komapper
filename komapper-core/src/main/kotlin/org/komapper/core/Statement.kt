package org.komapper.core

import kotlin.reflect.KClass

/**
 * The SQL statement.
 *
 * @property parts the parts of the SQL statement.
 */
@ThreadSafe
data class Statement(val parts: List<StatementPart>) {
    constructor(text: CharSequence) : this(listOf(StatementPart.Text(text)))

    companion object {
        val EMPTY = Statement(emptyList())

        fun createBindVariable(
            @Suppress("UNUSED_PARAMETER") index: Int,
            value: StatementPart.Value
        ): CharSequence {
            return value
        }
    }

    /**
     * The arguments of the SQL statement.
     */
    val args: List<Value<*>> = parts.filterIsInstance<StatementPart.Value>().map { it.value }

    /**
     * Converts the SQL statement to an SQL string.
     *
     * @param format the format function of the bind values
     */
    fun toSql(format: (Int, StatementPart.Value) -> CharSequence = ::createBindVariable): String {
        var index = 0
        return parts.joinToString(separator = "") { part ->
            when (part) {
                is StatementPart.Text -> part
                is StatementPart.Value -> {
                    format(index++, part)
                }
            }
        }
    }

    /**
     * Converts the SQL statement to an SQL string with arguments.
     *
     * @param format the format function of the bound values
     */
    fun toSqlWithArgs(format: (Any?, KClass<*>, Boolean) -> CharSequence): String {
        return parts.joinToString(separator = "") { part ->
            when (part) {
                is StatementPart.Text -> part.text
                is StatementPart.Value -> {
                    val value = part.value
                    format(value.any, value.klass, value.masking)
                }
            }
        }
    }

/**
     * Adds a part of the SQL statement.
     *
     * @param text a part of the SQL statement
     */
    infix operator fun plus(text: CharSequence): Statement {
        return copy(parts = parts + StatementPart.Text(text))
    }
}

data class DryRunStatement internal constructor(
    val sql: String = "",
    val sqlWithArgs: String = "",
    val args: List<Value<*>> = emptyList(),
) {

    companion object {
        val EMPTY = DryRunStatement()

        fun of(statement: Statement, dialect: Dialect): DryRunStatement {
            val sql = statement.toSql(dialect::createBindVariable)
            val sqlWithArgs = statement.toSqlWithArgs(dialect::formatValue)
            return DryRunStatement(sql = sql, sqlWithArgs = sqlWithArgs, args = statement.args)
        }

        fun of(statements: List<Statement>, dialect: Dialect): DryRunStatement {
            val sql = statements.joinToString(separator = ";") { it.toSql(dialect::createBindVariable) }
            val sqlWithArgs = statements.joinToString { it.toSqlWithArgs(dialect::formatValue) }
            val args = statements.fold(emptyList<Value<*>>()) { acc, statement -> acc + statement.args }
            return DryRunStatement(sql = sql, sqlWithArgs = sqlWithArgs, args = args)
        }
    }

    /**
     * Composes the statement.
     *
     * @param other the other statement
     */
    infix operator fun plus(other: DryRunStatement): DryRunStatement {
        return DryRunStatement(
            sql = concat(sql, other.sql),
            sqlWithArgs = concat(sqlWithArgs, other.sqlWithArgs),
            args = args + other.args
        )
    }

    private fun concat(left: String, right: String): String {
        val separator = if (sql.isEmpty() || sql.trimEnd().endsWith(";")) "" else ";"
        return left + separator + right
    }
}
