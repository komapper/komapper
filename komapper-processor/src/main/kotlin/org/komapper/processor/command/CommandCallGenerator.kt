package org.komapper.processor.command

import org.komapper.processor.BackquotedSymbols.QueryDsl
import org.komapper.processor.BackquotedSymbols.Value
import org.komapper.processor.BackquotedSymbols.typeOf
import org.komapper.processor.Context
import org.komapper.processor.name
import java.io.PrintWriter
import kotlin.collections.joinToString
import kotlin.text.isNotEmpty

internal class CommandCallGenerator(
    private val context: Context,
    private val command: Command,
    private val packageName: String,
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
        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }
    }

    private fun functionDeclaration() {
        val prefix = command.result.functionPrefix
        val commandTypeName = command.classDeclaration.asStarProjectedType().name
        val returnTypeName = command.result.returnType.name
        val bindCalls = command.parameters.joinToString("\n        ", prefix = "\n        ") {
            val name = it.name!!.asString()
            ".bindValue(\"$name\", $Value(command.$name, $typeOf<${it.type.resolve().name}>()))"
        }
        w.println("public fun $QueryDsl.${prefix}Command(command: $commandTypeName) : $returnTypeName {")
        w.println("    val sql = \"\"\"${command.sql}\"\"\".trimIndent()")
        w.println("    val binding = ${prefix}Template(sql)${ if (command.parameters.isNotEmpty()) bindCalls else ""}")
        w.println("    return with(command) {")
        w.println("        binding.execute()")
        w.println("    }")
        w.println("}")
    }
}
