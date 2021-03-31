package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSNode

internal class Exit(val level: Level, val report: Report) : Exception()

internal enum class Level { FAILURE, ERROR }

internal data class Report(val message: String, val node: KSNode)

internal fun report(message: String, node: KSNode, level: Level = Level.FAILURE): Nothing {
    throw Exit(level, Report(message, node))
}
