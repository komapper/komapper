package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.processor.Context
import org.komapper.processor.Exit
import org.komapper.processor.report
import org.komapper.template.sql.SqlException

internal class CommandAnalyzer(private val context: Context) {

    fun analyze(symbol: KSAnnotated): CommandAnalysisResult {
        val model = try {
            val command = CommandFactory(context, symbol).create()
            CommandModel(command)
        } catch (e: Exit) {
            return CommandAnalysisResult.Error(e)
        }
        try {
            validateCommand(model.command)
        } catch (e: Exit) {
            return CommandAnalysisResult.Failure(model, e)
        }
        return CommandAnalysisResult.Success(model)
    }

    private fun validateCommand(command: Command) {
        try {
            SqlValidator(context, command).validate()
        } catch (e: SqlException) {
            report("SQL validation error: ${e.message}", command.annotation)
        }
    }
}

internal sealed class CommandAnalysisResult {
    data class Success(val model: CommandModel) : CommandAnalysisResult()
    data class Failure(val model: CommandModel, val exit: Exit) : CommandAnalysisResult()
    data class Error(val exit: Exit) : CommandAnalysisResult()
    object Skip : CommandAnalysisResult()
}
