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

    /**
     * The enum type itself is mapped to database.
     * 
     * You need to register a user-defined data type corresponding to the Enum type. 
     * See [User-defined data types](https://www.komapper.org/docs/reference/data-type/#user-defined-data-types).
     * See [JDBC example](https://github.com/komapper/komapper/blob/main/integration-test-jdbc/src/main/kotlin/integration/jdbc/MoodType.kt).
     * See [R2DBC example](https://github.com/komapper/komapper/blob/main/integration-test-r2dbc/src/main/kotlin/integration/r2dbc/MoodType.kt).
     */
    TYPE,
}
