package org.komapper.core.query.context

import org.komapper.core.query.option.ForUpdateOption

internal data class ForUpdate(
    val option: ForUpdateOption? = null
)
