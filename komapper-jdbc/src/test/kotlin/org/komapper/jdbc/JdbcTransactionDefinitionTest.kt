package org.komapper.jdbc

import kotlin.test.Test
import kotlin.test.assertEquals

class JdbcTransactionDefinitionTest {

    @Test
    fun plus_same_keys() {
        val def =
            JdbcIsolationLevel.READ_COMMITTED + JdbcIsolationLevel.SERIALIZABLE + JdbcIsolationLevel.READ_UNCOMMITTED
        assertEquals(JdbcIsolationLevel.READ_UNCOMMITTED, def[JdbcIsolationLevel])
    }

    @Test
    fun plus_different_keys() {
        val def = JdbcIsolationLevel.READ_COMMITTED + JdbcTransactionName("aaa") + JdbcTransactionReadOnly(true)
        assertEquals(JdbcIsolationLevel.READ_COMMITTED, def[JdbcIsolationLevel])
        assertEquals(JdbcTransactionName("aaa"), def[JdbcTransactionName])
        assertEquals(JdbcTransactionReadOnly(true), def[JdbcTransactionReadOnly])
    }
}
