package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver

internal interface ContextFactory {
    fun create(resolver: Resolver): Context
}
