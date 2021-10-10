package org.komapper.core.dsl.metamodel

import kotlinx.coroutines.runBlocking
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AssignmentTest {

    data class Emp(val id: Int = 0)
    private fun toId(generatedKey: Long): Int = generatedKey.toInt()
    private fun setId(entity: Emp, id: Int): Emp = entity.copy(id = id)

    @Test
    fun autoIncrement() {
        val autoIncrement = Assignment.AutoIncrement(::toId, ::setId, "ID")
        val emp = autoIncrement.assign(Emp(), 123L)
        assertEquals(Emp(123), emp)
    }

    @Test
    fun sequence() {
        val startWith = 100
        val incrementBy = 2
        val sequence = Assignment.Sequence(
            ::toId,
            ::setId,
            "id",
            "catalog",
            "schema",
            true,
            startWith,
            incrementBy
        )
        val key = UUID.randomUUID()
        runBlocking {
            var sequenceName = ""
            var callCount = 0
            var value: Long = startWith.toLong()
            val enquote: (String) -> String = { "[$it]" }
            val nextValue: suspend (String) -> Long = {
                sequenceName = it
                callCount++
                val result = value
                value += incrementBy
                result
            }
            val emp = sequence.assign(Emp(), key, enquote, nextValue)
            assertEquals("[catalog].[schema].[id]", sequenceName)
            assertEquals(1, callCount)
            assertEquals(102, value)
            assertEquals(Emp(100), emp)
            val emp2 = sequence.assign(Emp(), key, enquote, nextValue)
            assertEquals(1, callCount)
            assertEquals(102, value)
            assertEquals(Emp(101), emp2)
            val emp3 = sequence.assign(Emp(), key, enquote, nextValue)
            assertEquals(2, callCount)
            assertEquals(104, value)
            assertEquals(Emp(102), emp3)
        }
    }
}
