package org.komapper.processor.command

import org.komapper.core.ThreadSafe
import org.komapper.processor.BackquotedSymbols.QueryDsl
import org.komapper.processor.BackquotedSymbols.Value
import org.komapper.processor.BackquotedSymbols.typeOf
import org.komapper.processor.name
import java.io.PrintWriter

@ThreadSafe
internal class CommandCallGenerator(
    private val command: Command,
    private val w: PrintWriter,
) : Runnable {

    override fun run() {
        suppress()
        packageDeclaration()
        functionDeclaration()
    }

    private fun suppress() {
        w.println("@file:Suppress(\"warnings\")")
    }

    private fun packageDeclaration() {
        if (command.packageName.isNotEmpty()) {
            w.println("package ${command.packageName}")
            w.println()
        }
    }

    private fun functionDeclaration() {
        val parameterTypeName = command.classDeclaration.asStarProjectedType().name
        val returnTypeName = command.returnType.name
        val templateCall = when (command.kind) {
            CommandKind.ONE -> "fromTemplate"
            CommandKind.MANY -> "fromTemplate"
            CommandKind.EXEC -> "executeTemplate"
            CommandKind.EXEC_RETURN_ONE -> "executeTemplate"
            CommandKind.EXEC_RETURN_MANY -> "executeTemplate"
        }.let { "$it(sql)" }
        val returningCall = if (command.kind.returning) "\n        .returning()" else ""
        val bindCalls = if (command.paramMap.isNotEmpty()) {
            command.paramMap.entries.joinToString("\n        ", prefix = "\n        ") {
                ".bindValue(\"${it.key}\", $Value(command.${it.key}, $typeOf<${it.value.name}>()))"
            }
        } else {
            ""
        }
        w.println("public fun $QueryDsl.`${command.functionName}`(command: $parameterTypeName) : $returnTypeName {")
        w.println("    val sql = \"\"\"${command.sql.replace("$", "\${'$'}") }\"\"\"")
        w.println("    val binding = $templateCall$returningCall$bindCalls")
        w.println("    return with(command) {")
        w.println("        binding.execute()")
        w.println("    }")
        w.println("}")
    }
}
