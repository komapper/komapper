package integration.jdbc

import integration.core.Run
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import org.komapper.core.dsl.QueryDsl
import org.komapper.jdbc.JdbcDatabase
import org.komapper.tx.core.TransactionProperty
import org.komapper.tx.jdbc.JdbcTransactionSession

class JdbcEnv :
    BeforeAllCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver,
    ExecutionCondition {

    companion object {
        @Volatile
        private var initialized: Boolean = false
        private val setting = JdbcSettingProvider.get()
        private val db = setting.database
        private val txManager = run {
            val session = db.config.session as JdbcTransactionSession
            session.transactionManager
        }
    }

    override fun beforeAll(context: ExtensionContext) {
        if (!initialized) {
            initialized = true
            db.runQuery {
                QueryDsl.executeScript(setting.createSql).options {
                    it.copy(suppressLogging = true)
                }
            }
        }
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        val resetSql = setting.resetSql
        if (resetSql != null) {
            db.runQuery {
                QueryDsl.executeScript(resetSql).options {
                    it.copy(suppressLogging = true)
                }
            }
        }
        txManager.begin(TransactionProperty.Name(context.displayName))
    }

    override fun afterTestExecution(context: ExtensionContext) {
        txManager.rollback()
    }

    override fun supportsParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Boolean = parameterContext.parameter.type === JdbcDatabase::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext,
        extensionContext: ExtensionContext,
    ): Any = db

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult? {
        return findAnnotation(context.element, Run::class.java)
            .map {
                if (Run.isRunnable(it, setting.dbms)) {
                    ConditionEvaluationResult.enabled("runnable: ${setting.dbms}")
                } else {
                    ConditionEvaluationResult.disabled("not runnable: ${setting.dbms}")
                }
            }.orElseGet {
                ConditionEvaluationResult.enabled("@Run is not present")
            }
    }
}
