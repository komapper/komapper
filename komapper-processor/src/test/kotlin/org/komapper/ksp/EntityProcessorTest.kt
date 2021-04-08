package org.komapper.ksp

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessors
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class EntityProcessorTest {

    @TempDir
    @JvmField
    var tempDir: Path? = null

    @Test fun `Duplicated definitions are found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    val id: Int
                )
                @KmEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Duplicated definitions are found.")
    }

    @Test fun `The same name property is not found in the entity`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class Dept(
                    val id: Int
                )
                @KmEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int,
                    val version: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The same name property is not found in the entity.")
    }

    @Test fun `The class name cannot start with '__'`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class __Dept(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The class name cannot start with '__'.")
    }

    @Test fun `The class name cannot start with '__', @KmEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class __Dept(
                    val id: Int
                )
                @KmEntityDef(entity = __Dept::class)
                data class DeptDef(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The class name cannot start with '__'.")
    }

    @Test fun `The property name cannot start with '__'`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    val __id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The property name cannot start with '__'.")
    }

    @Test fun `The property name cannot start with '__', @KmEntityDef`() {
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
                @KmEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The property name cannot start with '__'.")
    }

    @Test
    fun `The entity class must be a data class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                class Dept(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The entity class must be a data class.")
    }

    @Test
    fun `The entity class must be a data class, @KmEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                class Dept(
                    val id: Int
                )
                @KmEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The entity class must be a data class.")
    }

    @Test
    fun `@KmEntity cannot be applied to this element`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                class Dept(
                    @KmEntity val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmEntity cannot be applied to this element.")
    }

    @Test
    fun `@KmEntityDef cannot be applied to this element`() {
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
                    @KmEntityDef(entity = Dept::class) val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmEntityDef cannot be applied to this element.")
    }

    @Test
    fun `The entity class must not be private`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                private data class Dept(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The entity class must not be private.")
    }

    @Test
    fun `The entity class must not be private, @KmEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                private data class Dept(
                    val id: Int
                )
                @KmEntityDef(entity = Dept::class)
                data class DeptDef(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The entity class must not be private.")
    }

    @Test
    fun `The entity class must not have type parameters`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept<T>(
                    val id: T
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The entity class must not have type parameters.")
    }

    @Test
    fun `The entity class must not have type parameters, @KmEntityDef`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                data class Dept<T>(
                    val id: T
                )
                @KmEntityDef(entity = Dept::class)
                data class DeptDef<T>(
                    val id: T
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The entity class must not have type parameters.")
    }

    @Test
    fun `Multiple @KmVersion cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmVersion val aaa: Int,
                    @KmVersion val bbb: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Multiple @KmVersion cannot coexist in a single class.")
    }

    @Test
    fun `Multiple @KmCreatedAt cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                import java.time.LocalDateTime
                @KmEntity
                data class Dept(
                    @KmCreatedAt val aaa: LocalDateTime,
                    @KmCreatedAt val bbb: LocalDateTime
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Multiple @KmCreatedAt cannot coexist in a single class.")
    }

    @Test
    fun `Multiple @KmUpdatedAt cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*import java.time.LocalDateTime
                @KmEntity
                data class Dept(
                    @KmUpdatedAt val aaa: LocalDateTime,
                    @KmUpdatedAt val bbb: LocalDateTime
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Multiple @KmUpdatedAt cannot coexist in a single class.")
    }

    @Test
    fun `Any persistent properties are not found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmIgnore val aaa: Int = 0,
                    @KmIgnore val bbb: Int = 0
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Any persistent properties are not found.")
    }

    @Test
    fun `The property must not be private`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    private val aaa: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The property must not be private.")
    }

    @Test
    fun `@KmId and @KmVersion cannot coexist on the same property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmId @KmVersion val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmId and @KmVersion cannot coexist on the same property.")
    }

    @Test
    fun `@KmIdentityGenerator and @KmId must coexist on the same property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmIdentityGenerator val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmIdentityGenerator and @KmId must coexist on the same property.")
    }

    @Test
    fun `@KmIdentityGenerator and @KmSequenceGenerator cannot coexist on the same property`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmIdentityGenerator @KmSequenceGenerator("ID", 100) val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmIdentityGenerator and @KmSequenceGenerator cannot coexist on the same property.")
    }

    @Test
    fun `Multiple generator properties cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmId @KmIdentityGenerator val id1: Int,
                    @KmId @KmSequenceGenerator("ID", 100) val id2: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Multiple generator properties cannot coexist in a single class.")
    }

    @Test
    fun `The version property must either be Int or Long type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmVersion val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The version property must be either Int or Long type.")
    }

    @Test
    fun `The createdAt property must be either LocalDateTime or OffsetDateTime type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmCreatedAt val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The createdAt property must be either LocalDateTime or OffsetDateTime type.")
    }

    @Test
    fun `The updatedAt property must be either LocalDateTime or OffsetDateTime type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmUpdatedAt val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The updatedAt property must be either LocalDateTime or OffsetDateTime type.")
    }

    @Test
    fun `The ignored property must have a default value`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmIgnore val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The ignored property must have a default value.")
    }

    @Test
    fun `The identity generator property must be either Int or Long type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmId @KmIdentityGenerator val id: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The identity generator property must be either Int or Long type.")
    }

    @Test
    fun `The sequence generator property must be either Int or Long type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmId @KmSequenceGenerator("ID", 100) val id: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The sequence generator property must be either Int or Long type.")
    }

    @Test
    fun `@KmSequenceGenerator name is not found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmId @KmSequenceGenerator() val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmSequenceGenerator.name is not found.")
    }

    @Test
    fun `@KmSequenceGenerator incrementBy is not found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.annotation.*
                @KmEntity
                data class Dept(
                    @KmId @KmSequenceGenerator("ID_SEQ") val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmSequenceGenerator.incrementBy is not found.")
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = tempDir!!.toFile()
                inheritClassPath = true
                symbolProcessors = listOf(EntityProcessor())
                sources = sourceFiles.asList()
                verbose = false
                kspIncremental = false
            }
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return prepareCompilation(*sourceFiles).compile()
    }
}
