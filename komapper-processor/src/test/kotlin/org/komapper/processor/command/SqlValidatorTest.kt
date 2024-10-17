package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Variance
import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Tag
import org.komapper.processor.AbstractKspTest
import org.komapper.processor.Config
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("slow")
class SqlValidatorTest : AbstractKspTest() {
    @Test
    fun `bind variable - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/* items */(1, 2, 3)"
            val iterableDecl = resolver.builtIns.iterableType.declaration as KSClassDeclaration
            val typeRef = resolver.createKSTypeReferenceFromKSType(resolver.builtIns.intType)
            val typeArg = resolver.getTypeArgument(typeRef, Variance.INVARIANT)
            val iterableType = iterableDecl.asType(listOf(typeArg))
            val paramMap = mapOf("items" to iterableType)
            val usedParams = SqlValidator(context, sql, paramMap).validate()
            assertEquals(setOf("items"), usedParams)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `bind variable - ExprMustBeIterableException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/* items */(1, 2, 3)"
            val paramMap = mapOf("items" to resolver.builtIns.intType)
            val ex = assertFailsWith<SqlValidator.ExprMustBeIterableException> {
                SqlValidator(context, sql, paramMap).validate()
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `if - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%if a */ /*%end */"
            val paramMap = mapOf("a" to resolver.builtIns.booleanType)
            val usedParams = SqlValidator(context, sql, paramMap).validate()
            assertEquals(setOf("a"), usedParams)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `if - ExprMustBeBooleanException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%if a */ /*%end */"
            val paramMap = mapOf("a" to resolver.builtIns.stringType)
            val ex = assertFailsWith<SqlValidator.ExprMustBeBooleanException> {
                SqlValidator(context, sql, paramMap).validate()
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `elseif - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%if a */ /*%elseif b */ /*%end */"
            val paramMap = mapOf("a" to resolver.builtIns.booleanType, "b" to resolver.builtIns.booleanType)
            val usedParams = SqlValidator(context, sql, paramMap).validate()
            assertEquals(setOf("a", "b"), usedParams)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `elseif - ExprMustBeBooleanException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%if a */ /*%elseif b */ /*%end */"
            val paramMap = mapOf("a" to resolver.builtIns.booleanType, "b" to resolver.builtIns.stringType)
            val ex = assertFailsWith<SqlValidator.ExprMustBeBooleanException> {
                SqlValidator(context, sql, paramMap).validate()
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `for - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%for item in items */ /*%end */"
            val iterableDecl = resolver.builtIns.iterableType.declaration as KSClassDeclaration
            val typeRef = resolver.createKSTypeReferenceFromKSType(resolver.builtIns.stringType)
            val typeArg = resolver.getTypeArgument(typeRef, Variance.INVARIANT)
            val iterableType = iterableDecl.asType(listOf(typeArg))
            val paramMap = mapOf("items" to iterableType)
            val usedParams = SqlValidator(context, sql, paramMap).validate()
            assertEquals(setOf("items"), usedParams)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `for - special variables - success`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%for item in items */ /*# item_index */ /*# item_has_next */ /*# item_next_comma */ /*# item_next_and */ /*# item_next_or */ /*%end */"
            val iterableDecl = resolver.builtIns.iterableType.declaration as KSClassDeclaration
            val typeRef = resolver.createKSTypeReferenceFromKSType(resolver.builtIns.stringType)
            val typeArg = resolver.getTypeArgument(typeRef, Variance.INVARIANT)
            val iterableType = iterableDecl.asType(listOf(typeArg))
            val paramMap = mapOf("items" to iterableType)
            val usedParams = SqlValidator(context, sql, paramMap).validate()
            assertEquals(setOf("items", "item_index", "item_has_next", "item_next_comma", "item_next_and", "item_next_or"), usedParams)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `for - ExprMustBeIterableException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%for item in items */ /*%end */"
            val paramMap = mapOf("items" to resolver.builtIns.stringType)
            val ex = assertFailsWith<SqlValidator.ExprMustBeIterableException> {
                SqlValidator(context, sql, paramMap).validate()
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `for - StarProjectionNotSupportedException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql = "/*%for item in items */ /*%end */"
            val paramMap = mapOf("items" to resolver.builtIns.iterableType)
            val ex = assertFailsWith<SqlValidator.StarProjectionNotSupportedException> {
                SqlValidator(context, sql, paramMap).validate()
            }
            println(ex)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
