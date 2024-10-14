package org.komapper.processor.command

import com.tschuchort.compiletesting.KotlinCompilation
import org.junit.jupiter.api.Tag
import org.komapper.core.template.sql.SqlException
import org.komapper.processor.AbstractKspTest
import org.komapper.processor.Config
import org.komapper.processor.Context
import org.komapper.processor.ContextFactory
import org.komapper.processor.getClassDeclaration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@Tag("slow")
class SqlReassemblerTest : AbstractKspTest() {
    @Test
    fun success() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql =
                """
                /**
                 * multi line comment
                 */
                -- single line comment
                select
                    *
                from
                    dept
                where
                    /*%for id in list */
                    id = /* id */1
                    /*%if id_has_next *//*# "or" *//*%end */
                    /*%end */
                union all
                select
                    * 
                from
                    emp 
                where 
                    id = /* id */1
                    and
                    name = /*^ name */'foo'
                /*%if orderBy != null*/
                /*# orderBy */
                /*%elseif orderBy == "hoge" */
                order by hoge
                /*%elseif orderBy == "foo" */
                order by foo
                /*%else */
                order by id
                /*%end */
                """.trimIndent()
            val newSql = SqlReassembler(context, sql, emptyMap()).assemble()
            assertEquals(sql, newSql)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `success - partial binding`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("limit /* limit */0 offset /* offset */0")
            data class Pagination(val limit: Int, val offset: Int)
            """,
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val paginationType = context.getClassDeclaration("test.Pagination") { error(it) }.asType(emptyList())
            val sql =
                """
                select
                    *
                from
                    dept
                /*> pagination */
                """.trimIndent()
            val newSql = SqlReassembler(context, sql, mapOf("pagination" to paginationType)).assemble()
            assertEquals(
                """
                select
                    *
                from
                    dept
                /*%if pagination != null *//*%with pagination */limit /* limit */0 offset /* offset */0/*%end *//*%end */
                """.trimIndent(),
                newSql,
            )

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `success - partial embeddable`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("/*# value */")
            data class Partial(val value: Int)
            """,
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val partialType = context.getClassDeclaration("test.Partial") { error(it) }.asType(emptyList())
            val sql =
                """
                select
                    *
                from
                    dept
                /*> partial */
                """.trimIndent()
            val newSql = SqlReassembler(context, sql, mapOf("partial" to partialType)).assemble()
            assertEquals(
                """
                select
                    *
                from
                    dept
                /*%if partial != null *//*%with partial *//*# value *//*%end *//*%end */
                """.trimIndent(),
                newSql,
            )

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `success - partial literal`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("/*^ value */''")
            data class Partial(val value: Int)
            """,
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val partialType = context.getClassDeclaration("test.Partial") { error(it) }.asType(emptyList())
            val sql =
                """
                select
                    *
                from
                    dept
                /*> partial */
                """.trimIndent()
            val newSql = SqlReassembler(context, sql, mapOf("partial" to partialType)).assemble()
            assertEquals(
                """
                select
                    *
                from
                    dept
                /*%if partial != null *//*%with partial *//*^ value */''/*%end *//*%end */
                """.trimIndent(),
                newSql,
            )

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `success - partial if`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("/*%if value == 0 */0/*%elseif value == 1 */1/*%elseif value == 2 */2/*%else */3/*%end */")
            data class Partial(val value: Int)
            """,
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val partial = context.getClassDeclaration("test.Partial") { error(it) }.asType(emptyList())
            val sql =
                """
                select
                    *
                from
                    x
                /*> partial */
                """.trimIndent()
            val newSql = SqlReassembler(context, sql, mapOf("partial" to partial)).assemble()
            assertEquals(
                """
                select
                    *
                from
                    x
                /*%if partial != null *//*%with partial *//*%if value == 0 */0/*%elseif value == 1 */1/*%elseif value == 2 */2/*%else */3/*%end *//*%end *//*%end */
                """.trimIndent(),
                newSql,
            )

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `success - partial for`() {
        val result = compile(
            """
            package test
            import org.komapper.annotation.KomapperPartial
            @KomapperPartial("/*%for item in items *//* item */''/*%end */")
            data class Partial(val items: List<String>)
            """,
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val partial = context.getClassDeclaration("test.Partial") { error(it) }.asType(emptyList())
            val sql =
                """
                select
                    *
                from
                    x
                /*> partial */
                """.trimIndent()
            val newSql = SqlReassembler(context, sql, mapOf("partial" to partial)).assemble()
            assertEquals(
                """
                select
                    *
                from
                    x
                /*%if partial != null *//*%with partial *//*%for item in items *//* item */''/*%end *//*%end *//*%end */
                """.trimIndent(),
                newSql,
            )

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `error - SqlPartialEvaluationException`() {
        val result = compile("") { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val sql =
                """
                select
                    *
                from
                    dept
                /*> orderBy */
                """.trimIndent()
            val e = assertFailsWith<SqlException> {
                SqlReassembler(context, sql, emptyMap()).assemble()
            }
            println(e)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }

    @Test
    fun `error - SqlPartialAnnotationNotFoundException`() {
        val result = compile(
            """
            package test
            object OrderBy
            """,
        ) { env, resolver ->
            val context = ContextFactory { Context(env, Config.create(env.options), it) }.create(resolver)
            val orderByType = context.getClassDeclaration("test.OrderBy") { error(it) }.asType(emptyList())
            val sql =
                """
                select
                    *
                from
                    dept
                /*> orderBy */
                """.trimIndent()
            val e = assertFailsWith<SqlReassembler.SqlPartialAnnotationNotFoundException> {
                SqlReassembler(context, sql, mapOf("orderBy" to orderByType)).assemble()
            }
            println(e)

            emptyList()
        }
        assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
    }
}
