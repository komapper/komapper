package org.komapper.processor

import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.Test
import kotlin.test.assertEquals

class EntityProcessorOkTest : AbstractKspTest(EntityProcessorProvider()) {

    @Test
    fun `The enum property can be annotated with @KomapperEnum`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            enum class Color { BLUE, RED }
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                @KomapperEnum(type = EnumType.ORDINAL)
                val color: Color,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `The array property type can have a type parameter`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                @Suppress("ArrayInDataClass") val names: Array<String>,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Ignore properties`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                @KomapperIgnore private val foo: String = "", // private
                @KomapperIgnore val bar: List<Int> = listOf(1, 2, 3), // type parameters
                @KomapperIgnore val baz: Baz? = Baz(1) // value class has nullable property
            )
            @JvmInline
            value class Baz(val value: Int?)
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Allow typealias for property`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            typealias Snowflake = Long
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Snowflake
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow chained typealias`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            typealias Snowflake = Long
            typealias Snowball = Snowflake
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Snowball
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow embeddable values`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptId(
                val id1: Int,
                val id2: Int
            )
            data class DeptInfo(
                val name: String,
                val location: String
            )
            @KomapperEntity
            data class Dept(
                @KomapperEmbeddedId
                val id: DeptId,
                @KomapperEmbedded
                val info: DeptInfo
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow type parameters for embedded values`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptInfo<T>(
                val name: T
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbedded
                val info: DeptInfo<String>
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow typealias for embedded values`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            typealias Snowflake = Pair<String, Int>
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperEmbedded
                @KomapperColumnOverride("first", KomapperColumn("color"))
                @KomapperColumnOverride("second", KomapperColumn("size"))
                val snowflake: Snowflake
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow alternate type mapping`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString(val value: String)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperColumn(alternateType = ClobString::class)
                val description: String
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow alternate type mapping - nullable`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString(val value: String)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperColumn(alternateType = ClobString::class)
                val description: String?
            )
            """,
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `The targetEntity is annotated with @KomapperEntity`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                val name: String,
            )
            @KomapperEntity
            @KomapperManyToOne(Dept::class)
            data class Emp(
                @KomapperId
                val id: Int,
                val name: String,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `The targetEntity is annotated with @KomapperEntityDef`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class Dept(
                val id: Int,
                val name: String,
            )
            @KomapperEntityDef(Dept::class)
            data class DeptDef(
                @KomapperId
                val id: Nothing,
            )
            @KomapperEntity
            @KomapperManyToOne(DeptDef::class)
            data class Emp(
                @KomapperId
                val id: Int,
                val name: String,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Allow the use of the property named 'java', when the Instant types are used`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import java.time.Instant
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                val java: String,
                @KomapperCreatedAt
                val created: Instant,
                @KomapperUpdatedAt
                val updated: Instant,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Allow the use of the property named 'java', when LocalDateTime types are used`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import java.time.LocalDateTime
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                val java: String,
                @KomapperCreatedAt
                val created: LocalDateTime,
                @KomapperUpdatedAt
                val updated: LocalDateTime,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Allow the use of the property named 'java', when OffsetDateTime types are used`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import java.time.OffsetDateTime
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                val java: String,
                @KomapperCreatedAt
                val created: OffsetDateTime,
                @KomapperUpdatedAt
                val updated: OffsetDateTime,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
