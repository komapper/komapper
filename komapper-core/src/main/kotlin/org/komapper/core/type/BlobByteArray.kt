package org.komapper.core.type

@JvmInline
value class BlobByteArray(val value: ByteArray) {
    override fun toString(): String {
        return value.contentToString()
    }
}
