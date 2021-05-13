package org.komapper.core

import java.lang.annotation.Inherited

@DslMarker
annotation class Scope

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@Inherited
annotation class ThreadSafe
