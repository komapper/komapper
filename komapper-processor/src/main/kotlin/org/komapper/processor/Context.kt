package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import kotlin.reflect.KClass

internal data class Context(
    private val environment: SymbolProcessorEnvironment,
    val config: Config,
    val resolver: Resolver,
) {
    val logger = KomapperKSPLogger(environment.logger)
    val codeGenerator get() = environment.codeGenerator
}

internal fun Context.getClassDeclaration(name: String, onNotFound: (String) -> Nothing): KSClassDeclaration {
    val ksName = resolver.getKSNameFromString(name)
    return resolver.getClassDeclarationByName(ksName) ?: onNotFound(name)
}

internal fun Context.getClassDeclaration(klass: KClass<*>, onNotFound: (String) -> Nothing): KSClassDeclaration {
    return getClassDeclaration(klass.qualifiedName!!, onNotFound)
}

internal fun Context.getClassDeclaration(name: String, symbol: KSNode): KSClassDeclaration {
    return getClassDeclaration(name) { report("Class not found: $it", symbol) }
}

internal fun Context.getClassDeclaration(klass: KClass<*>, symbol: KSNode): KSClassDeclaration {
    return getClassDeclaration(klass.qualifiedName!!, symbol)
}
