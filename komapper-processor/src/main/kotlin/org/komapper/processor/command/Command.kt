package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import org.komapper.core.ThreadSafe

@ThreadSafe
internal data class Command(
    val kind: CommandKind,
    val sql: String,
    val disableValidation: Boolean,
    val annotation: KSAnnotation,
    val classDeclaration: KSClassDeclaration,
    val functionName: String,
    val paramMap: Map<String, KSType>,
    val unusedParams: Set<String>,
    val returnType: KSType,
    val packageName: String,
    val fileName: String,
)
