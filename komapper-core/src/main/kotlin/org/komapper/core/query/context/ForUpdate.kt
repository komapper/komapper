package org.komapper.core.query.context

import org.komapper.core.query.ForUpdateOption

data class ForUpdate(
    val option: ForUpdateOption? = null
)
