package org.komapper.template

import org.junit.jupiter.api.Nested
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.Value
import org.komapper.core.template.expression.ExprContext
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.expression.NoCacheExprNodeFactory
import kotlin.jvm.java
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.to

class ExprEvaluatorTest {
    private val extensions = TemplateBuiltinExtensions { it }

    private val evaluator = DefaultExprEvaluator(
        NoCacheExprNodeFactory(),
        DefaultExprEnvironment(mapOf("global" to Value("hello", typeOf<String>()))),
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
        NORTH,
        SOUTH,
        WEST,
        EAST,
    }

    sealed interface Color {
        object Red : Color
        object Green : Color
        object Blue : Color
    }

    @Nested
    inner class LiteralTest {
        @Test
        fun nullLiteral() {
            val ctx = ExprContext(mapOf("a" to Value(null, typeOf<Any>())), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(null, typeOf<Any>()), result)
        }

        @Test
        fun trueLiteral() {
            val ctx = ExprContext(mapOf("a" to Value(true, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun falseLiteral() {
            val ctx = ExprContext(mapOf("a" to Value(false, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun stringLiteral() {
            val ctx = ExprContext(mapOf("a" to Value("abc", typeOf<String>())), extensions)
            val result = evaluator.eval("a", ctx)
            assertEquals(Value("abc", typeOf<String>()), result)
        }
    }

    @Nested
    inner class ComparisonOperatorTest {
        @Test
        fun eq1() {
            val ctx = ExprContext(mapOf("a" to Value(1, typeOf<Int>())), extensions)
            val result = evaluator.eval("a == 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun eq2() {
            val ctx = ExprContext(mapOf("a" to Value(2, typeOf<Int>())), extensions)
            val result = evaluator.eval("a == 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun ne1() {
            val ctx = ExprContext(mapOf("a" to Value(0, typeOf<Int>())), extensions)
            val result = evaluator.eval("a != 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun ne2() {
            val ctx = ExprContext(mapOf("a" to Value(1, typeOf<Int>())), extensions)
            val result = evaluator.eval("a != 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun ge1() {
            val ctx = ExprContext(mapOf("a" to Value(2, typeOf<Int>())), extensions)
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun ge2() {
            val ctx = ExprContext(mapOf("a" to Value(2, typeOf<Int>())), extensions)
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun ge3() {
            val ctx = ExprContext(mapOf("a" to Value(0, typeOf<Int>())), extensions)
            val result = evaluator.eval("a >= 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun gt1() {
            val ctx = ExprContext(mapOf("a" to Value(2, typeOf<Int>())), extensions)
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun gt2() {
            val ctx = ExprContext(mapOf("a" to Value(1, typeOf<Int>())), extensions)
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun gt3() {
            val ctx = ExprContext(mapOf("a" to Value(0, typeOf<Int>())), extensions)
            val result = evaluator.eval("a > 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun le1() {
            val ctx = ExprContext(mapOf("a" to Value(2, typeOf<Int>())), extensions)
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun le2() {
            val ctx = ExprContext(mapOf("a" to Value(1, typeOf<Int>())), extensions)
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun le3() {
            val ctx = ExprContext(mapOf("a" to Value(0, typeOf<Int>())), extensions)
            val result = evaluator.eval("a <= 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun lt1() {
            val ctx = ExprContext(mapOf("a" to Value(2, typeOf<Int>())), extensions)
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun lt2() {
            val ctx = ExprContext(mapOf("a" to Value(1, typeOf<Int>())), extensions)
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun lt3() {
            val ctx = ExprContext(mapOf("a" to Value(0, typeOf<Int>())), extensions)
            val result = evaluator.eval("a < 1", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun is1() {
            val red: Color = Color.Red
            val ctx = ExprContext(mapOf("a" to Value(red, typeOf<Color>())), extensions)
            val result = evaluator.eval("a is @org.komapper.template.ExprEvaluatorTest\$Color\$Red@", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun is2() {
            val ctx = ExprContext(mapOf("a" to Value(Color.Red, typeOf<Color.Red>())), extensions)
            val result = evaluator.eval("a is @org.komapper.template.ExprEvaluatorTest@", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun as1() {
            val red: Color = Color.Red
            val ctx = ExprContext(mapOf("a" to Value(red, typeOf<Color>())), extensions)
            val result = evaluator.eval("a as @org.komapper.template.ExprEvaluatorTest\$Color\$Red@", ctx)
            assertEquals(Value(Color.Red, typeOf<Color.Red>()), result)
        }

        @Test
        fun as2() {
            val ctx = ExprContext(mapOf("a" to Value(Color.Red, typeOf<Color.Red>())), extensions)
            val ex = assertFailsWith<TypeCastException> {
                evaluator.eval("a as @org.komapper.template.ExprEvaluatorTest@", ctx)
            }
            println(ex)
        }

        @Test
        fun `is - The right operand must be a class reference`() {
            val ctx = ExprContext(mapOf("a" to Value(Color.Red, typeOf<Color.Red>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a is 1", ctx)
            }
            println(exception)
        }

        @Test
        fun `as - The right operand must be a class reference`() {
            val ctx = ExprContext(mapOf("a" to Value(Color.Red, typeOf<Color.Red>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a as 1", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the left operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, typeOf<Any>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a > 1", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the right operand is null`() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, typeOf<Any>())),
                extensions,
            )
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("1 > a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot compare because the operands are not comparable to each other`() {
            val ctx = ExprContext(mapOf("a" to Value("string", typeOf<String>())), extensions)
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
            val ctx = ExprContext(mapOf("a" to Value(false, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("!a", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun not_false() {
            val ctx = ExprContext(mapOf("a" to Value(true, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("!a", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun and_true() {
            val ctx = ExprContext(mapOf("a" to Value(true, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("a && true", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun and_false() {
            val ctx = ExprContext(mapOf("a" to Value(false, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("a && true", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun or_true() {
            val ctx = ExprContext(mapOf("a" to Value(true, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("a || false", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun or_false() {
            val ctx = ExprContext(mapOf("a" to Value(false, typeOf<Boolean>())), extensions)
            val result = evaluator.eval("a || false", ctx)
            assertEquals(Value(false, typeOf<Boolean>()), result)
        }

        @Test
        fun `Cannot perform the logical operator because the left operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, typeOf<Any>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a && true", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the right operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, typeOf<Any>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("true && a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because either operand is not boolean`() {
            val ctx = ExprContext(mapOf("a" to Value("string", typeOf<String>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("true && a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is null`() {
            val ctx = ExprContext(mapOf("a" to Value(null, typeOf<Any>())), extensions)
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("!a", ctx)
            }
            println(exception)
        }

        @Test
        fun `Cannot perform the logical operator because the operand is not Boolean`() {
            val ctx = ExprContext(mapOf("a" to Value("string", typeOf<String>())), extensions)
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
            assertEquals(Value(Hello, typeOf<Hello.Companion>()), result)
        }

        @Test
        fun companionObject_property() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@.name", ctx)
            assertEquals(Value(Hello.name, typeOf<String>()), result)
        }

        @Test
        fun companionObject_constProperty() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@.constName", ctx)
            assertEquals(Value(Hello.constName, typeOf<String>()), result)
        }

        @Test
        fun companionObject_function() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hello::class.java.name}@.greet(\"abc\")", ctx)
            assertEquals(Value("hello abc!", typeOf<String>()), result)
        }

        @Test
        fun `object`() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@", ctx)
            assertEquals(Value(Hi, typeOf<Hi>()), result)
        }

        @Test
        fun object_property() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@.name", ctx)
            assertEquals(Value(Hi.name, typeOf<String>()), result)
        }

        @Test
        fun object_constProperty() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@.constName", ctx)
            assertEquals(Value(Hi.constName, typeOf<String>()), result)
        }

        @Test
        fun object_function() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Hi::class.java.name}@.greet(\"abc\")", ctx)
            assertEquals(Value("hi abc!", typeOf<String>()), result)
        }

        @Test
        fun enum_element() {
            val ctx = ExprContext(emptyMap(), extensions)
            val result = evaluator.eval("@${Direction::class.java.name}@.WEST", ctx)
            assertEquals(Value(Direction.WEST, typeOf<Direction>()), result)
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
            assertEquals(Value("hello", typeOf<String>()), result)
        }

        @Test
        fun callable0() {
            val ctx = ExprContext(
                mapOf("h" to Value(::hello0, typeOf<Function<String>>())),
                extensions,
            )
            val result = evaluator.eval("h()", ctx)
            assertEquals(Value("hello world", typeOf<String>()), result)
        }

        @Test
        fun callable1() {
            val ctx = ExprContext(
                mapOf("h" to Value(::hello1, typeOf<Function<String>>())),
                extensions,
            )
            val result = evaluator.eval("h(\"WORLD\")", ctx)
            assertEquals(Value("hello WORLD", typeOf<String>()), result)
        }

        @Test
        fun callable2() {
            val ctx = ExprContext(
                mapOf("add" to Value(::add, typeOf<Function<Int>>())),
                extensions,
            )
            val result = evaluator.eval("add(1, 2)", ctx)
            assertEquals(Value(3, typeOf<Int>()), result)
        }

        @Test
        fun `The template variable is not bound to a value`() {
            val ctx = ExprContext(emptyMap(), extensions)
            val ex = assertFailsWith<ExprException> {
                evaluator.eval("a", ctx)
            }
            assertEquals(
                "The template variable \"a\" is not bound to a value. Make sure the variable name is correct at [a]:1:1",
                ex.message
            )
        }
    }

    @Nested
    inner class PropertyTest {
        @Test
        fun property() {
            val ctx = ExprContext(
                mapOf(
                    "p" to Value(
                        Person(1, "aaa", 20),
                        typeOf<Person>(),
                    ),
                ),
                extensions,
            )
            val result = evaluator.eval("p.name", ctx)
            assertEquals(Value("aaa", typeOf<String>()), result)
        }

        @Test
        fun property_nullable() {
            val ctx = ExprContext(
                mapOf(
                    "p" to Value(
                        Person(1, "aaa", null),
                        typeOf<Person>(),
                    ),
                ),
                extensions,
            )
            val result = evaluator.eval("p.age", ctx)
            assertEquals(Value(null, typeOf<Int>()), result)
        }

        @Test
        fun safeCall() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, typeOf<String>())),
                extensions,
            )
            val result = evaluator.eval("a?.length", ctx)
            assertEquals(Value(null, typeOf<Int>()), result)
        }

        @Test
        fun extensionProperty() {
            val ctx = ExprContext(
                mapOf("a" to Value("abc", typeOf<String>())),
                extensions,
            )
            val result = evaluator.eval("a.lastIndex", ctx)
            assertEquals(Value(2, typeOf<Int>()), result)
        }

        @Test
        fun `Failed to call the property`() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, typeOf<String>())),
                extensions,
            )
            val exception = assertFailsWith<ExprException> {
                evaluator.eval("a.length", ctx)
            }
            println(exception)
        }

        @Test
        fun `The property is not found`() {
            val ctx = ExprContext(
                mapOf("a" to Value("string", typeOf<String>())),
                extensions,
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
                mapOf("h" to Value(Hello(), typeOf<Hello>()), "w" to Value("world", typeOf<String>())),
                extensions,
            )
            val result = evaluator.eval("h.say(w)", ctx)
            assertEquals(Value("hello world", typeOf<String>()), result)
        }

        @Test
        fun function_2parameter() {
            val ctx = ExprContext(
                mapOf(
                    "h" to Value(Hello(), typeOf<Hello>()),
                    "w" to Value("world", typeOf<String>()),
                    "m" to Value("good luck", typeOf<String>()),
                ),
                extensions,
            )
            val result = evaluator.eval("h.say(w, m)", ctx)
            assertEquals(Value("hello world, good luck", typeOf<String>()), result)
        }

        @Test
        fun safeCall() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, typeOf<String>())),
                extensions,
            )
            val result = evaluator.eval("a?.subSequence(0, 1)", ctx)
            assertEquals(Value(null, typeOf<CharSequence>()), result)
        }

        @Test
        fun extensionFunction() {
            val ctx = ExprContext(
                mapOf("s" to Value("", typeOf<String>())),
                extensions,
            )
            val result = evaluator.eval("s.isBlank()", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun memberExtensionFunction() {
            val ctx = ExprContext(
                mapOf("s" to Value("abc", typeOf<String>())),
                extensions,
            )
            val result = evaluator.eval("s.asPrefix()", ctx)
            assertEquals(Value("abc%", typeOf<String>()), result)
        }

        @Test
        fun `Call an extension function when the receiver is null`() {
            val ctx = ExprContext(
                mapOf("s" to Value(null, typeOf<Any>())),
                extensions,
            )
            val result = evaluator.eval("s.isNullOrEmpty()", ctx)
            assertEquals(Value(true, typeOf<Boolean>()), result)
        }

        @Test
        fun `Failed to call the function`() {
            val ctx = ExprContext(
                mapOf("a" to Value(null, typeOf<String>())),
                extensions,
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
                mapOf("a" to Value("string", typeOf<String>())),
                extensions,
            )
            val exception = assertFailsWith<ExprException> {
                evaluator
                    .eval("a.notFound()", ctx)
            }
            println(exception)
        }
    }

    @Nested
    inner class ValueClassTest {
        @Test
        fun test() {
            val ctx = ExprContext(
                mapOf("uInt" to Value(1u, typeOf<UInt>())),
                extensions,
            )
            val result = evaluator.eval("uInt", ctx)
            assertEquals(Value(1u, typeOf<UInt>()), result)
        }
    }
}

fun hello0(): String {
    return "hello world"
}

fun hello1(name: String): String {
    return "hello $name"
}

fun add(a: Int, b: Int): Int {
    return a + b
}
