package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.IdentityStrategy
import integration.core.Run
import integration.core.SequenceStrategy
import integration.core.address
import integration.core.identityStrategy
import integration.core.sequenceStrategy
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dryRunQuery
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.metamodel.IdGenerator
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(JdbcEnv::class)
class JdbcInsertValuesReturningTest(private val db: JdbcDatabase) {

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun test() {
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
                a.version eq 0
            }.returning()
        }
        assertEquals(Address(19, "STREET 16", 0), address)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun testReturningSingleColumn() {
        val a = Meta.address
        val street = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
                a.version eq 0
            }.returning(a.street)
        }
        assertEquals("STREET 16", street)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun testReturningPairColumns() {
        val a = Meta.address
        val (street, version) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
                a.version eq 0
            }.returning(a.street, a.version)
        }
        assertEquals("STREET 16", street)
        assertEquals(0, version)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun testReturningTripleColumns() {
        val a = Meta.address
        val (street, version, addressId) = db.runQuery {
            QueryDsl.insert(a).values {
                a.addressId eq 19
                a.street eq "STREET 16"
                a.version eq 0
            }.returning(a.street, a.version, a.addressId)
        }
        assertEquals("STREET 16", street)
        assertEquals(0, version)
        assertEquals(19, addressId)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun generatedKeys_autoIncrement() {
        val a = Meta.identityStrategy
        val strategy = db.runQuery {
            QueryDsl.insert(a).values {
                a.id eq 10
                a.value eq "test"
            }.returning()
        }
        assertEquals(IdentityStrategy(1, "test"), strategy)
    }

    @Run(onlyIf = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun generatedKeys_sequence() {
        val generator = Meta.sequenceStrategy.idGenerator() as IdGenerator.Sequence<*, *>
        generator.clear()

        val a = Meta.sequenceStrategy
        val strategy = db.runQuery {
            QueryDsl.insert(a).values {
                a.value eq "test"
            }.returning()
        }
        assertEquals(SequenceStrategy(1, "test"), strategy)
    }

    @Run(unless = [Dbms.H2, Dbms.MARIADB, Dbms.POSTGRESQL])
    @Test
    fun unsupportedOperationException() {
        val a = Meta.address
        val ex = assertFailsWith<UnsupportedOperationException> {
            db.runQuery {
                QueryDsl.insert(a).values {
                    a.addressId eq 19
                    a.street eq "STREET 16"
                    a.version eq 0
                }.returning()
            }
            Unit
        }
        println(ex)
    }

    @Test
    fun dryRun() {
        val a = Meta.address
        val query = QueryDsl.insert(a).values {
            a.addressId eq 19
            a.street eq "STREET 16"
            a.version eq 0
        }.returning()
        println(db.dryRunQuery(query))
    }
}
