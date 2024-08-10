package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter

internal data class Command(
    val sql: String,
    val disableValidation: Boolean,
    val annotation: KSAnnotation,
    val classDeclaration: KSClassDeclaration,
    val parameters: List<KSValueParameter>,
    val result: CommandResult,
    val packageName: String,
    val fileName: String,
)
