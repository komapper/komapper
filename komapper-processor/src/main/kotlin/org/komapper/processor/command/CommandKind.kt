package org.komapper.processor.command

import org.komapper.core.Exec
import org.komapper.core.Many
import org.komapper.core.One
import org.komapper.core.ThreadSafe

@ThreadSafe
internal enum class CommandKind(val markerInterfaceName: String) {
    ONE(One::class.qualifiedName!!),
    MANY(Many::class.qualifiedName!!),
    EXEC(Exec::class.qualifiedName!!),
}
