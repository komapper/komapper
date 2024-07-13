package integration.core

import org.junit.jupiter.api.Assertions.assertNotNull
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.options.DeleteOptions
import org.komapper.core.dsl.options.InsertOptions
import org.komapper.core.dsl.options.SchemaOptions
import org.komapper.core.dsl.options.ScriptOptions
import org.komapper.core.dsl.options.SelectOptions
import org.komapper.core.dsl.options.TemplateExecuteOptions
import org.komapper.core.dsl.options.TemplateSelectOptions
import org.komapper.core.dsl.options.UpdateOptions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class QueryDslTest {

    @Test
    fun objectReference() {
        val dsl1 = QueryDsl
        val dsl2 = QueryDsl
        assertEquals(dsl1, dsl2)
    }

    @Test
    fun instantiation() {
        val dsl1 = QueryDsl()
        val dsl2 = QueryDsl
        assertNotEquals(dsl1, dsl2)
    }

    @Test
    fun deleteOptions() {
        val a = Meta.address

        val expected = DeleteOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: DeleteOptions? = null
        QueryDsl(deleteOptions = expected)
            .delete(a)
            .single(Address(1, "", 1))
            .options {
                actual = it
                it
            }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun insertOptions() {
        val a = Meta.address

        val expected = InsertOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: InsertOptions? = null
        QueryDsl(insertOptions = expected)
            .insert(a)
            .single(Address(1, "", 1))
            .options {
                actual = it
                it
            }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun schemaOptions_create() {
        val a = Meta.address

        val expected = SchemaOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: SchemaOptions? = null
        QueryDsl(schemaOptions = expected).create(a).options {
            actual = it
            it
        }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun schemaOptions_drop() {
        val a = Meta.address

        val expected = SchemaOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: SchemaOptions? = null
        QueryDsl(schemaOptions = expected).drop(a).options {
            actual = it
            it
        }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun scripOptions() {
        val expected = ScriptOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: ScriptOptions? = null
        QueryDsl(scriptOptions = expected).executeScript("").options {
            actual = it
            it
        }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun selectOptions() {
        val a = Meta.address

        val expected = SelectOptions(fetchSize = 10, queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: SelectOptions? = null
        QueryDsl(selectOptions = expected).from(a).options {
            actual = it
            it
        }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun templateSelectOptions() {
        val expected = TemplateSelectOptions(fetchSize = 10, queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: TemplateSelectOptions? = null
        QueryDsl(templateSelectOptions = expected).fromTemplate("").options {
            actual = it
            it
        }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun templateExecuteOptions() {
        val expected = TemplateExecuteOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: TemplateExecuteOptions? = null
        QueryDsl(templateExecuteOptions = expected).executeTemplate("").options {
            actual = it
            it
        }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun updateOptions() {
        val a = Meta.address

        val expected = UpdateOptions(queryTimeoutSeconds = 20, suppressLogging = true)
        var actual: UpdateOptions? = null
        QueryDsl(updateOptions = expected)
            .update(a)
            .single(Address(1, "", 1))
            .options {
                actual = it
                it
            }
        assertNotNull(actual)
        assertEquals(expected, actual)
    }
}
