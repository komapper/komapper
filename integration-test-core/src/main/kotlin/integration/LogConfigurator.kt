package integration

import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext

class LogConfigurator : BasicConfigurator() {
    override fun configure(lc: LoggerContext) {
        super.configure(lc)
        val logger: Logger = lc.getLogger("org.komapper")
        logger.level = Level.TRACE
        val rootLogger: Logger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.INFO
    }
}
