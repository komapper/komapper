package org.komapper.core.dsl.options

import org.komapper.core.EntityNotFoundException

interface MutationOptions : QueryOptions {
    /**
     * Whether to suppress [EntityNotFoundException].
     */
    val suppressEntityNotFoundException: Boolean
}
