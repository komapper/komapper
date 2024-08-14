package org.komapper.core

interface TemplateBuiltinExtensions {
    fun String?.escape(): String?
    fun String?.asPrefix(): String?
    fun String?.asInfix(): String?
    fun String?.asSuffix(): String?

    fun CharSequence.isBlank() = Companion.isBlank(this)
    fun CharSequence.isNotBlank() = Companion.isNotBlank(this)
    fun CharSequence?.isNullOrBlank() = Companion.isNullOrBlank(this)
    fun CharSequence.isEmpty() = Companion.isEmpty(this)
    fun CharSequence.isNotEmpty() = Companion.isNotEmpty(this)
    fun CharSequence.any() = Companion.any(this)
    fun CharSequence.none() = Companion.none(this)

    val CharSequence.lastIndex: Int get() = Companion.lastIndex(this)

    companion object {
        fun isBlank(c: CharSequence) = c.isBlank()
        fun isNotBlank(c: CharSequence) = c.isNotBlank()
        fun isNullOrBlank(c: CharSequence?) = c.isNullOrBlank()
        fun isEmpty(c: CharSequence) = c.isEmpty()
        fun isNotEmpty(c: CharSequence) = c.isNotEmpty()
        fun any(c: CharSequence) = c.any()
        fun none(c: CharSequence) = c.none()

        fun lastIndex(c: CharSequence) = c.lastIndex
    }
}

fun TemplateBuiltinExtensions(escape: (String) -> String): TemplateBuiltinExtensions {
    return DefaultTemplateBuiltinExtensions(escape)
}

internal class DefaultTemplateBuiltinExtensions(val escape: (String) -> String) : TemplateBuiltinExtensions {

    override fun String?.escape(): String? {
        return this?.let { escape(it) }
    }

    override fun String?.asPrefix(): String? {
        return this?.let { "${escape(it)}%" }
    }

    override fun String?.asInfix(): String? {
        return this?.let { "%${escape(it)}%" }
    }

    override fun String?.asSuffix(): String? {
        return this?.let { "%${escape(it)}" }
    }
}
