package org.komapper.processor.command

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Variance
import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Tag
import org.komapper.processor.AbstractKspTest
import org.komapper.processor.Config
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import org.komapper.processor.getClassDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("slow")
class ExprValidatorTest : AbstractKspTest() {
    sealed interface Color {
        object Red : Color
        object Green : Color
        object Blue : Color
    }

    @Test
    fun `perform logical operator - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.booleanType)
            val result = validator.validate("!a", paramMap)
            assertEquals(resolver.builtIns.booleanType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `perform logical operator - NonBooleanTypeException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.intType)
            val ex = assertFailsWith<ExprValidator.NonBooleanTypeException> {
                validator.validate("!a", paramMap)
            }
            println(ex)
            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `perform binary logical operator - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf(
                "a" to resolver.builtIns.booleanType,
                "b" to resolver.builtIns.booleanType,
                "c" to resolver.builtIns.booleanType,
            )
            val result = validator.validate("(a || b) && c", paramMap)
            assertEquals(resolver.builtIns.booleanType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `perform binary logical operator - EitherOperandNonBooleanException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf(
                "a" to resolver.builtIns.booleanType,
                "b" to resolver.builtIns.booleanType,
                "c" to resolver.builtIns.stringType,
            )
            val ex = assertFailsWith<ExprValidator.EitherOperandNonBooleanException> {
                validator.validate("(a || b) && c", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `compare - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.intType)
            val result = validator.validate("a < 0", paramMap)
            assertEquals(resolver.builtIns.booleanType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `compare - NonSameTypeException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.intType.makeNullable())
            val ex = assertFailsWith<ExprValidator.NonSameTypeException> {
                validator.validate("1 < a", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `compare - NonComparableTypeException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.intType.makeNullable())
            val ex = assertFailsWith<ExprValidator.NonComparableTypeException> {
                validator.validate("a < a", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `is - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val classDeclaration = context.getClassDeclaration("org.komapper.processor.command.ExprValidatorTest.Color.Red") { error(it) }
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to classDeclaration.asType(emptyList()))
            val result = validator.validate("a is @org.komapper.processor.command.ExprValidatorTest\$Color@", paramMap)
            assertEquals(resolver.builtIns.booleanType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `is - NotClassRefNodeException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val classDeclaration = context.getClassDeclaration("org.komapper.processor.command.ExprValidatorTest.Color.Red") { error(it) }
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to classDeclaration.asType(emptyList()))
            val ex = assertFailsWith<ExprValidator.NotClassRefNodeException> {
                validator.validate("a is 123", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `classRef - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = emptyMap<String, KSType>()
            val result = validator.validate("@kotlin.collections.Iterable@", paramMap)
            assertEquals(resolver.builtIns.iterableType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `classRef - ClassNotFoundException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = emptyMap<String, KSType>()
            val ex = assertFailsWith<ExprValidator.ClassNotFoundException> {
                validator.validate("@kotlin.collections.Unknown@", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `value - ParameterNotFoundException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = emptyMap<String, KSType>()
            val ex = assertFailsWith<ExprValidator.ParameterNotFoundException> {
                validator.validate("unknown", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `callableValue - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val typeRef = context.resolver.createKSTypeReferenceFromKSType(context.resolver.builtIns.stringType)
            val typeArg = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
            val function0Type = context.getClassDeclaration("kotlin.Function0") { error(it) }.asType(listOf(typeArg))
            val paramMap = mapOf("a" to function0Type)
            val result = validator.validate("a()", paramMap)
            assertEquals(context.resolver.builtIns.stringType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `callableValue - InvokeFunctionNotFoundException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to context.resolver.builtIns.intType)
            val ex = assertFailsWith<ExprValidator.InvokeFunctionNotFoundException> {
                validator.validate("a()", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `callableValue - ArgumentCountMismatchException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val typeRef = context.resolver.createKSTypeReferenceFromKSType(context.resolver.builtIns.stringType)
            val typeArg = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
            val function0Type = context.getClassDeclaration("kotlin.Function0") { error(it) }.asType(listOf(typeArg))
            val paramMap = mapOf("a" to function0Type)
            val ex = assertFailsWith<ExprValidator.ArgumentCountMismatchException> {
                validator.validate("a(1)", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `property - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.stringType)
            val result = validator.validate("a.length", paramMap)
            assertEquals(resolver.builtIns.intType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `property - PropertyNotFoundException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.stringType)
            val ex = assertFailsWith<ExprValidator.PropertyNotFoundException> {
                validator.validate("a.unknown", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Enum property - success`() {
        val result = compile(
            """
            package test
            enum class MyEnum(val value: String) {
                A("a"), B("b"), C("c")
            }
            class MyClass {
                val myEnum = MyEnum.A
            }
            """.trimIndent(),
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val decl = context.getClassDeclaration("test.MyClass") { error(it) }
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to decl.asType(emptyList()))
            val result = validator.validate("a.myEnum.value", paramMap)
            assertEquals(resolver.builtIns.stringType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Enum property - PropertyNotFoundException`() {
        val result = compile(
            """
            package test
            enum class MyEnum(val value: String) {
                A("a"), B("b"), C("c")
            }
            class MyClass {
                val myEnum = MyEnum.A
            }
            """.trimIndent(),
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val decl = context.getClassDeclaration("test.MyClass") { error(it) }
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to decl.asType(emptyList()))
            val ex = assertFailsWith<ExprValidator.PropertyNotFoundException> {
                validator.validate("a.myEnum.unknown", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Enum entry - success`() {
        val result = compile(
            """
            package test
            enum class MyEnum(val value: String) {
                A("a"), B("b"), C("c")
            }
            class MyClass {
                val myEnum = MyEnum.A
            }
            """.trimIndent(),
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val classDecl = context.getClassDeclaration("test.MyClass") { error(it) }
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to classDecl.asType(emptyList()))
            val result = validator.validate("a.myEnum.C", paramMap)
            val enumDecl = context.getClassDeclaration("test.MyEnum") { error(it) }
            val enumEntryType = enumDecl.declarations
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.ENUM_ENTRY }
                .filter { it.simpleName.asString() == "C" }
                .single().asType(emptyList())
            assertEquals(enumEntryType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `Enum entry - PropertyNotFoundException`() {
        val result = compile(
            """
            package test
            enum class MyEnum(val value: String) {
                A("a"), B("b"), C("c")
            }
            class MyClass {
                val myEnum = MyEnum.A
            }
            """.trimIndent(),
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val classDecl = context.getClassDeclaration("test.MyClass") { error(it) }
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to classDecl.asType(emptyList()))
            val ex = assertFailsWith<ExprValidator.PropertyNotFoundException> {
                validator.validate("a.myEnum.D", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.stringType)
            val result = validator.validate("a.subSequence(0, 1)", paramMap)
            val charSequenceType = context.getClassDeclaration("kotlin.CharSequence") { error(it) }.asType(emptyList())
            assertEquals(charSequenceType, result.type)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `function - FunctionNotFoundException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val validator = ExprValidator(context)
            val paramMap = mapOf("a" to resolver.builtIns.stringType)
            val ex = assertFailsWith<ExprValidator.FunctionNotFoundException> {
                validator.validate("a.subSequence(0)", paramMap)
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
