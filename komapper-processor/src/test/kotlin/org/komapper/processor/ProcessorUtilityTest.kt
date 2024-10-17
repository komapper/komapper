package org.komapper.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Variance
import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Tag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

@Tag("slow")
internal class ProcessorUtilityTest : AbstractKspTest() {
    @Test
    fun toPropertyNameFormat() {
        assertEquals("aaaBbbCcc", toCamelCase("AaaBbbCcc"))
        assertEquals("aa5BbbCcc", toCamelCase("Aa5BbbCcc"))
        assertEquals("a5aBbbCcc", toCamelCase("A5aBbbCcc"))
        assertEquals("vAddress", toCamelCase("VAddress"))
        assertEquals("uuidTest", toCamelCase("UUIDTest"))
    }

    @Test
    fun `resolve typeArgument of Iterable from List - INVARIANT`() {
        val result = compile(
            """
            package test
            annotation class TestAnnotation
            @TestAnnotation
            data class Dept(val list: List<String>)
            """,
        ) { _, resolver ->
            val symbols = resolver.getSymbolsWithAnnotation("test.TestAnnotation").toList()
            assertEquals(1, symbols.size)

            val klass = symbols.first() as KSClassDeclaration
            assertEquals("Dept", klass.simpleName.asString())

            val property = klass.getDeclaredProperties().first()
            assertEquals("list", property.simpleName.asString())

            val listType = property.type.resolve()

            val typeArgs = resolveTypeArgumentsOfAncestor(listType, resolver.builtIns.iterableType)
            assertEquals(1, typeArgs.size)
            val typeArg = typeArgs.single()
            assertEquals(Variance.INVARIANT, typeArg.variance)
            assertEquals(resolver.builtIns.stringType, typeArg.type?.resolve())

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `resolve typeArgument of Iterable from List - STAR`() {
        val result = compile(
            """
            package test
            annotation class TestAnnotation
            @TestAnnotation
            data class Dept(val list: List<*>)
            """,
        ) { _, resolver ->
            val symbols = resolver.getSymbolsWithAnnotation("test.TestAnnotation").toList()
            assertEquals(1, symbols.size)

            val klass = symbols.first() as KSClassDeclaration
            assertEquals("Dept", klass.simpleName.asString())

            val property = klass.getDeclaredProperties().first()
            assertEquals("list", property.simpleName.asString())

            val listType = property.type.resolve()

            val typeArgs = resolveTypeArgumentsOfAncestor(listType, resolver.builtIns.iterableType)
            assertEquals(1, typeArgs.size)
            val typeArg = typeArgs.single()
            assertEquals(Variance.STAR, typeArg.variance)
            assertNull(typeArg.type?.resolve())

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `resolve typeArgument of One - INVARIANT`() {
        val result = compile(
            """
            package test
            import org.komapper.core.One
            annotation class TestAnnotation
            @TestAnnotation
            data class Dept(val list: List<String>): One<String>({ error("not implemented") })
            """,
        ) { _, resolver ->
            val symbols = resolver.getSymbolsWithAnnotation("test.TestAnnotation").toList()
            assertEquals(1, symbols.size)

            val klass = symbols.first() as KSClassDeclaration
            assertEquals("Dept", klass.simpleName.asString())

            val descendantType = klass.asStarProjectedType()
            val ancestorType = resolver.getKSNameFromString("org.komapper.core.One").let {
                resolver.getClassDeclarationByName(it)?.asStarProjectedType()
                    ?: error("Class not found: ${it.asString()}")
            }
            val typeArgs = resolveTypeArgumentsOfAncestor(descendantType, ancestorType)
            assertEquals(1, typeArgs.size)
            val typeArg = typeArgs.single()
            assertEquals(Variance.INVARIANT, typeArg.variance)
            assertEquals(resolver.builtIns.stringType, typeArg.type?.resolve())

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
