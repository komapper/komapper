package org.komapper.core.type

@JvmInline
value class ClobString(val value: String) : CharSequence by value
