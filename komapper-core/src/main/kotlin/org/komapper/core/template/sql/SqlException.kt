package org.komapper.core.template.sql

open class SqlException(message: String, cause: Throwable?) : Exception(message, cause) {
    constructor(message: String) : this(message, null)
}
