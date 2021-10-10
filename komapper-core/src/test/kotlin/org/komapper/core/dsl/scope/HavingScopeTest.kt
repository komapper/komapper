package org.komapper.core.dsl.scope

import org.komapper.core.dsl.declaration.HavingDeclaration
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.metamodel.PropertyMetamodelStub
import org.komapper.core.dsl.operator.and
import org.komapper.core.dsl.operator.or
import org.komapper.core.dsl.operator.plus
import kotlin.test.Test
import kotlin.test.assertEquals

internal class HavingScopeTest {

    @Test
    fun plus() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val h1: HavingDeclaration = { p1.eq(1) }
        val h2: HavingDeclaration = { p2.greater("a") }
        val h3 = h1 + h2
        val scope = HavingScope().apply(h3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")), scope[1])
    }

    @Test
    fun and() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val h1: HavingDeclaration = { p1.eq(1) }
        val h2: HavingDeclaration = { p2.greater("a") }
        val h3 = h1 and h2
        val scope = HavingScope().apply(h3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.And(listOf(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")))), scope[1])
    }

    @Test
    fun or() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val h1: HavingDeclaration = { p1.eq(1) }
        val h2: HavingDeclaration = { p2.greater("a") }
        val h3 = h1 or h2
        val scope = HavingScope().apply(h3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.Or(listOf(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")))), scope[1])
    }
}
