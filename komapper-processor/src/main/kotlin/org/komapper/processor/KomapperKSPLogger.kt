package org.komapper.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.FileLocation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.NonExistLocation

class KomapperKSPLogger(private val logger: KSPLogger) : KSPLogger {
    override fun logging(message: String, symbol: KSNode?) {
        logger.logging(convertMessage(message, symbol))
    }

    override fun info(message: String, symbol: KSNode?) {
        logger.info(convertMessage(message, symbol))
    }

    override fun warn(message: String, symbol: KSNode?) {
        logger.warn(convertMessage(message, symbol))
    }

    override fun error(message: String, symbol: KSNode?) {
        logger.error(convertMessage(message, symbol))
    }

    override fun exception(e: Throwable) {
        logger.exception(e)
    }

    private fun convertMessage(message: String, symbol: KSNode?): String {
        return when (val location = symbol?.location) {
            is FileLocation -> "$message file://${location.filePath}:${location.lineNumber}"
            is NonExistLocation, null -> message
        }
    }
}
