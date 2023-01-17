package org.komapper.core.value

@JvmInline
value class ClobString internal constructor(val value: String) {
    companion object {
        fun of(value: String) = ClobString(value)
    }
}
