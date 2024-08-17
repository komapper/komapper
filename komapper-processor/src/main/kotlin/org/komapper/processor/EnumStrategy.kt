package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotation

sealed interface EnumStrategy {

    object Name : EnumStrategy {
        const val propertyName: String = "name"
        const val typeName: String = "String"
    }

    object Ordinal : EnumStrategy {
        const val propertyName: String = "ordinal"
        const val typeName: String = "Int"
    }

    data class Property(
        val propertyName: String,
        val annotation: KSAnnotation,
    ) : EnumStrategy

    object Type : EnumStrategy
}
