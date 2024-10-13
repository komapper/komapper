package org.komapper.core.dsl.scope

import org.komapper.core.dsl.expression.LockOption
import org.komapper.core.dsl.expression.LockTarget
import org.komapper.core.dsl.metamodel.EntityMetamodel

class ForUpdateScope {
    var lockTarget: LockTarget = LockTarget.Empty
    var lockOption: LockOption = LockOption.Default

    fun of(vararg metamodels: EntityMetamodel<*, *, *>): ForUpdateScope {
        lockTarget = LockTarget.Metamodels(metamodels.toList())
        return this
    }

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
