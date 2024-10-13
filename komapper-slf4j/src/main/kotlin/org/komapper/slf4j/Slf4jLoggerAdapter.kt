package org.komapper.slf4j

import org.komapper.core.Logger
import org.slf4j.LoggerFactory

class Slf4jLoggerAdapter : Logger {
    override fun trace(category: String, message: () -> String) {
        val logger = LoggerFactory.getLogger(category)
        if (logger.isTraceEnabled) {
            logger.trace(message())
        }
    }

    override fun debug(category: String, message: () -> String) {
        val logger = LoggerFactory.getLogger(category)
        if (logger.isDebugEnabled) {
            logger.debug(message())
        }
    }

    override fun info(category: String, message: () -> String) {
        val logger = LoggerFactory.getLogger(category)
        if (logger.isInfoEnabled) {
            logger.info(message())
        }
    }

    override fun warn(category: String, message: () -> String) {
        val logger = LoggerFactory.getLogger(category)
        if (logger.isWarnEnabled) {
            logger.warn(message())
        }
    }

    override fun error(category: String, message: () -> String) {
        val logger = LoggerFactory.getLogger(category)
        if (logger.isErrorEnabled) {
            logger.error(message())
        }
    }
}
