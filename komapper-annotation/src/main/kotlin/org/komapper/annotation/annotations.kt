@file:Suppress("unused")

package org.komapper.annotation

import java.time.LocalDateTime
import java.time.OffsetDateTime
import kotlin.reflect.KClass

/**
 * Indicates that the annotated class is an entity.
 * The annotated class must be a data class.
 * @param aliases aliases of the entity metamodel instances
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
 * Indicates that the annotated property is a version.
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
 * Adds table information.
 * @property name the table name
 * @property catalog the catalog name
 * @property schema the schema name
 * @property alwaysQuote whether to quote the [name], [catalog] and [schema]
 */
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

/**
 * Adds column information.
 * @property name the table name
 * @property alwaysQuote whether to quote the [name]
 * @property masking whether to mask the value that corresponds to the annotated property in the logs
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperColumn(
    val name: String = KomapperColumn.name,
    val alwaysQuote: Boolean = KomapperColumn.alwaysQuote,
    val masking: Boolean = KomapperColumn.masking,
) {
    companion object {
        const val name = ""
        const val alwaysQuote = false
        const val masking = false
    }
}

/**
 * Indicates that the annotated property is a timestamp to be set at creation time.
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
 * The type of the annotated property must be one of the following:
 * - [Int]
 * - [Long]
 * - [UInt]
 * - A value class whose property type is one of [Int], [Long] or [UInt].
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

/**
 * Indicates that the annotated property is an auto-increment column.
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
 * Indicates that the annotated class is an entity definition.
 * The annotated class and the entity class must be data classes.
 * @property entity the class defined as an entity
 * @property aliases the aliases of the entity metamodel instances
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntityDef(
    val entity: KClass<*>,
    val aliases: Array<String> = []
)
