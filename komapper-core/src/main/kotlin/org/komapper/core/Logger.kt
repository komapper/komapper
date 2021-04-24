package org.komapper.core

interface Logger {
    fun log(category: LogCategory, message: () -> String)
}

open class SqlStdOutLogger : Logger {
    override fun log(category: LogCategory, message: () -> String) {
        if (category == LogCategory.SQL) {
            println(message())
        }
    }
}

enum class LogCategory(val id: String) {
    SQL("org.komapper.core.sql"),
    SQL_WITH_ARGS("org.komapper.core.sqlWithArgs"),
    TRANSACTION("org.komapper.transaction"),
    OTHER("org.komapper.other")
}
