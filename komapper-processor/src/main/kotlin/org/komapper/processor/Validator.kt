package org.komapper.processor

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier

internal fun validateContainerClass(classDeclaration: KSClassDeclaration, recipient: KSNode) {
    val modifiers = classDeclaration.modifiers
    if (!modifiers.contains(Modifier.DATA)) {
        report("The class \"${classDeclaration.simpleName.asString()}\" must be a data class.", recipient)
    }
    if (classDeclaration.typeParameters.isNotEmpty()) {
        report("The class \"${classDeclaration.simpleName.asString()}\" must not have type parameters.", recipient)
    }
    if (classDeclaration.isPrivate()) {
        report("The class \"${classDeclaration.simpleName.asString()}\" must not be private.", recipient)
    }
    validateEnclosingDeclaration(classDeclaration, classDeclaration.parentDeclaration, recipient)
}

private fun validateEnclosingDeclaration(enclosed: KSDeclaration, enclosing: KSDeclaration?, recipient: KSNode) {
    if (enclosing == null) return
    if (!enclosing.isPublic()) {
        val enclosingName = enclosing.simpleName.asString()
        val enclosedName = enclosed.simpleName.asString()
        report("The enclosing declaration \"$enclosingName\" of the class \"$enclosedName\" must be public.", recipient)
    }
    validateEnclosingDeclaration(enclosed, enclosing.parentDeclaration, recipient)
}
