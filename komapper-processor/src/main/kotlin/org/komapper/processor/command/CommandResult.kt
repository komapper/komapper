package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSType

internal data class CommandResult(
    val kind: CommandKind,
    val returnType: KSType,
    val functionPrefix: String,
)
