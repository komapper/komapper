package org.komapper.core

interface Logger {
    fun log(category: LogCategory, message: () -> String)
}

open class StdOutSqlLogger : Logger {
    override fun log(category: LogCategory, message: () -> String) {
        if (category == LogCategory.SQL) {
            println(message())
        }
    }
}

enum class LogCategory(val id: String) {
    SQL("org.komapper.SQL"),
    SQL_WITH_ARGS("org.komapper.SQL_WITH_ARGS"),
    TRANSACTION("org.komapper.TRANSACTION"),
    OTHER("org.komapper.OTHER")
}
