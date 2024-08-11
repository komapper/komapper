package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.core.ThreadSafe
import org.komapper.core.template.sql.SqlException
import org.komapper.processor.Context
import org.komapper.processor.Exit
import org.komapper.processor.report
import kotlin.reflect.KClass

@ThreadSafe
internal class CommandAnalyzer(private val context: Context, private val annotationClass: KClass<*>) {
    private val sqlValidator = SqlValidator(context)

    fun analyze(symbol: KSAnnotated): CommandAnalysisResult {
        val command = try {
            CommandFactory(context, annotationClass, symbol).create()
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

    private fun validateCommand(command: Command) {
        try {
            sqlValidator.validate(command)
        } catch (e: SqlException) {
            report("SQL validation error: ${e.message}", command.annotation)
        }
    }
}

internal sealed class CommandAnalysisResult {
    data class Success(val command: Command) : CommandAnalysisResult()
    data class Failure(val command: Command, val exit: Exit) : CommandAnalysisResult()
    data class Error(val exit: Exit) : CommandAnalysisResult()
}
