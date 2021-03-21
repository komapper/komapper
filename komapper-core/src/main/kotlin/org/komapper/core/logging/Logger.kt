package org.komapper.core.logging

import org.komapper.core.data.Statement

interface Logger {
    fun logTxMessage(message: () -> String)
    fun logStatement(statement: Statement)
}

open class StdoutLogger : Logger {
    override fun logTxMessage(message: () -> String) = println(message())
    override fun logStatement(statement: Statement) {
        val log = statement.log ?: statement.sql
        println(log)
    }
}
