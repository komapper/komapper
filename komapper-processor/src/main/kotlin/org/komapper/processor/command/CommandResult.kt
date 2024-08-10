package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSType

data class CommandResult(val kind: CommandKind, val type: KSType)
