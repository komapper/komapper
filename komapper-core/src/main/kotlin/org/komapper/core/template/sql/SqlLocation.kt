package org.komapper.core.template.sql

import kotlin.math.max

class SqlLocation(private val sql: String, val lineNumber: Int, val position: Int) {
    override fun toString(): String = """
        |[
        |${format()}
        |]:$lineNumber:$position
    """.trimMargin()

    private fun format(): String {
        val lines = sql.lines()
        val prefix = lines.take(lineNumber)
        val markerLine = ".".repeat(max(position, 1) - 1) + "^"
        val suffix = lines.drop(lineNumber)
        return (prefix + markerLine + suffix).joinToString("\n")
    }
}
