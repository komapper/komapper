package org.komapper.core.jdbc

import org.komapper.core.ThreadSafe

@ThreadSafe
data class PrimaryKey(
    val name: String,
    val isAutoIncrement: Boolean = false
)
