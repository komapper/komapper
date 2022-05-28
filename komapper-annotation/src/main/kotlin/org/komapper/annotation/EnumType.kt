package org.komapper.annotation

/**
 * Indicates the mapping strategy for an enum class.
 */
enum class EnumType {
    /**
     * The value of `Enum.name` is written to database.
     */
    NAME,

    /**
     * The value of `Enum.ordinal` is written to database.
     */
    ORDINAL,
}
