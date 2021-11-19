package org.komapper.core.dsl.scope

import org.komapper.core.dsl.context.SubqueryContext
import org.komapper.core.dsl.expression.Criterion
import org.komapper.core.dsl.expression.EscapeExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.expression.SubqueryExpression
import org.komapper.core.dsl.expression.WhereDeclaration
import org.komapper.core.dsl.metamodel.PropertyMetamodelStub
import org.komapper.core.dsl.operator.and
import org.komapper.core.dsl.operator.or
import org.komapper.core.dsl.operator.plus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.test.fail

internal class WhereScopeTest {

    @Test
    fun plus() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val w1: WhereDeclaration = { p1.eq(1) }
        val w2: WhereDeclaration = { p2.greater("a") }
        val w3 = w1 + w2
        val scope = WhereScope().apply(w3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")), scope[1])
    }

    @Test
    fun and() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val w1: WhereDeclaration = { p1.eq(1) }
        val w2: WhereDeclaration = { p2.greater("a") }
        val w3 = w1.and(w2)
        val scope = WhereScope().apply(w3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.And(listOf(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")))), scope[1])
    }

    @Test
    fun or() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, String>()
        val w1: WhereDeclaration = { p1.eq(1) }
        val w2: WhereDeclaration = { p2.greater("a") }
        val w3 = w1.or(w2)
        val scope = WhereScope().apply(w3)
        assertEquals(2, scope.size)
        assertEquals(Criterion.Eq(Operand.Column(p1), Operand.Argument(p1, 1)), scope[0])
        assertEquals(Criterion.Or(listOf(Criterion.Greater(Operand.Column(p2), Operand.Argument(p2, "a")))), scope[1])
    }

    @Test
    fun eq() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 eq p1
            p1 eq 1
            1 eq p1
        }
        val criteria = scope.toList()
        assertEquals(3, criteria.size)
        assertIs<Criterion.Eq>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Column>(right)
        }
        assertIs<Criterion.Eq>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right)
        }
        assertIs<Criterion.Eq>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Argument<*, *>>(left)
            assertIs<Operand.Column>(right)
        }
    }

    @Test
    fun notEq() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 notEq p1
            p1 notEq 1
            1 notEq p1
        }
        val criteria = scope.toList()
        assertEquals(3, criteria.size)
        assertIs<Criterion.NotEq>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Column>(right)
        }
        assertIs<Criterion.NotEq>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right)
        }
        assertIs<Criterion.NotEq>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Argument<*, *>>(left)
            assertIs<Operand.Column>(right)
        }
    }

    @Test
    fun less() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 less p1
            p1 less 1
            1 less p1
        }
        val criteria = scope.toList()
        assertEquals(3, criteria.size)
        assertIs<Criterion.Less>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Column>(right)
        }
        assertIs<Criterion.Less>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right)
        }
        assertIs<Criterion.Less>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Argument<*, *>>(left)
            assertIs<Operand.Column>(right)
        }
    }

    @Test
    fun lessEq() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 lessEq p1
            p1 lessEq 1
            1 lessEq p1
        }
        val criteria = scope.toList()
        assertEquals(3, criteria.size)
        assertIs<Criterion.LessEq>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Column>(right)
        }
        assertIs<Criterion.LessEq>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right)
        }
        assertIs<Criterion.LessEq>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Argument<*, *>>(left)
            assertIs<Operand.Column>(right)
        }
    }

    @Test
    fun greater() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 greater p1
            p1 greater 1
            1 greater p1
        }
        val criteria = scope.toList()
        assertEquals(3, criteria.size)
        assertIs<Criterion.Greater>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Column>(right)
        }
        assertIs<Criterion.Greater>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right)
        }
        assertIs<Criterion.Greater>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Argument<*, *>>(left)
            assertIs<Operand.Column>(right)
        }
    }

    @Test
    fun greaterEq() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 greaterEq p1
            p1 greaterEq 1
            1 greaterEq p1
        }
        val criteria = scope.toList()
        assertEquals(3, criteria.size)
        assertIs<Criterion.GreaterEq>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Column>(right)
        }
        assertIs<Criterion.GreaterEq>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right)
        }
        assertIs<Criterion.GreaterEq>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Argument<*, *>>(left)
            assertIs<Operand.Column>(right)
        }
    }

    @Test
    fun like() {
        val p1 = PropertyMetamodelStub<Nothing, String>()
        val scope = WhereScope().apply {
            p1 like ""
            p1 startsWith ""
            p1 contains ""
            p1 endsWith ""
        }
        val criteria = scope.toList()
        assertEquals(4, criteria.size)
        assertIs<Criterion.Like>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Text>(right)
        }
        assertIs<Criterion.Like>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Composite>(right)
        }
        assertIs<Criterion.Like>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Composite>(right)
        }
        assertIs<Criterion.Like>(criteria[3]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Composite>(right)
        }
    }

    @Test
    fun notLike() {
        val p1 = PropertyMetamodelStub<Nothing, String>()
        val scope = WhereScope().apply {
            p1 notLike ""
            p1 notStartsWith ""
            p1 notContains ""
            p1 notEndsWith ""
        }
        val criteria = scope.toList()
        assertEquals(4, criteria.size)
        assertIs<Criterion.NotLike>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Text>(right)
        }
        assertIs<Criterion.NotLike>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Composite>(right)
        }
        assertIs<Criterion.NotLike>(criteria[2]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Composite>(right)
        }
        assertIs<Criterion.NotLike>(criteria[3]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<EscapeExpression.Composite>(right)
        }
    }

    @Test
    fun between() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 between 1..10
        }
        val criteria = scope.toList()
        assertEquals(1, criteria.size)
        assertIs<Criterion.Between>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right.first)
            assertIs<Operand.Argument<*, *>>(right.second)
        }
    }

    @Test
    fun notBetween() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val scope = WhereScope().apply {
            p1 notBetween 1..10
        }
        val criteria = scope.toList()
        assertEquals(1, criteria.size)
        assertIs<Criterion.NotBetween>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertIs<Operand.Argument<*, *>>(right.first)
            assertIs<Operand.Argument<*, *>>(right.second)
        }
    }

    @Test
    fun inList() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val subquery = object : SubqueryExpression<Int?> {
            override val context: SubqueryContext
                get() = fail()
        }
        val scope = WhereScope().apply {
            p1 inList listOf(1, 2)
            p1 inList { subquery }
        }
        val criteria = scope.toList()
        assertEquals(2, criteria.size)
        assertIs<Criterion.InList>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertTrue(right.all { it is Operand.Argument<*, *> })
        }
        assertIs<Criterion.InSubQuery>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertEquals(subquery, right)
        }
    }

    @Test
    fun notInList() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val subquery = object : SubqueryExpression<Int?> {
            override val context: SubqueryContext
                get() = fail()
        }
        val scope = WhereScope().apply {
            p1 notInList listOf(1, 2)
            p1 notInList { subquery }
        }
        val criteria = scope.toList()
        assertEquals(2, criteria.size)
        assertIs<Criterion.NotInList>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertTrue(right.all { it is Operand.Argument<*, *> })
        }
        assertIs<Criterion.NotInSubQuery>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left)
            assertEquals(subquery, right)
        }
    }

    @Test
    fun inList2() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, Int>()
        val subquery = object : SubqueryExpression<Pair<Int?, Int?>> {
            override val context: SubqueryContext
                get() = fail()
        }
        val scope = WhereScope().apply {
            (p1 to p2) inList2 listOf(1 to 2)
            (p1 to p2) inList2 { subquery }
        }
        val criteria = scope.toList()
        assertEquals(2, criteria.size)
        assertIs<Criterion.InList2>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left.first)
            assertIs<Operand.Column>(left.second)
            assertTrue(
                right.all {
                    it.first is Operand.Argument<*, *>
                    it.second is Operand.Argument<*, *>
                }
            )
        }
        assertIs<Criterion.InSubQuery2>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left.first)
            assertIs<Operand.Column>(left.second)
            assertEquals(subquery, right)
        }
    }

    @Test
    fun notInList2() {
        val p1 = PropertyMetamodelStub<Nothing, Int>()
        val p2 = PropertyMetamodelStub<Nothing, Int>()
        val subquery = object : SubqueryExpression<Pair<Int?, Int?>> {
            override val context: SubqueryContext
                get() = fail()
        }
        val scope = WhereScope().apply {
            (p1 to p2) notInList2 listOf(1 to 2)
            (p1 to p2) notInList2 { subquery }
        }
        val criteria = scope.toList()
        assertEquals(2, criteria.size)
        assertIs<Criterion.NotInList2>(criteria[0]).let { (left, right) ->
            assertIs<Operand.Column>(left.first)
            assertIs<Operand.Column>(left.second)
            assertTrue(
                right.all {
                    it.first is Operand.Argument<*, *>
                    it.second is Operand.Argument<*, *>
                }
            )
        }
        assertIs<Criterion.NotInSubQuery2>(criteria[1]).let { (left, right) ->
            assertIs<Operand.Column>(left.first)
            assertIs<Operand.Column>(left.second)
            assertEquals(subquery, right)
        }
    }

    @Test
    fun exists() {
        val subquery = object : SubqueryExpression<Int?> {
            override val context: SubqueryContext
                get() = fail()
        }
        val scope = WhereScope().apply {
            exists { subquery }
        }
        val criteria = scope.toList()
        assertEquals(1, criteria.size)
        assertIs<Criterion.Exists>(criteria[0])
    }

    @Test
    fun notExists() {
        val subquery = object : SubqueryExpression<Int?> {
            override val context: SubqueryContext
                get() = fail()
        }
        val scope = WhereScope().apply {
            notExists { subquery }
        }
        val criteria = scope.toList()
        assertEquals(1, criteria.size)
        assertIs<Criterion.NotExists>(criteria[0])
    }
}
