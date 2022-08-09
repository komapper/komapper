package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal open class AnnotationVisitor : KSEmptyVisitor<Unit, KSAnnotation?>() {
    override fun defaultHandler(node: KSNode, data: Unit): KSAnnotation? {
        return null
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit): KSAnnotation? {
        return annotation
    }
}
