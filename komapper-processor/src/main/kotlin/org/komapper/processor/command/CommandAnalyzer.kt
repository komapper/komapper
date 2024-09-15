package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.core.template.expression.CacheExprNodeFactory
import org.komapper.core.template.sql.CacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import org.komapper.processor.Context
import org.komapper.processor.Exit
import org.komapper.processor.report
import kotlin.reflect.KClass

internal class CommandAnalyzer(private val context: Context, private val annotationClass: KClass<*>) {

    private val sqlNodeFactory = CacheSqlNodeFactory()
    private val exprValidator = ExprValidator(context, CacheExprNodeFactory())

    fun analyze(symbol: KSAnnotated): CommandAnalysisResult {
        val command = try {
            CommandFactory(context, annotationClass, symbol).create().let {
                reassembleSql(it)
            }
        } catch (e: Exit) {
            return CommandAnalysisResult.Error(e)
        }
        if (!command.disableValidation) {
            try {
                validateCommand(command)
            } catch (e: Exit) {
                return CommandAnalysisResult.Failure(command, e)
            }
        }
        return CommandAnalysisResult.Success(command)
    }

    private fun reassembleSql(command: Command): Command {
        val sql =
            try {
                SqlReassembler(context, command.sql, command.paramMap, sqlNodeFactory, exprValidator).assemble()
            } catch (e: SqlException) {
                report("SQL reassembly error: ${e.message}", command.annotation)
            }
        return command.copy(sql = sql)
    }

    private fun validateCommand(command: Command) {
        val usedParams = try {
            SqlValidator(context, command.sql, command.paramMap, sqlNodeFactory, exprValidator).validate()
        } catch (e: SqlException) {
            report("SQL validation error: ${e.message}", command.annotation)
        }
        val unusedParams = command.paramMap.keys - usedParams - command.unusedParams
        if (unusedParams.isNotEmpty()) {
            context.logger.warn("Unused SQL params: $unusedParams. You can suppress this warning message by specifying @KomapperUnused for the param properties.", command.classDeclaration)
        }
    }
}

internal sealed class CommandAnalysisResult {
    data class Success(val command: Command) : CommandAnalysisResult()
    data class Failure(val command: Command, val exit: Exit) : CommandAnalysisResult()
    data class Error(val exit: Exit) : CommandAnalysisResult()
}
