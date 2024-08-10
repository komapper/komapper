package org.komapper.processor.command

import org.komapper.processor.BackquotedSymbols.QueryDsl
import org.komapper.processor.BackquotedSymbols.Value
import org.komapper.processor.BackquotedSymbols.typeOf
import org.komapper.processor.Context
import org.komapper.processor.name
import java.io.PrintWriter
import kotlin.collections.joinToString
import kotlin.collections.map
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
        when (command.result.kind) {
            CommandKind.Execute -> executeCommand()
            else -> fromCommand()
        }
    }

    private fun executeCommand() {
        val resultType = command.result.type.name
        val bindCalls = command.parameters.map {
            val name = it.name!!.asString()
            ".bindValue(\"$name\", $Value(command.$name, $typeOf<${it.type.resolve().name}>()))"
        }.joinToString("\n        ", prefix = "\n        ")
        w.println("public fun $QueryDsl.executeCommand(command: ${command.classDeclaration.asStarProjectedType().name}) : $resultType {")
        w.println("    val sql = \"\"\"${command.sql}\"\"\"")
        w.println("    val query = executeTemplate(sql)${ if (command.parameters.isNotEmpty()) bindCalls else ""}")
        w.println("    return with(command) {")
        w.println("        query.execute()")
        w.println("    }")
        w.println("}")
    }

    private fun fromCommand() {
        val resultType = command.result.type.name
        val bindCalls = command.parameters.map {
            val name = it.name!!.asString()
            ".bindValue(\"$name\", $Value(command.$name, $typeOf<${it.type.resolve().name}>()))"
        }.joinToString("\n        ", prefix = "\n        ")
        w.println("public fun $QueryDsl.fromCommand(command: ${command.classDeclaration.asStarProjectedType().name}) : $resultType {")
        w.println("    val sql = \"\"\"${command.sql}\"\"\"")
        w.println("    val builder = fromTemplate(sql)${ if (command.parameters.isNotEmpty()) bindCalls else ""}")
        w.println("    return with(command) {")
        w.println("        builder.select()")
        w.println("    }")
        w.println("}")
    }
}
