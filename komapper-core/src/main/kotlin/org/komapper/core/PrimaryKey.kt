package org.komapper.core

@ThreadSafe
data class PrimaryKey(
    val name: String,
    val isAutoIncrement: Boolean = false
)
