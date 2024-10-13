package org.komapper.core.template.sql

class SqlLocation(
    private val sql: String,
    val lineNumber: Int,
    @Deprecated("Do not use this property directly.")
    val position: Int,
    val columnNumber: Int,
    val tokenLength: Int,
) {
    companion object {
        private const val MARKER_START = ">>>"
        private const val MARKER_END = "<<<"
    }

    override fun toString(): String = """
        |[
        |${highlightSql()}
        |]:$lineNumber:$columnNumber
        """.trimMargin()

    private fun highlightSql(): String {
        val lineIndex = lineNumber - 1
        val startColumnIndex = columnNumber - 1
        val endColumnIndex = startColumnIndex + tokenLength
        val lines = sql.lines()

        // Check if the line index is within the valid range
        if (lineIndex !in lines.indices) {
            return sql
        }

        val highlightedLines = lines.toMutableList()
        val targetLine = lines[lineIndex]

        // Check if the column indices are within the valid range
        if (startColumnIndex !in 0..targetLine.length || endColumnIndex !in startColumnIndex..targetLine.length) {
            return sql
        }

        // Add markers to the target line
        val highlightedLine = buildString {
            append(targetLine.substring(0, startColumnIndex))
            append(MARKER_START)
            append(targetLine.substring(startColumnIndex, endColumnIndex))
            append(MARKER_END)
            append(targetLine.substring(endColumnIndex))
        }
        highlightedLines[lineIndex] = highlightedLine

        return highlightedLines.joinToString("\n")
    }
}
