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
 * @property aliases the names of the entity metamodel instances
 * @property unit the unit object class
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntity(
    val aliases: Array<String> = [],
    val unit: KClass<*> = Void::class,
)

/**
 * Indicates that the result of a SELECT query can be projected into an instance of an annotated class.
 * The default function name is a concatenation of "selectAs" and the simple name of the class.
 *
 * @property function the function name for performing projection.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperProjection(
    val function: String = "",
)

/**
 * Indicates that the annotated property is a primary key.
 * @property virtual If `true`, the annotated property does not actually map to a primary key
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperId(
    val virtual: Boolean = false,
)

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
 *
 * The hint property has a different meaning depending on the type.
 * - When the type is [EnumType.NAME], it is ignored.
 * - When the type is [EnumType.ORDINAL], it is ignored.
 * - When the type is [EnumType.PROPERTY], it means a property name of the annotated enum class.
 *
 * @property type the mapping strategy
 * @property hint the hint for the mapping strategy
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEnum(val type: EnumType, val hint: String = HINT) {
    companion object {
        const val HINT: String = ""
    }
}

/**
 * Indicates that the annotated property is an embedded value.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEmbedded

/**
 * Indicates that the annotated property is an embedded value for composite identifiers.
 *
 * @property virtual If `true`, the annotated property does not actually map to composite primary keys
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEmbeddedId(
    val virtual: Boolean = false,
)

/**
 * Used to override the column of an embeddable class`s property.
 *
 * @property name the property name
 * @property column the column
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class KomapperColumnOverride(val name: String, val column: KomapperColumn)

/**
 * Used to override the enum of an embeddable class`s property.
 *
 * @property name the property name
 * @property enum the enum
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class KomapperEnumOverride(val name: String, val enum: KomapperEnum)

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
    val alwaysQuote: Boolean = ALWAYS_QUOTE,
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
 * @property alternateType the alternate type. This type must be a value class.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperColumn(
    val name: String = NAME,
    val alwaysQuote: Boolean = ALWAYS_QUOTE,
    val masking: Boolean = MASKING,
    val alternateType: KClass<*> = Void::class,
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
    val alwaysQuote: Boolean = ALWAYS_QUOTE,
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
 * @property unit the unit object class
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperEntityDef(
    val entity: KClass<*>,
    val aliases: Array<String> = [],
    val unit: KClass<*> = Void::class,
)

/**
 * Indicates that the result of a SELECT query can be projected into an instance of a [projection] class.
 * The default function name is a concatenation of "selectAs" and the simple name of the [projection] class.
 *
 * The [projection] class must be a data class.
 *
 * @property projection the projection class
 * @property function the function name for performing projection
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperProjectionDef(
    val projection: KClass<*>,
    val function: String = "",
)

/**
 * The annotated class is a command that encapsulates an SQL template, SQL parameters, and SQL execution into a single unit.
 *
 * @property sql the SQL template
 * @property disableValidation if `true`, SQL validation at compile time will be disabled
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperCommand(
    val sql: String,
    val disableValidation: Boolean = DISABLE_VALIDATION,
) {
    companion object {
        const val DISABLE_VALIDATION: Boolean = false
    }
}

/**
 * Indicates that the annotated property is not used within [KomapperCommand.sql].
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperUnused

/**
 * Indicates an association link.
 *
 * @property source the source name of an entity metamodel instance. The source is inferred from the annotated class.
 * @property target the target name of an entity metamodel instance. The target is inferred from the targetEntity property of association annotation.
 */
@KomapperExperimentalAssociation
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperLink(
    val source: String = SOURCE,
    val target: String = TARGET,
) {
    companion object {
        const val SOURCE: String = ""
        const val TARGET: String = ""
    }
}

/**
 * Indicates a one-to-one association.
 *
 * @property targetEntity the target entity class. The class must be annotated with [KomapperEntity] or [KomapperEntityDef].
 * @property navigator the function name for navigation. The navigator is inferred from the [KomapperLink.target] value.
 * @property link the association link
 */
@KomapperExperimentalAssociation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class KomapperOneToOne(
    val targetEntity: KClass<*>,
    val navigator: String = NAVIGATOR,
    val link: KomapperLink = KomapperLink(),
) {
    companion object {
        const val NAVIGATOR: String = ""
    }
}

/**
 * Indicates a one-to-many association.
 *
 * @property targetEntity the target entity class. The class must be annotated with [KomapperEntity] or [KomapperEntityDef].
 * @property navigator the function name for navigation. The navigator is inferred from the [KomapperLink.target] value.
 * @property link the association link
 */
@KomapperExperimentalAssociation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class KomapperOneToMany(
    val targetEntity: KClass<*>,
    val navigator: String = NAVIGATOR,
    val link: KomapperLink = KomapperLink(),
) {
    companion object {
        const val NAVIGATOR: String = ""
    }
}

/**
 * Indicates a many-to-one association.
 *
 * @property targetEntity the target entity class. The class must be annotated with [KomapperEntity] or [KomapperEntityDef].
 * @property navigator the function name for navigation. The navigator is inferred from the [KomapperLink.target] value.
 * @property link the association link
 */
@KomapperExperimentalAssociation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Repeatable
annotation class KomapperManyToOne(
    val targetEntity: KClass<*>,
    val navigator: String = NAVIGATOR,
    val link: KomapperLink = KomapperLink(),
) {
    companion object {
        const val NAVIGATOR: String = ""
    }
}

/**
 * Indicates an aggregate root.
 *
 * @property navigator the function name for navigation. The navigator is inferred from the annotated class.
 */
@KomapperExperimentalAssociation
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class KomapperAggregateRoot(
    val navigator: String = NAVIGATOR,
) {
    companion object {
        const val NAVIGATOR: String = ""
    }
}

/**
 * Indicates that the annotated element uses the experimental association API.
 */
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
annotation class KomapperExperimentalAssociation
