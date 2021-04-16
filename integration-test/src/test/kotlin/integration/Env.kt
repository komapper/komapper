package integration

import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ConditionEvaluationResult
import org.junit.jupiter.api.extension.ExecutionCondition
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.platform.commons.support.AnnotationSupport.findAnnotation
import org.komapper.core.Database
import org.komapper.core.dsl.ScriptQuery
import org.komapper.core.dsl.execute

internal class Env :
    BeforeTestExecutionCallback,
    AfterTestExecutionCallback,
    ParameterResolver,
    ExecutionCondition {

    private val setting = getSetting()
    private val db = Database(setting.config)
    private val txManager = db.config.session.transactionManager ?: error("Enable transaction.")

    override fun beforeTestExecution(context: ExtensionContext?) {
        db.transaction {
            db.execute {
                ScriptQuery.execute(setting.createSql)
            }
        }
        txManager.begin()
    }

    override fun afterTestExecution(context: ExtensionContext?) {
        txManager.rollback()
        db.transaction {
            db.execute {
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
                if (setting.dbms in it.onlyIf && setting.dbms !in it.unless) {
                    ConditionEvaluationResult.enabled("runnable: ${setting.dbms}")
                } else {
                    ConditionEvaluationResult.disabled("not runnable: ${setting.dbms}")
                }
            }.orElseGet {
                ConditionEvaluationResult.enabled("@Run is not present")
            }
    }
}

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Run(val onlyIf: Array<Dbms> = [], val unless: Array<Dbms> = [])
