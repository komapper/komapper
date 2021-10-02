package org.komapper.ksp

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityProcessorTest {

    @TempDir
    @JvmField
    var tempDir: Path? = null

    @Test
    fun `Define a companion object in the class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId val id: Int
                )
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("Define a companion object in the class."))
    }

    @Test
    fun `The entity class must have at least one id property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must have at least one id property."))
    }

    @Test
    fun `The parent declaration of the entity class must be public`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                internal class Parent { 
                    @KomapperEntity
                    data class Dept(
                        val id: Int
                    ) { companion object }
                }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The parent declaration of the entity class must be public."))
    }

    @Test
    fun `The same name property is not found in the entity`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class Dept(
                    val id: Int
                )
                @KomapperEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int,
                    val version: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The same name property is not found in the entity."))
    }

    @Test
    fun `The class name cannot start with '__'`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @Suppress("ClassName")
                @KomapperEntity
                data class __Dept(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The class name cannot start with '__'."))
    }

    @Test
    fun `The class name cannot start with '__', @KomapperEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @Suppress("ClassName")
                data class __Dept(
                    val id: Int
                )
                @KomapperEntityDef(entity = __Dept::class)
                data class DeptDef(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The class name cannot start with '__'."))
    }

    @Test
    fun `The property name cannot start with '__'`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    val __id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The property name cannot start with '__'."))
    }

    @Test
    fun `The property name cannot start with '__', @KomapperEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class Dept(
                    val id: Int,
                    val __name: String
                )
                @KomapperEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The property name cannot start with '__'."))
    }

    @Test
    fun `The entity class must be a data class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                class Dept(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must be a data class."))
    }

    @Test
    fun `The entity class must be a data class, @KomapperEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                class Dept(
                    val id: Int
                )
                @KomapperEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must be a data class."))
    }

    @Test
    fun `@KomapperEntity cannot be applied to this element`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                class Dept(
                    @KomapperEntity val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperEntity cannot be applied to this element."))
    }

    @Test
    fun `@KomapperEntityDef cannot be applied to this element`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class Dept(
                    val id: Int
                )
                class DeptDef(
                    @KomapperEntityDef(entity = Dept::class) val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperEntityDef cannot be applied to this element."))
    }

    @Test
    fun `The entity class must not be private`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                private data class Dept(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must not be private."))
    }

    @Test
    fun `The entity class must not be private, @KomapperEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                private data class Dept(
                    val id: Int
                )
                @KomapperEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must not be private."))
    }

    @Test
    fun `The entity class must not have type parameters`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept<T>(
                    val id: T
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must not have type parameters."))
    }

    @Test
    fun `The entity class must not have type parameters, @KomapperEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class Dept<T>(
                    val id: T
                )
                @KomapperEntityDef(entity = Dept::class)
                data class DeptDef<T>(
                    val id: T
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The entity class must not have type parameters."))
    }

    @Test
    fun `Multiple @KomapperVersion cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperVersion val aaa: Int,
                    @KomapperVersion val bbb: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("Multiple @KomapperVersion cannot coexist in a single class."))
    }

    @Test
    fun `Multiple @KomapperCreatedAt cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                import java.time.LocalDateTime
                @KomapperEntity
                data class Dept(
                    @KomapperCreatedAt val aaa: LocalDateTime,
                    @KomapperCreatedAt val bbb: LocalDateTime
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("Multiple @KomapperCreatedAt cannot coexist in a single class."))
    }

    @Test
    fun `Multiple @KomapperUpdatedAt cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*import java.time.LocalDateTime
                @KomapperEntity
                data class Dept(
                    @KomapperUpdatedAt val aaa: LocalDateTime,
                    @KomapperUpdatedAt val bbb: LocalDateTime
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("Multiple @KomapperUpdatedAt cannot coexist in a single class."))
    }

    @Test
    fun `Any persistent properties are not found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperIgnore val aaa: Int = 0,
                    @KomapperIgnore val bbb: Int = 0
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("Any persistent properties are not found."))
    }

    @Test
    fun `The property must not be private`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    private val aaa: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The property must not be private."))
    }

    @Test
    fun `@KomapperId and @KomapperVersion cannot coexist on the same property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperVersion val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperId and @KomapperVersion cannot coexist on the same property."))
    }

    @Test
    fun `@KomapperAutoIncrement and @KomapperId must coexist on the same property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperAutoIncrement val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperAutoIncrement and @KomapperId must coexist on the same property."))
    }

    @Test
    fun `@KomapperAutoIncrement and @KomapperSequence cannot coexist on the same property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperAutoIncrement @KomapperSequence("ID", 1, 100) val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperAutoIncrement and @KomapperSequence cannot coexist on the same property."))
    }

    @Test
    fun `@KomapperAutoIncrement and @KomapperSequence cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperAutoIncrement val id1: Int,
                    @KomapperId @KomapperSequence("ID", 1, 100) val id2: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperAutoIncrement and @KomapperSequence cannot coexist in a single class."))
    }

    @Test
    fun `The type of @KomapperVersion annotated property must be either Int, Long, UInt or value class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperVersion val aaa: String
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The type of @KomapperVersion annotated property must be either Int, Long, UInt or value class."))
    }

    @Test
    fun `When the type of @KomapperVersion annotated property is value class, the type of the value class's own property must be either Int, Long or UInt`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class MyVersion(
                    val version: String
                )
                @KomapperEntity
                data class Dept(
                    @KomapperVersion val version: MyVersion
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperVersion annotated property is value class, " +
                    "the type of the value class's own property must be either Int, Long or UInt."
            )
        )
    }

    @Test
    fun `The type of @KomapperCreatedAt annotated property must be either LocalDateTime or OffsetDateTime`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperCreatedAt val aaa: String
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The type of @KomapperCreatedAt annotated property must be either LocalDateTime or OffsetDateTime."))
    }

    @Test
    fun `When the type of @KomapperCreatedAt annotated property is value class, the type of the value class's own property must be either LocalDateTime or OffsetDateTime`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class MyDateTime(
                    val dataTime: String
                )
                @KomapperEntity
                data class Dept(
                    @KomapperCreatedAt val dataTime: MyDateTime
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperCreatedAt annotated property is value class, " +
                    "the type of the value class's own property must be either LocalDateTime or OffsetDateTime."
            )
        )
    }

    @Test
    fun `The type of @KomapperUpdatedAt annotated property must be either LocalDateTime or OffsetDateTime`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperUpdatedAt val aaa: String
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The type of @KomapperUpdatedAt annotated property must be either LocalDateTime or OffsetDateTime."))
    }

    @Test
    fun `When the type of @KomapperUpdatedAt annotated property is value class, the type of the value class's own property must be either LocalDateTime or OffsetDateTime`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class MyDateTime(
                    val dataTime: String
                )
                @KomapperEntity
                data class Dept(
                    @KomapperUpdatedAt val dataTime: MyDateTime
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperUpdatedAt annotated property is value class, " +
                    "the type of the value class's own property must be either LocalDateTime or OffsetDateTime."
            )
        )
    }

    @Test
    fun `The ignored property must have a default value`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperIgnore val aaa: String
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The ignored property must have a default value."))
    }

    @Test
    fun `The type of @KomapperAutoIncrement annotated property must be either Int, Long, UInt or value class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperAutoIncrement val id: String
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The type of @KomapperAutoIncrement annotated property must be either Int, Long, UInt or value class."))
    }

    @Test
    fun `The type of @KomapperSequence annotated property must be either Int, Long, UInt or value class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperSequence("ID", 1, 100) val id: String
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The type of @KomapperSequence annotated property must be either Int, Long, UInt or value class."))
    }

    @Test
    fun `When the type of @KomapperAutoIncrement annotated property is value class, the type of value class's own property must be either Int, Long or UInt`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class MyId(
                    val id: String
                )
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperAutoIncrement val id: MyId
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("When the type of @KomapperAutoIncrement annotated property is value class, the type of the value class's own property must be either Int, Long or UInt."))
    }

    @Test
    fun `When the type of @KomapperSequence annotated property is value class, the type of value class's own property must be either Int, Long or UInt`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class MyId(
                    val id: String
                )
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperSequence("my_seq") val id: MyId
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperSequence annotated property is value class, " +
                    "the type of the value class's own property must be either Int, Long or UInt."
            )
        )
    }

    @Test
    fun `@KomapperSequence name is not found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KomapperEntity
                data class Dept(
                    @KomapperId @KomapperSequence() val id: Int
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("@KomapperSequence.name is not found."))
    }

    @Test
    fun `The value class property must not be private`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class Name(private val name: String)
                @KomapperEntity
                data class Dept(
                    @KomapperId val id: Int,
                    val name: Name,
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The value class's own property 'name' must not be private."))
    }

    @Test
    fun `The value class property must not be nullable`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @JvmInline
                value class Name(val name: String?)
                @KomapperEntity
                data class Dept(
                    @KomapperId val id: Int,
                    val name: Name,
                ) { companion object }
                """
            )
        )
        assertEquals(result.exitCode, KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertTrue(result.messages.contains("The value class's own property 'name' must not be nullable."))
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
