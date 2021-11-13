package org.komapper.core.dsl.scope

import org.komapper.core.dsl.declaration.WhenDeclaration
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodelStub
import org.komapper.core.dsl.operator.and
import org.komapper.core.dsl.operator.or
import org.komapper.core.dsl.operator.plus
import kotlin.test.Test
import kotlin.test.assertEquals

internal class WhenScopeTest {

    @Test
    fun plus() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val w1: WhenDeclaration = { p1.eq(1) }
        val w2: WhenDeclaration = { p2.greater("a") }
        val w3 = w1 + w2
        val scope = WhenScope().apply(w3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")), scope[1])
    }

    @Test
    fun and() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val w1: WhenDeclaration = { p1.eq(1) }
        val w2: WhenDeclaration = { p2.greater("a") }
        val w3 = w1.and(w2)
        val scope = WhenScope().apply(w3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.And(listOf(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")))), scope[1])
    }

    @Test
    fun or() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val w1: WhenDeclaration = { p1.eq(1) }
        val w2: WhenDeclaration = { p2.greater("a") }
        val w3 = w1.or(w2)
        val scope = WhenScope().apply(w3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.Or(listOf(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")))), scope[1])
    }
}
