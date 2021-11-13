package integration.jdbc

import integration.Address
import integration.Department
import integration.Employee
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class SqlSelectQuerySelectTest(private val db: JdbcDatabase) {

    @Test
    fun selectColumn() {
        val a = Address.meta
        val streetList = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
        }
        assertEquals(listOf("STREET 1", "STREET 2"), streetList)
    }

    @Test
    fun selectColumn_first() {
        val a = Address.meta
        val value = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.street)
                .first()
        }
        assertEquals("STREET 1", value)
    }

    @Test
    fun selectColumnsAsPair() {
        val a = Address.meta
        val pairList = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(listOf(1 to "STREET 1", 2 to "STREET 2"), pairList)
    }

    @Test
    fun selectColumnsAsTriple() {
        val a = Address.meta
        val tripleList = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1)
            ),
            tripleList
        )
    }

    @Test
    fun selectColumnsAsRecord() {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, concat(a.street, " test"))
        }
        assertEquals(2, list.size)
        val record0 = list[0]
        assertEquals(1, record0[a.addressId])
        assertEquals("STREET 1", record0[a.street])
        assertEquals(1, record0[a.version])
        assertEquals("STREET 1 test", record0[concat(a.street, " test")])
        val record1 = list[1]
        assertEquals(2, record1[a.addressId])
        assertEquals("STREET 2", record1[a.street])
        assertEquals(1, record1[a.version])
        assertEquals("STREET 2 test", record1[concat(a.street, " test")])
    }

    @Test
    fun selectColumns() {
        val a = Address.meta
        val list = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectColumns(a.addressId, a.street, a.version)
        }
        assertEquals(2, list.size)
        val record0 = list[0]
        assertEquals(1, record0[a.addressId])
        assertEquals("STREET 1", record0[a.street])
        assertEquals(1, record0[a.version])
        val record1 = list[1]
        assertEquals(2, record1[a.addressId])
        assertEquals("STREET 2", record1[a.street])
        assertEquals(1, record1[a.version])
    }

    @Test
    fun selectEntity() {
        val a = Address.meta
        val e = Employee.meta
        val list: List<Address> = db.runQuery {
            QueryDsl.from(a)
                .leftJoin(e) {
                    a.addressId eq e.addressId
                }
                .orderBy(a.addressId)
        }
        assertEquals(15, list.size)
    }

    @Test
    fun selectColumnsAsPair_scalar() {
        val d = Department.meta
        val e = Employee.meta
        val subquery = QueryDsl.from(e).where { d.departmentId eq e.departmentId }.select(count())
        val list = db.runQuery {
            QueryDsl.from(d)
                .orderBy(d.departmentId)
                .select(d.departmentName, subquery)
        }
        assertEquals(4, list.size)
        assertEquals("ACCOUNTING" to 3L, list[0])
        assertEquals("RESEARCH" to 5L, list[1])
        assertEquals("SALES" to 6L, list[2])
        assertEquals("OPERATIONS" to 0L, list[3])
    }
}
