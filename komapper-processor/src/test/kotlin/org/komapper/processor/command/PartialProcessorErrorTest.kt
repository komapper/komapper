package org.komapper.processor.command

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Tag
import org.komapper.processor.AbstractKspTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("slow")
class PartialProcessorErrorTest : AbstractKspTest(PartialProcessorProvider()) {
    @Test
    fun `The class must not have type parameters`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("")
            class Execute<T>(val id: Int)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Execute\" must not have type parameters."))
    }

    @Test
    fun `The expression eval result must be a Boolean - if directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/*%if 1 *//*%end*/")
            class Execute(val id: Int)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The expression must be a Boolean"))
    }

    @Test
    fun `The expression eval result must be a Boolean - elseif directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/*%if true *//*%elseif 1 *//*%end*/")
            class Execute(val id: Int)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The expression must be a Boolean"))
    }

    @Test
    fun `The expression must be Iterable - bind directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* value */('a', 'b')")
            class Execute(val value: String)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The expression must be Iterable"))
    }

    @Test
    fun `The expression must be Iterable - for directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/*%for each in s *//*%end*/")
            class Execute(val s: String)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The expression must be Iterable"))
    }

    @Test
    fun `Specifying a star projection for Iterable is not supported`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/*%for each in list *//*%end*/")
            class Execute(val list: List<*>)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Specifying a star projection for Iterable is not supported"))
    }

    @Test
    fun `Cannot perform the logical operator because the operand is not Boolean`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* !value */'a'")
            class Execute(val value: String)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot perform the logical operator because the operand is not Boolean"))
    }

    @Test
    fun `Cannot perform the logical operator because either operands is not Boolean`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* a && b */'a'")
            class Execute(val a: String, val b: Boolean)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot perform the logical operator because either operands is not Boolean"))
    }

    @Test
    fun `Cannot compare because the operands are not the same type`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* a < b */'a'")
            class Execute(val a: String, val b: Boolean)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot compare because the operands(left=String, right=Boolean) are not the same type"))
    }

    @Test
    fun `Cannot compare because operands are not Comparable type`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            data class User(val id: Int)
            @KomapperPartial("/* a < b */'a'")
            class Execute(val a: User, val b: User)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot compare because operands(left=User, right=User) are not Comparable type"))
    }

    @Test
    fun `The parameter is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* a */'' /* d */''")
            class Execute(val a: String, val b: String, val c: String)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The parameter \"d\" is not found at [d]:1:1. Available parameters are: [a, b, c]"))
    }

    @Test
    fun `The property is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* a.unknown */''")
            class Execute(val a: String)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"unknown\" is not found"))
    }

    @Test
    fun `The function is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* a.unknown(b) */''")
            class Execute(val a: String, val b: Int)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The function \"unknown\" (parameter size is 1) is not found"))
    }

    @Test
    fun `The class must not be private`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("/* a */'' /* b */''")
            private class Execute(val a: String, val b: Int)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Execute\" must not be private."))
    }

    @Test
    fun `The enclosing declaration must not be private`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            private class MyClass {
                @KomapperPartial("/* a */'' /* b */''")
                class Execute(val a: String, val b: Int)
            }
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The enclosing declaration \"MyClass\" of the class \"Execute\" must not be private."))
    }

    @Test
    fun `The corresponding end directive is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("order by id, name /*%if true */")
            object OrderBy
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The corresponding end directive is not found"))
    }

    @Test
    fun `The partial directive is not supported`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("order by id, name /*> nestedPartial */")
            object OrderBy
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The partial directive \"/*> nestedPartial */\" is not supported"))
    }
}
