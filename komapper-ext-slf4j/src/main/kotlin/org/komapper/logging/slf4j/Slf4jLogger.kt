package org.komapper.logging.slf4j

import org.komapper.core.LogCategory
import org.komapper.core.Logger
import org.slf4j.LoggerFactory

class Slf4jLogger : Logger {

    override fun log(category: LogCategory, message: () -> String) {
        val logger = LoggerFactory.getLogger(category.id)
        if (logger.isDebugEnabled) {
            logger.debug(message())
        }
    }
}
