package org.komapper.core.dsl.runner

/**
 * @param message the message
 * @param cause the cause
 */
class ValueExtractingException(message: String, cause: Exception) :
    RuntimeException(message, cause)
