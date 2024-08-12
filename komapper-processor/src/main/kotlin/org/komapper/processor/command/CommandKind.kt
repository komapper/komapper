package org.komapper.processor.command

import org.komapper.core.Exec
import org.komapper.core.ExecChange
import org.komapper.core.FetchMany
import org.komapper.core.FetchOne
import org.komapper.core.Many
import org.komapper.core.One
import org.komapper.core.ThreadSafe

@ThreadSafe
internal enum class CommandKind(val interfaceName: String, val abstractClasName: String) {
    ONE(One::class.qualifiedName!!, FetchOne::class.qualifiedName!!),
    MANY(Many::class.qualifiedName!!, FetchMany::class.qualifiedName!!),
    EXEC(Exec::class.qualifiedName!!, ExecChange::class.qualifiedName!!),
}
