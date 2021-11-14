package org.komapper.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal abstract class ClassDeclarationVisitor : KSEmptyVisitor<Unit, KSClassDeclaration>() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): KSClassDeclaration {
        return classDeclaration
    }
}
