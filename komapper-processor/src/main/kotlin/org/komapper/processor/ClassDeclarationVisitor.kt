package org.komapper.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal open class ClassDeclarationVisitor : KSEmptyVisitor<Unit, KSClassDeclaration?>() {
    override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration? {
        return null
    }
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): KSClassDeclaration? {
        return classDeclaration
    }
}
