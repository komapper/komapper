package org.komapper.core.dsl.metamodel

import kotlinx.coroutines.runBlocking
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertEquals

internal class IdAssignmentTest {

    data class Emp(val id: Int = 0)
    private fun toId(generatedKey: Long): Int = generatedKey.toInt()
    private val property = object : PropertyMetamodel<Emp, Int, Int> by PropertyMetamodelStub() {
        override val setter: (Emp, Int) -> Emp
            get() = { entity, id -> entity.copy(id = id) }
    }

    @Test
    fun autoIncrement() {
        val autoIncrement = IdAssignment.AutoIncrement(::toId, property)
        val emp = autoIncrement.assign(Emp(), 123L)
        assertEquals(Emp(123), emp)
    }

    @Test
    fun sequence() {
        val startWith = 100
        val incrementBy = 2
        val sequence = IdAssignment.Sequence(
            ::toId,
            property,
            ConcurrentHashMap(),
            "id",
            "catalog",
            "schema",
            true,
            startWith,
            incrementBy,
            false,
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
