package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal class Exit(message: String, val node: KSNode?) : Exception(message) {
    override val message: String
        get() = super.message!!
}

internal fun report(message: String, node: KSNode? = null): Nothing {
    throw Exit(message, node)
}

internal fun <T> Sequence<T>.anyDuplicates(predicate: (T) -> Boolean): Boolean {
    return this.filter(predicate).take(2).count() == 2
}

internal fun KSClassDeclaration.hasCompanionObject(): Boolean {
    return declarations.any { it.accept(CompanionObjectVisitor(), Unit) }
}

private class CompanionObjectVisitor : KSEmptyVisitor<Unit, Boolean>() {
    override fun defaultHandler(node: KSNode, data: Unit): Boolean {
        return false
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): Boolean {
        return classDeclaration.isCompanionObject && classDeclaration.simpleName.asString() == "Companion"
    }
}
