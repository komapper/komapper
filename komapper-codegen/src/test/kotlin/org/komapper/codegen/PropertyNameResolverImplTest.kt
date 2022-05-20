package org.komapper.codegen

import kotlin.test.Test
import kotlin.test.assertEquals

class PropertyNameResolverImplTest {
    
    private val converter = PropertyNameResolverImpl()
    
    @Test
    fun resolve() {
        assertEquals("abc", converter.resolve(MutableColumn().apply { name = "abc" }))
        assertEquals("`class`", converter.resolve(MutableColumn().apply { name = "class" }))
    }

}