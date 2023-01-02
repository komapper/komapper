package org.komapper.annotation

/**
 * Indicates the mapping strategy for an enum class.
 */
enum class EnumType {
    /**
     * The value of `Enum.name` is mapped to database.
     */
    NAME,

    /**
     * The value of `Enum.ordinal` is mapped to database.
     */
    ORDINAL,

    /**
     * The value of arbitrary Enum property is mapped to database.
     */
    PROPERTY,
}
