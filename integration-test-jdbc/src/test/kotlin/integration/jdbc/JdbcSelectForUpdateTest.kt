package integration.jdbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(JdbcEnv::class)
class JdbcSelectForUpdateTest(private val db: JdbcDatabase) {
    @Test
    fun forUpdate() {
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a).where { a.addressId inList listOf(9, 10) }
                .orderBy(a.addressId.desc())
                .forUpdate()
        }
        assertEquals(
            listOf(
                Address(10, "STREET 10", 1),
                Address(9, "STREET 9", 1),
            ),
            list,
        )
    }

    @Run(onlyIf = [Dbms.H2])
    @Test
    fun forUpdate_unsupportedException() {
        val a = Meta.address
        assertFailsWith<UnsupportedOperationException> {
            db.runQuery {
                QueryDsl.from(a)
                    .where { a.addressId eq 10 }
                    .forUpdate { nowait() }
                    .first()
            }
            Unit
        }
    }

    @Run(onlyIf = [Dbms.MARIADB, Dbms.MYSQL, Dbms.ORACLE, Dbms.POSTGRESQL, Dbms.SQLSERVER])
    @Test
    fun forUpdate_nowait() {
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a)
                .where { a.addressId eq 10 }
                .forUpdate { nowait() }
                .first()
        }
        assertEquals(Address(10, "STREET 10", 1), address)
    }

    @Run(onlyIf = [Dbms.MARIADB, Dbms.MYSQL, Dbms.ORACLE, Dbms.POSTGRESQL])
    @Test
    fun forUpdate_skipLocked() {
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a)
                .where { a.addressId eq 10 }
                .forUpdate { skipLocked() }
                .first()
        }
        assertEquals(Address(10, "STREET 10", 1), address)
    }

    @Run(onlyIf = [Dbms.MARIADB, Dbms.ORACLE])
    @Test
    fun forUpdate_wait() {
        val a = Meta.address
        val address = db.runQuery {
            QueryDsl.from(a)
                .where { a.addressId eq 10 }
                .forUpdate { wait(1) }
                .first()
        }
        assertEquals(Address(10, "STREET 10", 1), address)
    }

    @Run(onlyIf = [Dbms.ORACLE])
    @Test
    fun forUpdate_of_columns_nowait() {
        val a = Meta.address
        val e = Meta.employee
        val address = db.runQuery {
            QueryDsl.from(a)
                .innerJoin(e) { a.addressId eq e.addressId }
                .where { a.addressId eq 10 }
                .forUpdate {
                    of(a, e).nowait()
                }
                .first()
        }
        assertEquals(Address(10, "STREET 10", 1), address)
    }

    @Run(onlyIf = [Dbms.MYSQL, Dbms.POSTGRESQL])
    @Test
    fun forUpdate_of_tables_nowait() {
        val a = Meta.address
        val e = Meta.employee
        val address = db.runQuery {
            QueryDsl.from(a)
                .innerJoin(e) { a.addressId eq e.addressId }
                .where { a.addressId eq 10 }
                .forUpdate {
                    of(a, e).nowait()
                }
                .first()
        }
        assertEquals(Address(10, "STREET 10", 1), address)
    }
}
