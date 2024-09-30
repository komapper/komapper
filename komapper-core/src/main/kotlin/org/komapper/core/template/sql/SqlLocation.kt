package org.komapper.core.template.sql

class SqlLocation(
    private val sql: String,
    val lineNumber: Int,
    val startColumnIndex: Int,
    val endColumnIndex: Int,
) {
    companion object {
        private const val MARKER_START = ">>>"
        private const val MARKER_END = "<<<"
    }

    override fun toString(): String = """
        |[
        |${highlightSql()}
        |]:$lineNumber:$startColumnIndex..$endColumnIndex
    """.trimMargin()

    private fun highlightSql(): String {
        val lines = sql.lines()

        // Check if the line number is within the valid range
        if (lineNumber !in 1..lines.size) {
            return sql
        }

        val highlightedLines = lines.toMutableList()
        val targetLine = lines[lineNumber - 1]

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
        highlightedLines[lineNumber - 1] = highlightedLine

        return highlightedLines.joinToString("\n")
    }
}
