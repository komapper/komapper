package org.komapper.core.dsl.expression

sealed class LockOption {
    object Default : LockOption()
    object Nowait : LockOption()
    object SkipLocked : LockOption()
    class Wait(val second: Int) : LockOption()
}
