package org.komapper.processor.command

import org.komapper.core.ThreadSafe
import org.komapper.processor.BackquotedSymbols.QueryDsl
import org.komapper.processor.BackquotedSymbols.Value
import org.komapper.processor.BackquotedSymbols.typeOf
import org.komapper.processor.name
import java.io.PrintWriter
import kotlin.collections.joinToString
import kotlin.text.isNotEmpty

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
        val prefix = command.result.functionPrefix
        val commandTypeName = command.classDeclaration.asStarProjectedType().name
        val returnTypeName = command.result.returnType.name
        val bindCalls = command.paramMap.entries.joinToString("\n        ", prefix = "\n        ") {
            ".bindValue(\"${it.key}\", $Value(command.${it.key}, $typeOf<${it.value.name}>()))"
        }
        w.println("public fun $QueryDsl.`${command.name}`(command: $commandTypeName) : $returnTypeName {")
        w.println("    val sql = \"\"\"${command.sql}\"\"\".trimIndent()")
        w.println("    val binding = ${prefix}Template(sql)${ if (command.paramMap.isNotEmpty()) bindCalls else ""}")
        w.println("    return with(command) {")
        w.println("        binding.execute()")
        w.println("    }")
        w.println("}")
    }
}
