package org.komapper.core.jdbc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.komapper.jdbc.EnumType
import kotlin.reflect.KClass

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
