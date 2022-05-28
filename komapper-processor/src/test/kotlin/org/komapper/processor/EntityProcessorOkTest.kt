package org.komapper.processor

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals

class EntityProcessorOkTest {

    @TempDir
    @JvmField
    var tempDir: Path? = null

    @Test
    fun `The enum property can be annotated with @KomapperEnum`() {
        val result = compile(
            kotlin(
                "source.kt",
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
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `The array property type can have a type parameter`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId val id: Int,
                    @Suppress("ArrayInDataClass") val names: Array<String>,
                )
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `Ignore properties`() {
        val result = compile(
            kotlin(
                "source.kt",
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
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK)
    }

    @Test
    fun `Allow typealias for property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                typealias Snowflake = Long
                @KomapperEntity
                data class Dept(
                    @KomapperAutoIncrement @KomapperId
                    val id: Snowflake
                )
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    @Test
    fun `Allow chained typealias`() {
        val result = compile(
            kotlin(
                "source.kt",
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
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.OK, result.messages)
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = tempDir!!.toFile()
                inheritClassPath = true
                symbolProcessorProviders = listOf(EntityProcessorProvider())
                sources = sourceFiles.asList()
                verbose = false
                kspIncremental = false
            }
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return prepareCompilation(*sourceFiles).compile()
    }
}
