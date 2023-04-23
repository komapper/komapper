package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import integration.core.address
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcUpdateSetReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun test() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }.returning()
        }
        assertEquals(1, list.size)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(list.single(), address)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningSingleColumn() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }.returning(a.street)
        }
        assertEquals(1, list.size)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(list.single(), address.street)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningPairColumns() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }.returning(a.street, a.version)
        }
        assertEquals(1, list.size)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(list.single(), address.street to address.version)
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testReturningTripleColumns() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }.returning(a.street, a.version, a.addressId)
        }
        assertEquals(1, list.size)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(list.single(), Triple(address.street, address.version, address.addressId))
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun testMultipleUpdate() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.update(a).set {
                a.version eq 100
            }.where {
                a.addressId inList listOf(1, 2)
            }.returning()
        }
        assertEquals(2, list.size)
        val list2 = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId inList listOf(1, 2)
            }
        }
        assertTrue(list.all { it.version == 100 })
        assertEquals(list.toSet(), list2.toSet())
    }

    @Run(onlyIf = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun incrementVersion_auto() {
        val a = Meta.address
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.first() }
        assertEquals(1, address1.version)

        val versions = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }.returning(a.version)
        }
        assertEquals(1, versions.size)
        assertEquals(2, versions.single())

        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(2, address2.version)
    }

    @Run(unless = [Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun unsupportedOperationException_updateReturning() {
        val a = Meta.address
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery {
                QueryDsl.update(a).set {
                    a.street eq "STREET 16"
                }.where {
                    a.addressId eq 1
                }.returning()
            }
            Unit
        }
        println(ex)
    }

    @Test
    fun dryRun() {
        val a = Meta.address
        val query = QueryDsl.update(a).set {
            a.street eq "STREET 16"
        }.where {
            a.addressId eq 1
        }.returning()
        println(db.dryRunQuery(query))
    }
}
