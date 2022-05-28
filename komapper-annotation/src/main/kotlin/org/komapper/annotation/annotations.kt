@file:Suppress("unused")

package org.komapper.annotation

import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

/**
 * Indicates that the annotated class is an entity class.
 *
 * The annotated class must be a data class.
 *
 * @param aliases the names of the entity metamodel instances
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntity(
    val aliases: Array<String> = [],
)

/**
 * Indicates that the annotated property is a primary key.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperId

/**
 * Indicates that the annotated property is a version number of the optimistic lock.
 *
 * The type of the annotated property must be one of the following:
 * - [Int]
 * - [Long]
 * - [UInt]
 * - A value class whose property type is one of [Int], [Long] or [UInt].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperVersion

/**
 * Indicates that the annotated property is an enum class.
 * * @property type the mapping strategy
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEnum(val type: EnumType)

/**
 * Adds table information.
 *
 * @property name the table name
 * @property catalog the catalog name
 * @property schema the schema name
 * @property alwaysQuote whether to quote the [name], [catalog] and [schema]
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperTable(
    val name: String = NAME,
    val catalog: String = CATALOG,
    val schema: String = SCHEMA,
    val alwaysQuote: Boolean = ALWAYS_QUOTE
) {
    companion object {
        const val NAME: String = ""
        const val CATALOG: String = ""
        const val SCHEMA: String = ""
        const val ALWAYS_QUOTE: Boolean = false
    }
}

/**
 * Adds column information.
 *
 * @property name the table name
 * @property alwaysQuote whether to quote the [name]
 * @property masking whether to mask the value that corresponds to the annotated property in logs
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperColumn(
    val name: String = NAME,
    val alwaysQuote: Boolean = ALWAYS_QUOTE,
    val masking: Boolean = MASKING,
) {
    companion object {
        const val NAME = ""
        const val ALWAYS_QUOTE = false
        const val MASKING = false
    }
}

/**
 * Indicates that the annotated property is a timestamp to be set at creation time.
 *
 * The type of the annotated property must be one of the following:
 * - [LocalDateTime]
 * - [OffsetDateTime]
 * - A value class whose property type is [LocalDateTime] or [OffsetDateTime].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperCreatedAt

/**
 * Indicates that the annotated property is a timestamp to be set at creation time and update time.
 *
 * The type of the annotated property must be one of the following:
 * - [LocalDateTime]
 * - [OffsetDateTime]
 * - A value class whose property type is [LocalDateTime] or [OffsetDateTime].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperUpdatedAt

/**
 * Indicates that the annotated property is not mapped to a column.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperIgnore

/**
 * Indicates that the annotated property is numbered using the sequence.
 *
 * The type of the annotated property must be one of the following:
 * - [Int]
 * - [Long]
 * - [UInt]
 * - A value class whose property type is one of [Int], [Long] or [UInt].
 *
 * @property name the sequence name
 * @property startWith the initial value of the sequence
 * @property incrementBy the increment value of the sequence
 * @property catalog the catalog of the sequence
 * @property schema the schema of the sequence
 * @property alwaysQuote whether to quote the [name], [catalog] and [schema]
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperSequence(
    val name: String,
    val startWith: Int = START_WITH,
    val incrementBy: Int = INCREMENT_BY,
    val catalog: String = CATALOG,
    val schema: String = SCHEMA,
    val alwaysQuote: Boolean = ALWAYS_QUOTE
) {
    companion object {
        const val START_WITH: Int = 1
        const val INCREMENT_BY: Int = 50
        const val CATALOG: String = ""
        const val SCHEMA: String = ""
        const val ALWAYS_QUOTE: Boolean = false
    }
}

/**
 * Indicates that the annotated property is an auto-increment column.
 *
 * The type of the annotated property must be one of the following:
 * - [Int]
 * - [Long]
 * - [UInt]
 * - A value class whose property type is one of [Int], [Long] or [UInt].
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperAutoIncrement

/**
 * Indicates that the annotated class is an entity definition class.
 *
 * The annotated class and the entity class must be data classes.
 *
 * @property entity the entity class
 * @property aliases the names of the entity metamodel instances
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntityDef(
    val entity: KClass<*>,
    val aliases: Array<String> = []
)
