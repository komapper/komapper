package org.komapper.processor

import com.tschuchort.compiletesting.KotlinCompilation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EntityProcessorErrorTest : AbstractKspTest(EntityProcessorProvider()) {

    @Test
    fun `The entity class must have at least one id property`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The entity class must have at least one id property."))
    }

    @Test
    fun `The entity class can have either @KomapperEmbeddedId or @KomapperId`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptId(
                val id1: Int,
                val id2: Int
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbeddedId
                val id2: DeptId
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The entity class can have either @KomapperEmbeddedId or @KomapperId."))
    }

    @Test
    fun `The enclosing declaration of the class must be public`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            internal class Parent { 
                @KomapperEntity
                data class Dept(
                    val id: Int
                )
            }
            """,
        )

        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The enclosing declaration \"Parent\" of the class \"Dept\" must be public."))
    }

    @Test
    fun `The same name property is not found in the entity`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The same name property is not found in the entity."))
    }

    @Test
    fun `The class name cannot start with '__'`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @Suppress("ClassName")
            @KomapperEntity
            data class __Dept(
                val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class name cannot start with '__'."))
    }

    @Test
    fun `The class name cannot start with '__', @KomapperEntityDef`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class name cannot start with '__'."))
    }

    @Test
    fun `The property name cannot start with '__'`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                val __id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property name cannot start with '__'."))
    }

    @Test
    fun `The property name cannot start with '__', @KomapperEntityDef`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property name cannot start with '__'."))
    }

    @Test
    fun `The class must be a data class, @KomapperEntity`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            class Dept(
                val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Dept\" must be a data class."))
    }

    @Test
    fun `The class must be a data class, @KomapperEntityDef`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            class Dept(
                val id: Int
            )
            @KomapperEntityDef(entity = Dept::class)
            data class DeptDef(
                val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Dept\" must be a data class."))
    }

    @Test
    fun `The class must be a data class, @KomapperEmbedded`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            class DeptInfo(
                val name: String,
                val location: String
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbedded
                val info: DeptInfo
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"DeptInfo\" must be a data class."))
    }

    @Test
    fun `The class must be a data class, @KomapperEmbeddedId`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            class DeptId(
                val id1: Int,
                val id2: Int
            )
            @KomapperEntity
            data class Dept(
                @KomapperEmbeddedId
                val id: DeptId
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"DeptId\" must be a data class."))
    }

    @Test
    fun `@KomapperEntity cannot be applied to this element`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            class Dept(
                @KomapperEntity val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperEntity cannot be applied to this element."))
    }

    @Test
    fun `@KomapperEntityDef cannot be applied to this element`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class Dept(
                val id: Int
            )
            class DeptDef(
                @KomapperEntityDef(entity = Dept::class) val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperEntityDef cannot be applied to this element."))
    }

    @Test
    fun `The class must not be private, @KomapperEntity`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            private data class Dept(
                val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Dept\" must not be private."))
    }

    @Test
    fun `The class must not be private, @KomapperEntityDef`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            private data class Dept(
                val id: Int
            )
            @KomapperEntityDef(entity = Dept::class)
            data class DeptDef(
                val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Dept\" must not be private."))
    }

    @Test
    fun `The class must not be private, @KomapperEmbedded`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            private data class DeptInfo(
                val id1: Int,
                val id2: Int
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbedded
                val info: DeptInfo
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"DeptInfo\" must not be private."))
    }

    @Test
    fun `The class must not have type parameters, @KomapperEntity`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept<T>(
                val id: T
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Dept\" must not have type parameters."))
    }

    @Test
    fun `The class must not have type parameters, @KomapperEntityDef`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class Dept<T>(
                val id: T
            )
            @KomapperEntityDef(entity = Dept::class)
            data class DeptDef<T>(
                val id: T
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The class \"Dept\" must not have type parameters."))
    }

    @Test
    fun `Multiple @KomapperEmbeddedId cannot coexist in a single class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptId(
                val id1: Int,
                val id2: Int
            )
            @KomapperEntity
            data class Dept(
                @KomapperEmbeddedId val aaa: DeptId,
                @KomapperEmbeddedId val bbb: DeptId
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Multiple @KomapperEmbeddedId cannot coexist in a single class."))
    }

    @Test
    fun `Multiple @KomapperVersion cannot coexist in a single class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperVersion val aaa: Int,
                @KomapperVersion val bbb: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Multiple @KomapperVersion cannot coexist in a single class."))
    }

    @Test
    fun `Multiple @KomapperCreatedAt cannot coexist in a single class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            import java.time.LocalDateTime
            @KomapperEntity
            data class Dept(
                @KomapperCreatedAt val aaa: LocalDateTime,
                @KomapperCreatedAt val bbb: LocalDateTime
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Multiple @KomapperCreatedAt cannot coexist in a single class."))
    }

    @Test
    fun `Multiple @KomapperUpdatedAt cannot coexist in a single class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*import java.time.LocalDateTime
            @KomapperEntity
            data class Dept(
                @KomapperUpdatedAt val aaa: LocalDateTime,
                @KomapperUpdatedAt val bbb: LocalDateTime
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Multiple @KomapperUpdatedAt cannot coexist in a single class."))
    }

    @Test
    fun `Any persistent properties are not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperIgnore val aaa: Int = 0,
                @KomapperIgnore val bbb: Int = 0
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("Any persistent properties are not found."))
    }

    @Test
    fun `The property must not be private`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                private val aaa: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property must not be private."))
    }

    @Test
    fun `@KomapperId and @KomapperVersion cannot coexist on the same property`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId @KomapperVersion val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperId and @KomapperVersion cannot coexist on the same property."))
    }

    @Test
    fun `@KomapperAutoIncrement and @KomapperId must coexist on the same property`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperAutoIncrement and @KomapperId must coexist on the same property."))
    }

    @Test
    fun `@KomapperAutoIncrement and @KomapperSequence cannot coexist on the same property`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperSequence("ID", 1, 100) val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperAutoIncrement and @KomapperSequence cannot coexist on the same property."))
    }

    @Test
    fun `@KomapperAutoIncrement and @KomapperSequence cannot coexist in a single class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId @KomapperAutoIncrement val id1: Int,
                @KomapperId @KomapperSequence("ID", 1, 100) val id2: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperAutoIncrement and @KomapperSequence cannot coexist in a single class."))
    }

    @Test
    fun `The type of @KomapperVersion annotated property must be either Int, Long, UInt or value class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperVersion val aaa: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The type of @KomapperVersion annotated property must be either Int, Long, UInt or value class."))
    }

    @Test
    fun `When the type of @KomapperVersion annotated property is value class, the type of the value class's own property must be either Int, Long or UInt`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperVersion annotated property is value class, " +
                    "the type of the value class's own property must be either Int, Long or UInt.",
            ),
        )
    }

    @Test
    fun `The type of @KomapperCreatedAt annotated property must be either Instant, LocalDateTime or OffsetDateTime`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperCreatedAt val aaa: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The type of @KomapperCreatedAt annotated property must be either Instant, LocalDateTime or OffsetDateTime."))
    }

    @Test
    fun `When the type of @KomapperCreatedAt annotated property is value class, the type of the value class's own property must be either Instant, LocalDateTime or OffsetDateTime`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperCreatedAt annotated property is value class, " +
                    "the type of the value class's own property must be either Instant, LocalDateTime or OffsetDateTime.",
            ),
        )
    }

    @Test
    fun `The type of @KomapperUpdatedAt annotated property must be either Instant, LocalDateTime or OffsetDateTime`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperUpdatedAt val aaa: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The type of @KomapperUpdatedAt annotated property must be either Instant, LocalDateTime or OffsetDateTime."))
    }

    @Test
    fun `When the type of @KomapperUpdatedAt annotated property is value class, the type of the value class's own property must be either Instant, LocalDateTime or OffsetDateTime`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperUpdatedAt annotated property is value class, " +
                    "the type of the value class's own property must be either Instant, LocalDateTime or OffsetDateTime.",
            ),
        )
    }

    @Test
    fun `The ignored property must have a default value`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperIgnore val aaa: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The ignored property must have a default value."))
    }

    @Test
    fun `The type of @KomapperAutoIncrement annotated property must be either Int, Long, UInt or value class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId @KomapperAutoIncrement val id: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The type of @KomapperAutoIncrement annotated property must be either Int, Long, UInt or value class."))
    }

    @Test
    fun `The type of @KomapperSequence annotated property must be either Int, Long, UInt or value class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId @KomapperSequence("ID", 1, 100) val id: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The type of @KomapperSequence annotated property must be either Int, Long, UInt or value class."))
    }

    @Test
    fun `When the type of @KomapperAutoIncrement annotated property is value class, the type of value class's own property must be either Int, Long or UInt`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("When the type of @KomapperAutoIncrement annotated property is value class, the type of the value class's own property must be either Int, Long or UInt."))
    }

    @Test
    fun `When the type of @KomapperSequence annotated property is value class, the type of value class's own property must be either Int, Long or UInt`() {
        val result = compile(
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
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(
            result.messages.contains(
                "When the type of @KomapperSequence annotated property is value class, " +
                    "the type of the value class's own property must be either Int, Long or UInt.",
            ),
        )
    }

    @Test
    fun `@KomapperSequence name is not found`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId @KomapperSequence() val id: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperSequence.name is not found."))
    }

    @Test
    fun `The value class property must not be private`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @JvmInline
            value class Name(private val name: String)
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                val name: Name,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The value class's own property 'name' must not be private."))
    }

    @Test
    fun `The value class property must not be nullable`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @JvmInline
            value class Name(val name: String?)
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                val name: Name,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The value class's own property 'name' must not be nullable."))
    }

    @Test
    fun `@KomapperEnum is valid only for enum property types - value class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @JvmInline
            value class Name(val name: String)
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                @KomapperEnum
                val name: Name,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperEnum is valid only for enum property types."))
    }

    @Test
    fun `@KomapperEnum is valid only for enum property types - plain class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                @KomapperEnum
                val name: String,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperEnum is valid only for enum property types."))
    }

    @Test
    fun `The property must not be a generic type`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                val names: List<String>,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"names\" must not be a generic type \"List<String>\"."))
    }

    @Test
    fun `The property must not be a generic type, @KomapperEmbedded`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId val id: Int,
                @KomapperEmbedded val info: Pair<String, List<Int>>,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"info.second\" must not be a generic type \"List<Int>\"."))
    }

    @Test
    fun `The property is not found in the class, @KomapperColumnOverride`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptInfo(
                val name: String,
                val location: String
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbedded
                @KomapperColumnOverride("address", KomapperColumn("ADDRESS"))
                val info: DeptInfo
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"address\" is not found in the class \"DeptInfo\"."))
    }

    @Test
    fun `The property is not found in the class, @KomapperEnumOverride`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            enum class Location { TOKYO, OSAKA }
            data class DeptInfo(
                val name: String,
                val location: Location
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbedded
                @KomapperEnumOverride("address", KomapperEnum(EnumType.ORDINAL))
                val info: DeptInfo
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"address\" is not found in the class \"DeptInfo\"."))
    }

    @Test
    fun `@KomapperColumnOverride must be used with either @KomapperEmbedded or @KomapperEmbeddedId`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptInfo(
                val name: String,
                val location: String
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperColumnOverride("address", KomapperColumn("ADDRESS"))
                val info: DeptInfo
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperColumnOverride must be used with either @KomapperEmbedded or @KomapperEmbeddedId."))
    }

    @Test
    fun `@KomapperEnumOverride must be used with either @KomapperEmbedded or @KomapperEmbeddedId`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class DeptInfo(
                val name: String,
                val location: String
            )
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEnumOverride("address", KomapperEnum(EnumType.ORDINAL))
                val info: DeptInfo
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperEnumOverride must be used with either @KomapperEmbedded or @KomapperEmbeddedId."))
    }

    @Test
    fun `The property must not be a star-projected type`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEmbedded
                val info: Pair<Int, *>
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"info.second\" must not be a star-projected type."))
    }

    @Test
    fun `The unit value of @KomapperEntity must be an object`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            @KomapperEntity(unit = String::class)
            data class Dept(
                @KomapperId
                val id: Int,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The unit value of @KomapperEntity must be an object."))
    }

    @Test
    fun `The unit value of @KomapperEntityDef must be an object`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class Dept(
                val id: Int,
            )
            @KomapperEntityDef(entity = Dept::class, unit = String::class)
            data class DeptDef(
                @KomapperId
                val id: Int,
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The unit value of @KomapperEntityDef must be an object."))
    }

    @Test
    fun `The property is not found in the enum class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            enum class Color { RED, BLUE }
            @KomapperEntity
            data class Dept(
                @KomapperId
                val id: Int,
                @KomapperEnum(EnumType.PROPERTY, "unknown")
                val color: Color
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"unknown\" is not found in the test.Color. KomapperEnum's hint property is incorrect."))
    }

    @Test
    fun `The valueClass property must be a value class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            data class ClobString(val value: String)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val description: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The valueClass property must be a value class."))
    }

    @Test
    fun `The constructor must be public`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString private constructor(val value: String)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val description: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The constructor of \"test.ClobString\" must be public."))
    }

    @Test
    fun `The property parameter must be public`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString (private val value: String)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val description: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property parameter of \"test.ClobString\" must be public."))
    }

    @Test
    fun `The property parameter must not be nullable`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString (val value: String?)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val description: String
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property parameter of \"test.ClobString\" must not be nullable."))
    }

    @Test
    fun `The property type does not match the parameter property type in the value class`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString (val value: String)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val number: Int
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"number\" is invalid. The property type does not match the parameter property type in \"test.ClobString\"."))
    }

    @Test
    fun `KomapperAlternate is invalid for enum property types`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString (val value: String)
            enum class Color { RED }
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val color: Color
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("@KomapperAlternate is invalid for enum property types."))
    }

    @Test
    fun `The parameter property type does not match between value classes`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.*
            value class ClobString (val value: String)
            value class Color(val value: ClobString)
            @KomapperEntity
            data class Dept(
                @KomapperAutoIncrement @KomapperId
                val id: Int,
                @KomapperAlternate(ClobString::class)
                val color: Color
            )
            """,
        )
        assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        assertTrue(result.messages.contains("The property \"color\" is invalid. The parameter property type does not match between \"test.Color\" and \"test.ClobString\"."))
    }
}
