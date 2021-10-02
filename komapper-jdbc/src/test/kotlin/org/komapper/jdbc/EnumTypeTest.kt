package org.komapper.jdbc

import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals

internal class EnumTypeTest {

    @Suppress("unused")
    enum class Direction {
        NORTH, SOUTH, WEST, EAST
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun toEnumConstant() {
        val kClass = Direction::class as KClass<Enum<*>>
        val enumType = EnumType(kClass, "enum")
        val constant = enumType.toEnumConstant("WEST")
        assertEquals(Direction.WEST, constant)
    }
}
