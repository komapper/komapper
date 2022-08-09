package org.komapper.processor

import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.visitor.KSEmptyVisitor

open class TypeParameterVisitor : KSEmptyVisitor<Unit, KSTypeParameter?>() {
    override fun defaultHandler(node: KSNode, data: Unit): KSTypeParameter? {
        return null
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit): KSTypeParameter? {
        return typeParameter
    }
}
