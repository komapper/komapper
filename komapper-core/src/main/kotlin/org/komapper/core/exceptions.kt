package org.komapper.core

import java.sql.SQLException

/**
 * Thrown if an optimistic lock is failed.
 */
class OptimisticLockException : Exception()

/**
 * Thrown if an unique constraint is violated.
 *
 * @param cause the cause exception
 */
class UniqueConstraintException(cause: SQLException) : Exception(cause)
