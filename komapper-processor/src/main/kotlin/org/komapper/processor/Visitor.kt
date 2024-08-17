package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal open class AnnotationVisitor : KSEmptyVisitor<Unit, KSAnnotation?>() {
    override fun defaultHandler(node: KSNode, data: Unit): KSAnnotation? {
        return null
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit): KSAnnotation? {
        return annotation
    }
}

internal open class ClassDeclarationVisitor : KSEmptyVisitor<Unit, KSClassDeclaration?>() {
    override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration? {
        return null
    }
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit): KSClassDeclaration? {
        return classDeclaration
    }
}

open class TypeParameterVisitor : KSEmptyVisitor<Unit, KSTypeParameter?>() {
    override fun defaultHandler(node: KSNode, data: Unit): KSTypeParameter? {
        return null
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit): KSTypeParameter? {
        return typeParameter
    }
}
