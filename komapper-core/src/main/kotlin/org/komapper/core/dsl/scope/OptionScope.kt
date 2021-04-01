package org.komapper.core.dsl.scope

import org.komapper.core.dsl.query.QueryOption

internal interface OptionScope<OPTION : QueryOption> {
    fun asOption(): OPTION
}
