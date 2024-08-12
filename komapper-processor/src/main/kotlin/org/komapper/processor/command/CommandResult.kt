package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSType
import org.komapper.core.ThreadSafe

@ThreadSafe
internal data class CommandResult(
    val kind: CommandKind,
    val returnType: KSType,
)
