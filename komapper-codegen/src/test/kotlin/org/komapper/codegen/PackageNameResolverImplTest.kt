package org.komapper.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class PackageNameResolverImplTest {
    private val converter = PackageNameResolverImpl()

    @Test
    fun resolve() {
        assertEquals("abc", converter.resolve("abc"))
        assertEquals("`class`", converter.resolve("class"))
        assertEquals("example.`class`", converter.resolve("example.class"))
    }
}
