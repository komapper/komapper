package org.komapper.core.dsl.expression

import org.komapper.core.ThreadSafe
import kotlin.reflect.KClass

/**
 * Represents a table expression in the DSL.
 * This interface provides methods to access various properties of the table.
 *
 * @param T The type of the entity associated with the table.
 */
@ThreadSafe
interface TableExpression<T : Any> {
    /**
     * Returns the KClass of the entity.
     *
     * @return The KClass of the entity.
     */
    fun klass(): KClass<T>

    /**
     * Returns the name of the table.
     *
     * @return The name of the table.
     */
    fun tableName(): String

    /**
     * Returns the name of the catalog.
     *
     * @return The name of the catalog.
     */
    fun catalogName(): String

    /**
     * Returns the name of the schema.
     *
     * @return The name of the schema.
     */
    fun schemaName(): String

    /**
     * Returns whether the table name should be quoted.
     *
     * @return `true` if the table name should be quoted, `false` otherwise.
     */
    fun alwaysQuote(): Boolean

    /**
     * Returns whether the sequence assignment is disabled.
     *
     * @return `true` if the sequence assignment is disabled, `false` otherwise.
     */
    fun disableSequenceAssignment(): Boolean

    /**
     * Returns whether the auto increment is disabled.
     *
     * @return `true` if the auto increment is disabled, `false` otherwise.
     */
    fun disableAutoIncrement(): Boolean = false

    /**
     * Returns the canonical table name.
     *
     * @param enquote The function to quote the table name.
     * @return The canonical table name.
     */
    fun getCanonicalTableName(enquote: (String) -> String): String {
        val transform = if (alwaysQuote()) {
            enquote
        } else {
            { it }
        }
        return listOf(catalogName(), schemaName(), tableName())
            .filter { it.isNotBlank() }.joinToString(".", transform = transform)
    }
}
