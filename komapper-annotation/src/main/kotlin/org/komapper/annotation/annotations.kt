@file:Suppress("unused")

package org.komapper.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntity

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperId

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperVersion

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperTable(
    val name: String = KomapperTable.name,
    val catalog: String = KomapperTable.catalog,
    val schema: String = KomapperTable.schema,
    val alwaysQuote: Boolean = KomapperTable.alwaysQuote
) {
    companion object {
        const val name: String = ""
        const val catalog: String = ""
        const val schema: String = ""
        const val alwaysQuote: Boolean = false
    }
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperColumn(
    val name: String = KomapperColumn.name,
    val alwaysQuote: Boolean = KomapperColumn.alwaysQuote
) {
    companion object {
        const val name = ""
        const val alwaysQuote = false
    }
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperCreatedAt

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperUpdatedAt

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperIgnore

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperSequence(
    val name: String,
    val startWith: Int = KomapperSequence.startWith,
    val incrementBy: Int = KomapperSequence.incrementBy,
    val catalog: String = KomapperSequence.catalog,
    val schema: String = KomapperSequence.schema,
    val alwaysQuote: Boolean = KomapperSequence.alwaysQuote
) {
    companion object {
        const val startWith: Int = 1
        const val incrementBy: Int = 50
        const val catalog: String = ""
        const val schema: String = ""
        const val alwaysQuote: Boolean = false
    }
}

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperAutoIncrement

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntityDef(val entity: KClass<*>)
