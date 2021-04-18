package org.komapper.template.expression

class ExprBuiltinFunctionExtensions(val escape: (String) -> String) {

    fun String?.escape(): String? {
        return this?.let { escape(it) }
    }

    fun String?.asPrefix(): String? {
        return this?.let { "${escape(it)}%" }
    }

    fun String?.asInfix(): String? {
        return this?.let { "%${escape(it)}%" }
    }

    fun String?.asSuffix(): String? {
        return this?.let { "%${escape(it)}" }
    }
}
