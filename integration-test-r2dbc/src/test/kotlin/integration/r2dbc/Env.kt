package integration.r2dbc

import integration.r2dbc.setting.SettingProvider
import integration.setting.Dbms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.runBlocking
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
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.withTransaction
import java.util.concurrent.atomic.AtomicBoolean

internal class Env :
    BeforeAllCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver,
    ExecutionCondition,
    ExtensionContext.Store.CloseableResource {

    companion object {
        val initialized: AtomicBoolean = AtomicBoolean(false)
    }

    private val setting = SettingProvider.get()
    private val db = R2dbcDatabase.create(setting.config)
    private var beforeAllFlow: Flow<Unit> = emptyFlow()

    override fun beforeAll(context: ExtensionContext?) {
        val self = this
        beforeAllFlow = flow {
            if (!initialized.getAndSet(true)) {
                context?.root?.getStore(GLOBAL)?.put("drop all objects", self)
                db.withTransaction {
                    db.runQuery {
                        ScriptDsl.execute(setting.createSql).options {
                            it.copy(suppressLogging = true)
                        }
                    }
                }
            }
        }
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
        var beforeTestExecutionFlow: Flow<Unit> = emptyFlow()
        val resetSql = setting.resetSql
        if (resetSql != null) {
            beforeTestExecutionFlow = flow {
                db.withTransaction {
                    db.runQuery {
                        ScriptDsl.execute(resetSql).options {
                            it.copy(suppressLogging = false)
                        }
                    }
                }
            }
        }

        runBlocking {
            beforeAllFlow.onCompletion {
                beforeTestExecutionFlow.collect()
            }.collect()
        }
    }

    override fun afterTestExecution(context: ExtensionContext?) {
//        txManager.rollback()
    }

    override fun close() = runBlocking {
        db.withTransaction {
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
    ): Boolean = parameterContext!!.parameter.type === R2dbcDatabase::class.java

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
