package org.komapper.template.expression

class ExprBuiltinFunctionExtensions(val escape: (String) -> String) {

    open fun String?.escape(): String? {
        return this?.let { escape(it) }
    }

    open fun String?.asPrefix(): String? {
        return this?.let { "${escape(it)}%" }
    }

    open fun String?.asInfix(): String? {
        return this?.let { "%${escape(it)}%" }
    }

    open fun String?.asSuffix(): String? {
        return this?.let { "%${escape(it)}" }
    }
}
