package org.komapper.core

interface TemplateBuiltinExtensions {
    fun String?.escape(): String?
    fun String?.asPrefix(): String?
    fun String?.asInfix(): String?
    fun String?.asSuffix(): String?
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
