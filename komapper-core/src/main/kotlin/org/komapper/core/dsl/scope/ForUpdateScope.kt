package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.LockOption

class ForUpdateScope {

    var lockOption: LockOption = LockOption.Default

    fun nowait() {
        lockOption = LockOption.Nowait
    }

    fun skipLocked() {
        lockOption = LockOption.SkipLocked
    }

    fun wait(second: Int) {
        lockOption = LockOption.Wait(second)
    }
}
