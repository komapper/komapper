package org.komapper.template.sql

internal class SqlLocation(private val sql: String, val lineNumber: Int, val position: Int) {
    override fun toString(): String = "<$sql>:$lineNumber:$position"
}
