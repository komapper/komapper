package org.komapper.ksp

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessors
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EntityProcessorTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `@KmEntity must be applied to data class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                class Dept(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmEntity must be applied to data class.")
    }

    @Test
    fun `@KmEntity cannot be applied to private data class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                private data class Dept(
                    val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmEntity cannot be applied to private data class.")
    }

    @Test
    fun `@KmEntity annotated class must not have type parameters`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept<T>(
                    val id: T
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmEntity annotated class must not have type parameters.")
    }

    @Test
    fun `Multiple @KmVersion cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
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
                import org.komapper.core.*
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
                import org.komapper.core.*import java.time.LocalDateTime
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
                import org.komapper.core.*
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
    fun `The parameter must not be private`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    private val aaa: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("The parameter must not be private.")
    }

    @Test
    fun `@KmId and @KmVersion cannot coexist on the same parameter`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmId @KmVersion val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmId and @KmVersion cannot coexist on the same parameter.")
    }

    @Test
    fun `@KmIdentityGenerator and @KmId must coexist`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmIdentityGenerator val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmIdentityGenerator and @KmId must coexist on the same parameter.")
    }

    @Test
    fun `@KmIdentityGenerator and @KmSequenceGenerator cannot coexist on the same parameter`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmIdentityGenerator @KmSequenceGenerator("ID", 100) val id: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmIdentityGenerator and @KmSequenceGenerator cannot coexist on the same parameter.")
    }

    @Test
    fun `Multiple Generators cannot coexist in a single class`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmId @KmIdentityGenerator val id1: Int,
                    @KmId @KmSequenceGenerator("ID", 100) val id2: Int
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("Multiple Generators cannot coexist in a single class.")
    }

    @Test
    fun `@KmVersion cannot apply to String type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmVersion val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmVersion cannot apply to String type.")
    }

    @Test
    fun `@KmCreatedAt cannot apply to String type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmCreatedAt val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmCreatedAt cannot apply to String type.")
    }

    @Test
    fun `@KmUpdatedAt cannot apply to String type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmUpdatedAt val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmUpdatedAt cannot apply to String type.")
    }

    @Test
    fun `@KmIgnore annotated parameter must have default value`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmIgnore val aaa: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmIgnore annotated parameter must have default value.")
    }

    @Test
    fun `@KmIdentityGenerator cannot apply to String type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmId @KmIdentityGenerator val id: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmIdentityGenerator cannot apply to String type.")
    }

    @Test
    fun `@KmSequenceGenerator cannot apply to String type`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
                @KmEntity
                data class Dept(
                    @KmId @KmSequenceGenerator("ID", 100) val id: String
                )
                """
            )
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.COMPILATION_ERROR)
        assertThat(result.messages).contains("@KmSequenceGenerator cannot apply to String type.")
    }

    @Test
    fun `@KmSequenceGenerator name is not found`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test
                import org.komapper.core.*
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
                import org.komapper.core.*
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
                workingDir = temporaryFolder.root
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
