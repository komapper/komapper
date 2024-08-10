package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.processor.Context
import org.komapper.processor.Exit

internal class CommandAnalyzer(private val context: Context) {

    fun analyze(symbol: KSAnnotated): CommandAnalysisResult {
        return try {
            val command = CommandFactory(context, symbol).create()
            validateCommand(command)
            val model = CommandModel(command)
            CommandAnalysisResult.Success(model)
        } catch (e: Exit) {
            CommandAnalysisResult.Error(e)
        }
    }

    private fun validateCommand(command: Command) {
        // TODO
    }
}

internal sealed class CommandAnalysisResult {
    data class Success(val model: CommandModel) : CommandAnalysisResult()
    data class Failure(val model: CommandModel, val exit: Exit) : CommandAnalysisResult()
    data class Error(val exit: Exit) : CommandAnalysisResult()
    object Skip : CommandAnalysisResult()
}
