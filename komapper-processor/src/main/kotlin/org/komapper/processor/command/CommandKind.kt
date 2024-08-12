package org.komapper.processor.command

import org.komapper.core.Exec
import org.komapper.core.ExecReturnMany
import org.komapper.core.ExecReturnOne
import org.komapper.core.Many
import org.komapper.core.One
import org.komapper.core.ThreadSafe

@ThreadSafe
internal enum class CommandKind(val className: String, val returning: Boolean = false) {
    ONE(One::class.qualifiedName!!, false),
    MANY(Many::class.qualifiedName!!, false),
    EXEC(Exec::class.qualifiedName!!, false),
    EXEC_RETURN_ONE(ExecReturnOne::class.qualifiedName!!, true),
    EXEC_RETURN_MANY(ExecReturnMany::class.qualifiedName!!, true),
}
