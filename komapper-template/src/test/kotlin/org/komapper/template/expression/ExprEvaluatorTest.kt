package org.komapper.template.expression

import org.junit.jupiter.api.Nested
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExprEvaluatorTest {

    private val extensions = TemplateBuiltinExtensions { it }

    private val evaluator = DefaultExprEvaluator(
        NoCacheExprNodeFactory(),
        DefaultExprEnvironment(mapOf("global" to Value("hello")))
    )

    data class Person(val id: Int, val name: String, val age: Int?)

    @Suppress("UNUSED")
    class Hello {
        fun say(name: String): String {
            return "hello $name"
        }

        fun say(name: String, message: String): String {
            return "hello $name, $message"
        }

        companion object {
            const val name = "hello"

            const val constName = "hello const"

            fun greet(name: String): String {
                return "hello $name!"
            }
        }
    }

    object Hi {
        val name = "hi"

        const val constName = "hi const"

        @Suppress("unused")
        fun greet(name: String): String {
            return "hi $name!"
        }
    }

    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    @Nested
    inner class LiteralTest {
        @Test
        fun nullLiteral() {
            val ctx = ExprContext(mapOf("a" to Value(null, Any::class)), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(null, Any::class), result)
        }

        @Test
        fun trueLiteral() {
            val ctx = ExprContext(mapOf("a" to Value(true)), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun falseLiteral() {
            val ctx = ExprContext(mapOf("a" to Value(false)), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun stringLiteral() {
            val ctx = ExprContext(mapOf("a" to Value("abc")), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value("abc"), result)
        }
    }

    @Nested
    inner class ComparisonOperatorTest {

        @Test
        fun eq1() {
            val ctx = ExprContext(mapOf("a" to Value(1)), extensions)
            val result = evaluator.eval("a == 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun eq2() {
            val ctx = ExprContext(mapOf("a" to Value(2)), extensions)
            val result = evaluator.eval("a == 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun ne1() {
            val ctx = ExprContext(mapOf("a" to Value(0)), extensions)
            val result = evaluator.eval("a != 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun ne2() {
            val ctx = ExprContext(mapOf("a" to Value(1)), extensions)
            val result = evaluator.eval("a != 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun ge1() {
            val ctx = ExprContext(mapOf("a" to Value(2)), extensions)
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun ge2() {
            val ctx = ExprContext(mapOf("a" to Value(2)), extensions)
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun ge3() {
            val ctx = ExprContext(mapOf("a" to Value(0)), extensions)
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun gt1() {
            val ctx = ExprContext(mapOf("a" to Value(2)), extensions)
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun gt2() {
            val ctx = ExprContext(mapOf("a" to Value(1)), extensions)
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun gt3() {
            val ctx = ExprContext(mapOf("a" to Value(0)), extensions)
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun le1() {
            val ctx = ExprContext(mapOf("a" to Value(2)), extensions)
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun le2() {
            val ctx = ExprContext(mapOf("a" to Value(1)), extensions)
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun le3() {
            val ctx = ExprContext(mapOf("a" to Value(0)), extensions)
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun lt1() {
            val ctx = ExprContext(mapOf("a" to Value(2)), extensions)
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun lt2() {
            val ctx = ExprContext(mapOf("a" to Value(1)), extensions)
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun lt3() {
            val ctx = ExprContext(mapOf("a" to Value(0)), extensions)
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun `Cannot compare because the left operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, Any::class)), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a > 1", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the right operand is null`() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, Any::class)), extensions
            )
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("1 > a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the operands are not comparable to each other`() {
            val ctx = ExprContext(mapOf("a" to Value("string")), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a > 1", ctx)
            }
            println(exception)
        }
    }

    @Nested
    inner class LogicalOperatorTest {
        @Test
        fun not_true() {
            val ctx = ExprContext(mapOf("a" to Value(false)), extensions)
            val result = evaluator.eval("!a", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun not_false() {
            val ctx = ExprContext(mapOf("a" to Value(true)), extensions)
            val result = evaluator.eval("!a", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun and_true() {
            val ctx = ExprContext(mapOf("a" to Value(true)), extensions)
            val result = evaluator.eval("a && true", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun and_false() {
            val ctx = ExprContext(mapOf("a" to Value(false)), extensions)
            val result = evaluator.eval("a && true", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun or_true() {
            val ctx = ExprContext(mapOf("a" to Value(true)), extensions)
            val result = evaluator.eval("a || false", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun or_false() {
            val ctx = ExprContext(mapOf("a" to Value(false)), extensions)
            val result = evaluator.eval("a || false", ctx)
            assertEquals(Value(false), result)
        }

        @Test
        fun `Cannot perform the logical operator because the left operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, Any::class)), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a && true", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the right operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, Any::class)), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("true && a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because either operand is not boolean`() {
            val ctx = ExprContext(mapOf("a" to Value("string")), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("true && a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, Any::class)), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("!a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is not Boolean`() {
            val ctx = ExprContext(mapOf("a" to Value("string")), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("!a", ctx)
            }
            println(exception)
        }
    }

    @Nested
    inner class ClassRefTest {
        @Test
        fun companionObject() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@", ctx)
            assertEquals(Value(Hello), result)
        }

        @Test
        fun companionObject_property() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@.name", ctx)
            assertEquals(Value(Hello.name), result)
        }

        @Test
        fun companionObject_constProperty() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@.constName", ctx)
            assertEquals(Value(Hello.constName), result)
        }

        @Test
        fun companionObject_function() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@.greet(\"abc\")", ctx)
            assertEquals(Value("hello abc!"), result)
        }

        @Test
        fun `object`() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@", ctx)
            assertEquals(Value(Hi), result)
        }

        @Test
        fun object_property() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@.name", ctx)
            assertEquals(Value(Hi.name), result)
        }

        @Test
        fun object_constProperty() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@.constName", ctx)
            assertEquals(Value(Hi.constName), result)
        }

        @Test
        fun object_function() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@.greet(\"abc\")", ctx)
            assertEquals(Value("hi abc!"), result)
        }

        @Test
        fun enum_element() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Direction::class.java.name}@.WEST", ctx)
            assertEquals(Value(Direction.WEST), result)
        }

        @Suppress("UNCHECKED_CAST")
        @Test
        fun enum_function() {
            val ctx = ExprContext(emptyMap(), extensions)
            val (obj) = evaluator.eval("@${Direction::class.java.name}@.values()", ctx)
            assertContentEquals(Direction.values(), obj as Array<Direction>)
        }
    }

    @Nested
    inner class ValueTest {
        @Test
        fun global() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("global", ctx)
            assertEquals(Value("hello"), result)
        }

        @Test
        fun `The value cannot be resolved`() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(null, Any::class), result)
        }
    }

    @Nested
    inner class PropertyTest {
        @Test
        fun property() {
            val ctx = ExprContext(
                mapOf(
                    "p" to Value(
                        Person(1, "aaa", 20)
                    )
                ),
                extensions
            )
            val result = evaluator.eval("p.name", ctx)
            assertEquals(Value("aaa"), result)
        }

        @Test
        fun property_nullable() {
            val ctx = ExprContext(
                mapOf(
                    "p" to Value(Person(1, "aaa", null))
                ),
                extensions
            )
            val result = evaluator.eval("p.age", ctx)
            assertEquals(Value(null, Int::class), result)
        }

        @Test
        fun safeCall() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, String::class)), extensions
            )
            val result = evaluator.eval("a?.length", ctx)
            assertEquals(Value(null, Int::class), result)
        }

        @Test
        fun extensionProperty() {
            val ctx = ExprContext(
                mapOf("a" to Value("abc")), extensions
            )
            val result = evaluator.eval("a.lastIndex", ctx)
            assertEquals(Value(2), result)
        }

        @Test
        fun `Failed to call the property`() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, String::class)), extensions
            )
            val exception = assertFailsWith<ExprException> {
                evaluator.eval("a.length", ctx)
            }
            println(exception)
        }

        @Test
        fun `The property is not found`() {
            val ctx = ExprContext(
                mapOf("a" to Value("string")), extensions
            )
            val exception = assertFailsWith<ExprException> {
                evaluator.eval("a.notFound", ctx)
            }
            println(exception)
        }
    }

    @Nested
    inner class FunctionTest {
        @Test
        fun function_1parameter() {
            val ctx = ExprContext(
                mapOf("h" to Value(Hello()), "w" to Value("world")), extensions
            )
            val result = evaluator.eval("h.say(w)", ctx)
            assertEquals(Value("hello world"), result)
        }

        @Test
        fun function_2parameter() {
            val ctx = ExprContext(
                mapOf(
                    "h" to Value(Hello(), Hello::class),
                    "w" to Value("world", String::class),
                    "m" to Value("good luck", String::class)
                ),
                extensions
            )
            val result = evaluator.eval("h.say(w, m)", ctx)
            assertEquals(Value("hello world, good luck"), result)
        }

        @Test
        fun safeCall() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, String::class)), extensions
            )
            val result = evaluator.eval("a?.subSequence(0, 1)", ctx)
            assertEquals(Value(null, CharSequence::class), result)
        }

        @Test
        fun extensionFunction() {
            val ctx = ExprContext(
                mapOf("s" to Value("")), extensions
            )
            val result = evaluator.eval("s.isBlank()", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun memberExtensionFunction() {
            val ctx = ExprContext(
                mapOf("s" to Value("abc")), extensions
            )
            val result = evaluator.eval("s.asPrefix()", ctx)
            assertEquals(Value("abc%", String::class), result)
        }

        @Test
        fun `Call an extension function when the receiver is null`() {
            val ctx = ExprContext(
                mapOf("s" to Value(null, Any::class)), extensions
            )
            val result = evaluator.eval("s.isNullOrEmpty()", ctx)
            assertEquals(Value(true), result)
        }

        @Test
        fun `Failed to call the function`() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, String::class)), extensions
            )
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a.subSequence(0, 1)", ctx)
            }
            println(exception)
        }

        @Test
        fun `The function is not found`() {
            val ctx = ExprContext(
                mapOf("a" to Value("string")), extensions
            )
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a.notFound()", ctx)
            }
            println(exception)
        }
    }
}
