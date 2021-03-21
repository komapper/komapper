package org.komapper.ksp

import com.google.common.truth.Truth.assertThat
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.SourceFile.Companion.kotlin
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessors
import org.intellij.lang.annotations.Language
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText

class EntityProcessorTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    @Test
    fun `@KmId and @KmVersion cannot coexist`() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test

                import org.komapper.core.KmEntity
                import org.komapper.core.KmId
                import org.komapper.core.KmVersion
                
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

                import org.komapper.core.KmEntity
                import org.komapper.core.KmId
                import org.komapper.core.KmIdentityGenerator
                import org.komapper.core.KmVersion
                
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

    @ExperimentalPathApi
    @Test
    @Ignore
    fun test() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test

                import org.komapper.core.KmEntity
                import org.komapper.core.KmId
                import org.komapper.core.KmVersion
                
                @KmEntity
                data class Emp(
                    @KmId val id: Int,
                    val name: String,
                    @KmVersion val version: Int
                    ) {}
                """
            )
        )
        assertContent(
            "test/Emp_.kt",
            """
                package test

                import java.time.Clock
                import org.komapper.core.metamodel.EntityMetamodel
                import org.komapper.core.metamodel.PropertyDescriptor
                import org.komapper.core.metamodel.PropertyMetamodel

                @Suppress("ClassName")
                class Emp_ : EntityMetamodel<Emp> {
                    private object EntityDescriptor {
                        val id = PropertyDescriptor<Emp, kotlin.Int>(kotlin.Int::class, "ID", { it.id }) { (e, v) -> e.copy(id = v) }
                        val name = PropertyDescriptor<Emp, kotlin.String>(kotlin.String::class, "NAME", { it.name }) { (e, v) -> e.copy(name = v) }
                        val version = PropertyDescriptor<Emp, kotlin.Int>(kotlin.Int::class, "VERSION", { it.version }) { (e, v) -> e.copy(version = v) }
                    }
                    val id by lazy { PropertyMetamodel(this, EntityDescriptor.id) }
                    val name by lazy { PropertyMetamodel(this, EntityDescriptor.name) }
                    val version by lazy { PropertyMetamodel(this, EntityDescriptor.version) }
                    override fun tableName() = "EMP"
                    override fun identityProperties(): List<PropertyMetamodel<Emp, *>> = listOf(id)
                    override fun versionProperty(): PropertyMetamodel<Emp, *>? = version
                    override fun allProperties(): List<PropertyMetamodel<Emp, *>> = listOf(id, name, version)
                    override fun instantiate(a: Array<Any?>) = Emp(a[0] as kotlin.Int, a[1] as kotlin.String, a[2] as kotlin.Int)
                    override fun incrementVersion(e: Emp): Emp = version.set(e to version.get(e)!!.inc())
                    override fun setCreationClock(e: Emp, c: Clock): Emp = e
                    override fun setUpdateClock(e: Emp, c: Clock): Emp = e
                }

                """
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @ExperimentalPathApi
    @Test
    @Ignore
    fun nestedClass() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test

                import org.komapper.core.KmEntity
                import org.komapper.core.KmId
                import org.komapper.core.KmVersion
                
                class Hoge {
                    @KmEntity
                    data class Emp(
                        @KmId val id: Int,
                        val name: String,
                        @KmVersion val version: Int
                        ) {}
                    }
                """
            )
        )
        assertContent(
            "test/Hoge_Emp_.kt",
            """
                package test

                import java.time.Clock
                import org.komapper.core.metamodel.EntityMetamodel
                import org.komapper.core.metamodel.PropertyDescriptor
                import org.komapper.core.metamodel.PropertyMetamodel

                @Suppress("ClassName")
                class Hoge_Emp_ : EntityMetamodel<Hoge.Emp> {
                    private object EntityDescriptor {
                        val id = PropertyDescriptor<Hoge.Emp, kotlin.Int>(kotlin.Int::class, "ID", { it.id }) { (e, v) -> e.copy(id = v) }
                        val name = PropertyDescriptor<Hoge.Emp, kotlin.String>(kotlin.String::class, "NAME", { it.name }) { (e, v) -> e.copy(name = v) }
                        val version = PropertyDescriptor<Hoge.Emp, kotlin.Int>(kotlin.Int::class, "VERSION", { it.version }) { (e, v) -> e.copy(version = v) }
                    }
                    val id by lazy { PropertyMetamodel(this, EntityDescriptor.id) }
                    val name by lazy { PropertyMetamodel(this, EntityDescriptor.name) }
                    val version by lazy { PropertyMetamodel(this, EntityDescriptor.version) }
                    override fun tableName() = "EMP"
                    override fun identityProperties(): List<PropertyMetamodel<Hoge.Emp, *>> = listOf(id)
                    override fun versionProperty(): PropertyMetamodel<Hoge.Emp, *>? = version
                    override fun allProperties(): List<PropertyMetamodel<Hoge.Emp, *>> = listOf(id, name, version)
                    override fun instantiate(a: Array<Any?>) = Hoge.Emp(a[0] as kotlin.Int, a[1] as kotlin.String, a[2] as kotlin.Int)
                    override fun incrementVersion(e: Hoge.Emp): Hoge.Emp = version.set(e to version.get(e)!!.inc())
                    override fun setCreationClock(e: Hoge.Emp, c: Clock): Hoge.Emp = e
                    override fun setUpdateClock(e: Hoge.Emp, c: Clock): Hoge.Emp = e
                }

                """
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @ExperimentalPathApi
    @Test
    @Ignore
    fun companionObject() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test

                import org.komapper.core.KmEntity
                import org.komapper.core.KmId
                import org.komapper.core.KmVersion
                
                @KmEntity
                data class Emp(
                    @KmId val id: Int,
                    val name: String,
                    @KmVersion val version: Int
                ) {
                    companion object {}
                }
                """
            )
        )
        assertContent(
            "test/Emp_.kt",
            """
                package test

                import java.time.Clock
                import org.komapper.core.metamodel.EntityMetamodel
                import org.komapper.core.metamodel.PropertyDescriptor
                import org.komapper.core.metamodel.PropertyMetamodel

                @Suppress("ClassName")
                class Emp_ : EntityMetamodel<Emp> {
                    private object EntityDescriptor {
                        val id = PropertyDescriptor<Emp, kotlin.Int>(kotlin.Int::class, "ID", { it.id }) { (e, v) -> e.copy(id = v) }
                        val name = PropertyDescriptor<Emp, kotlin.String>(kotlin.String::class, "NAME", { it.name }) { (e, v) -> e.copy(name = v) }
                        val version = PropertyDescriptor<Emp, kotlin.Int>(kotlin.Int::class, "VERSION", { it.version }) { (e, v) -> e.copy(version = v) }
                    }
                    val id by lazy { PropertyMetamodel(this, EntityDescriptor.id) }
                    val name by lazy { PropertyMetamodel(this, EntityDescriptor.name) }
                    val version by lazy { PropertyMetamodel(this, EntityDescriptor.version) }
                    override fun tableName() = "EMP"
                    override fun identityProperties(): List<PropertyMetamodel<Emp, *>> = listOf(id)
                    override fun versionProperty(): PropertyMetamodel<Emp, *>? = version
                    override fun allProperties(): List<PropertyMetamodel<Emp, *>> = listOf(id, name, version)
                    override fun instantiate(a: Array<Any?>) = Emp(a[0] as kotlin.Int, a[1] as kotlin.String, a[2] as kotlin.Int)
                    override fun incrementVersion(e: Emp): Emp = version.set(e to version.get(e)!!.inc())
                    override fun setCreationClock(e: Emp, c: Clock): Emp = e
                    override fun setUpdateClock(e: Emp, c: Clock): Emp = e
                }

                fun Emp.Companion.metamodel() = Emp_()

                """
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @ExperimentalPathApi
    @Test
    @Ignore
    fun clock() {
        val result = compile(
            kotlin(
                "source.kt",
                """
                package test

                import java.time.LocalDateTime
                import org.komapper.core.KmCreatedAt
                import org.komapper.core.KmEntity
                import org.komapper.core.KmId
                import org.komapper.core.KmUpdatedAt
                
                @KmEntity
                data class Dept(
                    @KmId val id: Int,
                    @KmCreatedAt val createdAt: LocalDateTime,
                    @KmUpdatedAt val updatedAt: LocalDateTime
                )
                """
            )
        )
        assertContent(
            "test/Dept_.kt",
            """
                package test

                import java.time.Clock
                import org.komapper.core.metamodel.EntityMetamodel
                import org.komapper.core.metamodel.PropertyDescriptor
                import org.komapper.core.metamodel.PropertyMetamodel
                
                @Suppress("ClassName")
                class Dept_ : EntityMetamodel<Dept> {
                    private object EntityDescriptor {
                        val id = PropertyDescriptor<Dept, kotlin.Int>(kotlin.Int::class, "ID", { it.id }) { (e, v) -> e.copy(id = v) }
                        val createdAt = PropertyDescriptor<Dept, java.time.LocalDateTime>(java.time.LocalDateTime::class, "CREATEDAT", { it.createdAt }) { (e, v) -> e.copy(createdAt = v) }
                        val updatedAt = PropertyDescriptor<Dept, java.time.LocalDateTime>(java.time.LocalDateTime::class, "UPDATEDAT", { it.updatedAt }) { (e, v) -> e.copy(updatedAt = v) }
                    }
                    val id by lazy { PropertyMetamodel(this, EntityDescriptor.id) }
                    val createdAt by lazy { PropertyMetamodel(this, EntityDescriptor.createdAt) }
                    val updatedAt by lazy { PropertyMetamodel(this, EntityDescriptor.updatedAt) }
                    override fun tableName() = "DEPT"
                    override fun identityProperties(): List<PropertyMetamodel<Dept, *>> = listOf(id)
                    override fun versionProperty(): PropertyMetamodel<Dept, *>? = null
                    override fun allProperties(): List<PropertyMetamodel<Dept, *>> = listOf(id, createdAt, updatedAt)
                    override fun instantiate(a: Array<Any?>) = Dept(a[0] as kotlin.Int, a[1] as java.time.LocalDateTime, a[2] as java.time.LocalDateTime)
                    override fun incrementVersion(e: Dept): Dept = e
                    override fun setCreationClock(e: Dept, c: Clock): Dept = createdAt.set(e to java.time.LocalDateTime.now(c))
                    override fun setUpdateClock(e: Dept, c: Clock): Dept = updatedAt.set(e to java.time.LocalDateTime.now(c))
                }

                """
        )
        assertThat(result.exitCode).isEqualTo(KotlinCompilation.ExitCode.OK)
    }

    @ExperimentalPathApi
    private fun assertContent(path: String, @Language("kotlin") content: String) {
        val kotlinDir = temporaryFolder.root.toPath().resolve("ksp/sources/kotlin")
        val file = kotlinDir.resolve(path)
        val actual = file.readText()
        val expected = content.trimIndent()
        assertThat(actual).isEqualTo(expected)
    }

    private fun prepareCompilation(vararg sourceFiles: SourceFile): KotlinCompilation {
        return KotlinCompilation()
            .apply {
                workingDir = temporaryFolder.root
                inheritClassPath = true
                symbolProcessors = listOf(EntityProcessor())
                sources = sourceFiles.asList()
                verbose = false
                kspIncremental = true
            }
    }

    private fun compile(vararg sourceFiles: SourceFile): KotlinCompilation.Result {
        return prepareCompilation(*sourceFiles).compile()
    }
}
