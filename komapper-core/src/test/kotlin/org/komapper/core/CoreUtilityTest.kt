package org.komapper.core

import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CoreUtilityTest {
    @Test
    fun mustNotBeMarkedNullable_nonNullable() {
        MyData.nonNullable.mustNotBeMarkedNullable(MyData::class.qualifiedName, "nonNullable")
    }

    @Test
    fun mustNotBeMarkedNullable_nullable() {
        val th = assertFailsWith<IllegalStateException> {
            MyData.nullable.mustNotBeMarkedNullable(MyData::class.qualifiedName, "nullable")
        }
        val expected = "The 'nullable' property of 'org.komapper.core.MyData' must not be marked as nullable: java.lang.String? (Kotlin reflection is not available)"
        assertEquals(expected, th.message)
    }
}

object MyData {
    val nonNullable: KType = typeOf<String>()
    val nullable: KType = typeOf<String?>()
}
