package org.komapper.core

/**
 * Thrown if an optimistic lock is failed.
 *
 * @param message the detail message
 */
class OptimisticLockException(message: String) : Exception(message)

/**
 * Thrown if a unique constraint is violated.
 *
 * @param cause the cause exception
 */
class UniqueConstraintException(cause: Exception) : Exception(cause)
