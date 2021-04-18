package integration

import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import org.komapper.core.Database
import org.komapper.core.dsl.ScriptQuery
import org.komapper.core.dsl.runQuery

internal class Env :
    BeforeAllCallback,
    AfterAllCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver,
    ExecutionCondition {

    private val setting = getSetting()
    private val db = Database(setting.config)
    private val txManager = db.config.session.transactionManager ?: error("Enable transaction.")

    override fun beforeAll(context: ExtensionContext?) {
        db.transaction {
            db.runQuery {
                ScriptQuery.execute(setting.createSql)
            }
        }
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
        txManager.begin()
    }

    override fun afterTestExecution(context: ExtensionContext?) {
        txManager.rollback()
    }

    override fun afterAll(context: ExtensionContext?) {
        db.transaction {
            db.runQuery {
                ScriptQuery.execute(setting.dropSql)
            }
        }
    }

    override fun supportsParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Boolean = parameterContext!!.parameter.type === Database::class.java

    override fun resolveParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Any = db

    override fun evaluateExecutionCondition(context: ExtensionContext): ConditionEvaluationResult? {
        return findAnnotation(context.element, Run::class.java)
            .map {
                if (isRunnable(it)) {
                    ConditionEvaluationResult.enabled("runnable: ${setting.dbms}")
                } else {
                    ConditionEvaluationResult.disabled("not runnable: ${setting.dbms}")
                }
            }.orElseGet {
                ConditionEvaluationResult.enabled("@Run is not present")
            }
    }

    private fun isRunnable(run: Run): Boolean {
        val dbms = setting.dbms
        with(run) {
            if (onlyIf.isNotEmpty()) {
                return dbms in onlyIf
            }
            if (unless.isNotEmpty()) {
                return dbms !in unless
            }
        }
        return true
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Run(val onlyIf: Array<Dbms> = [], val unless: Array<Dbms> = [])
