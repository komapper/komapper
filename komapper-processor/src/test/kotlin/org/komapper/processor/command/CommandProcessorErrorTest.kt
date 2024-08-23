package org.komapper.processor.command

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Tag
import org.komapper.processor.AbstractKspTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Tag("slow")
class CommandProcessorErrorTest : AbstractKspTest(CommandProcessorProvider()) {

    @Test
    fun `The class with type parameters is not supported`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("")
            class Execute<T>(val id: Int): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class with type parameters is not supported."))
    }

    @Test
    fun `must extend one of the following classes, One, Many, Exec, ExecReturnOne, or ExecReturnMany`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("")
            class Execute(val id: Int)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("must extend one of the following classes: One, Many, Exec, ExecReturnOne, or ExecReturnMany"))
    }

    @Test
    fun `The const must be annotated with @KomapperPartial`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperPartial("")
            const val partial = ""
            @KomapperCommand("/*> unknownPartial */")
            class Execute(val id: Int): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The const must be annotated with @KomapperPartial"))
    }

    @Test
    fun `The expression eval result must be a Boolean - if directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("/*%if 1 *//*%end*/")
            class Execute(val id: Int): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The expression eval result must be a Boolean"))
    }

    @Test
    fun `The expression eval result must be a Boolean - elseif directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("/*%if true *//*%elseif 1 *//*%end*/")
            class Execute(val id: Int): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The expression eval result must be a Boolean"))
    }

    @Test
    fun `The expression must be Iterable - bind directive`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("/* value */('a', 'b')")
            class Execute(val value: String): Exec()
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
            @KomapperCommand("/*%for each in s *//*%end*/")
            class Execute(val s: String): Exec()
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
            @KomapperCommand("/*%for each in list *//*%end*/")
            class Execute(val list: List<*>): Exec()
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
            @KomapperCommand("/* !value */'a'")
            class Execute(val value: String): Exec()
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
            @KomapperCommand("/* a && b */'a'")
            class Execute(val a: String, val b: Boolean): Exec()
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
            @KomapperCommand("/* a < b */'a'")
            class Execute(val a: String, val b: Boolean): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot compare because the operands are not the same type"))
    }

    @Test
    fun `Cannot compare because operands are not Comparable type`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            data class User(val id: Int)
            @KomapperCommand("/* a < b */'a'")
            class Execute(val a: User, val b: User): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Cannot compare because operands are not Comparable type"))
    }

    @Test
    fun `The variable is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("/* a */'' /* d */''")
            class Execute(val a: String, val b: String, val c: String): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The variable \"d\" is not found at <d>:1. Available variables are: [a, b, c]"))
    }

    @Test
    fun `The property is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import org.komapper.core.*
            @KomapperCommand("/* a.unknown */''")
            class Execute(val a: String): Exec()
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
            @KomapperCommand("/* a.unknown(b) */''")
            class Execute(val a: String, val b: Int): Exec()
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The function \"unknown\" is not found"))
    }
}
