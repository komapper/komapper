package org.komapper.core

import java.lang.annotation.Inherited

@DslMarker
annotation class Scope

/**
 * Indicates that the annotated type and its child types are thread-safe.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Inherited
annotation class ThreadSafe
