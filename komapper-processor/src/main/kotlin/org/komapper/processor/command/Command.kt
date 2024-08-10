package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

data class Command(
    val sql: String,
    val classDeclaration: KSClassDeclaration,
    val parameters: List<KSValueParameter>,
    val result: CommandResult,
    val packageName: String,
    val fileName: String,
)
