package integration.core

import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import org.komapper.core.LogCategory

class LogConfigurator : BasicConfigurator() {
    override fun configure(lc: LoggerContext) {
        super.configure(lc)
        val sqlLogger: Logger = lc.getLogger(LogCategory.SQL_WITH_ARGS.value)
        sqlLogger.level = Level.TRACE
        val txLogger: Logger = lc.getLogger(LogCategory.TRANSACTION.value)
        txLogger.level = Level.TRACE
        val rootLogger: Logger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.INFO
    }
}
