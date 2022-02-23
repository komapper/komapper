package integration.r2dbc

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
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.r2dbc.withTransaction

class R2dbcEnv :
    BeforeAllCallback,
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver,
    ExecutionCondition {

    companion object {
        @Volatile
        private var initialized: Boolean = false
        private val setting = R2dbcSettingProvider.get()
        private val db = R2dbcDatabase.create(setting.config)
    }

    override fun beforeAll(context: ExtensionContext?) {
        if (!initialized) {
            initialized = true
            runBlockingWithTimeout {
                db.withTransaction {
                    db.runQuery {
                        QueryDsl.executeScript(setting.createSql).options {
                            it.copy(suppressLogging = true)
                        }
                    }
                }
            }
        }
    }

    override fun beforeTestExecution(context: ExtensionContext?) {
        val resetSql = setting.resetSql
        if (resetSql != null) {
            runBlockingWithTimeout {
                db.withTransaction {
                    db.runQuery {
                        QueryDsl.executeScript(resetSql).options {
                            it.copy(suppressLogging = true)
                        }
                    }
                }
            }
        }
    }

    override fun afterTestExecution(context: ExtensionContext?) {
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
