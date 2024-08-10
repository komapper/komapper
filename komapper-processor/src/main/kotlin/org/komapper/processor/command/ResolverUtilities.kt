package org.komapper.processor.command

import com.google.devtools.ksp.processing.Resolver

val Resolver.extensions: Extensions get() = Extensions(this)

class Extensions(private val resolver: Resolver) {
    val comparableType = resolver.getKSNameFromString("kotlin.Comparable").let {
        resolver.getClassDeclarationByName(it)?.asStarProjectedType() ?: error("Class not found: ${it.asString()}")
    }
}
