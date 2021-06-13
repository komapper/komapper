package integration.jdbc

import integration.jdbc.setting.SettingProvider
import integration.setting.Dbms
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace.GLOBAL
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import org.komapper.core.dsl.ScriptDsl
import org.komapper.jdbc.JdbcDatabase
import org.komapper.tx.jdbc.transaction
import org.komapper.tx.jdbc.transactionManager

internal class Env :
    BeforeAllCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver,
    ExecutionCondition,
    ExtensionContext.Store.CloseableResource {

    companion object {
        @Volatile
        var initialized: Boolean = false
    }

    private val setting = SettingProvider.get()
    private val db = JdbcDatabase.create(setting.config)
    private val txManager = db.config.session.transactionManager

    override fun beforeAll(context: ExtensionContext?) {
        if (!initialized) {
            initialized = true
            context?.root?.getStore(GLOBAL)?.put("drop all objects", this)
            db.transaction {
                db.runQuery {
                    ScriptDsl.execute(setting.createSql).options {
                        it.copy(suppressLogging = true)
                    }
                }
            }
        }
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
        val resetSql = setting.resetSql
        if (resetSql != null) {
            db.transaction {
                db.runQuery {
                    ScriptDsl.execute(resetSql).options {
                        it.copy(suppressLogging = true)
                    }
                }
            }
        }
        txManager.begin()
    }

    override fun afterTestExecution(context: ExtensionContext?) {
        txManager.rollback()
    }

    override fun close() {
        db.transaction {
            db.runQuery {
                ScriptDsl.execute(setting.dropSql).options {
                    it.copy(suppressLogging = true)
                }
            }
        }
    }

    override fun supportsParameter(
        parameterContext: ParameterContext?,
        extensionContext: ExtensionContext?
    ): Boolean = parameterContext!!.parameter.type === JdbcDatabase::class.java

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
