package integration.core

import ch.qos.logback.classic.BasicConfigurator
import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.Configurator
import org.komapper.core.LogCategory

class LogConfigurator : BasicConfigurator() {
    override fun configure(lc: LoggerContext): Configurator.ExecutionStatus {
        val status = super.configure(lc)
        val sqlLogger: Logger = lc.getLogger(LogCategory.SQL_WITH_ARGS)
        sqlLogger.level = Level.TRACE
        val txLogger: Logger = lc.getLogger(LogCategory.TRANSACTION)
        txLogger.level = Level.TRACE
        val rootLogger: Logger = lc.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.INFO
        return when (status) {
            Configurator.ExecutionStatus.NEUTRAL -> Configurator.ExecutionStatus.DO_NOT_INVOKE_NEXT_IF_ANY
            else -> status
        }
    }
}
