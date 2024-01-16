package integration.jdbc

import integration.core.Address
import integration.core.AddressDto
import integration.core.address
import integration.core.department
import integration.core.employee
import integration.core.selectAsAddress
import integration.core.selectAsAddressDto
import integration.core.selectAsDepartmentDto
import integration.core.selectAsRobot
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.transform
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(JdbcEnv::class)
class JdbcSelectProjectionTest(private val db: JdbcDatabase) {

    @Test
    fun selectSingleColumn() {
        val a = Meta.address
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
    fun selectSingleColumn_first() {
        val a = Meta.address
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
    fun selectPairColumns() {
        val a = Meta.address
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
    fun selectTripleColumns() {
        val a = Meta.address
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
                Triple(2, "STREET 2", 1),
            ),
            tripleList,
        )
    }

    @Test
    fun selectRecord() {
        val a = Meta.address
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
        val a = Meta.address
        val list = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectAsRecord(a.addressId, a.street, a.version)
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
        val a = Meta.address
        val e = Meta.employee
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
    fun selectPairColumns_scalar() {
        val d = Meta.department
        val e = Meta.employee
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

    @Test
    fun selectSingleNotNullColumn() {
        val a = Meta.address
        val streetList: List<String> = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectNotNull(a.street)
        }
        assertEquals(listOf("STREET 1", "STREET 2"), streetList)
    }

    @Test
    fun selectPairNotNullColumns() {
        val a = Meta.address
        val pairList: List<Pair<Int, String>> = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street)
        }
        assertEquals(listOf(1 to "STREET 1", 2 to "STREET 2"), pairList)
    }

    @Test
    fun selectTripleNotNullColumns() {
        val a = Meta.address
        val tripleList: List<Triple<Int, String, Int>> = db.runQuery {
            QueryDsl.from(a)
                .where {
                    a.addressId inList listOf(1, 2)
                }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            tripleList,
        )
    }

    @Test
    fun selectSingleNotNullColumn_error() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.managerId)
            }
            Unit
        }
        println(ex.message)
    }

    @Test
    fun selectPairNotNullColumn_error() {
        val e = Meta.employee
        val ex1 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.managerId, e.employeeId)
            }
            Unit
        }
        println(ex1.message)

        val ex2 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.employeeId, e.managerId)
            }
            Unit
        }
        println(ex2.message)
    }

    @Test
    fun selectTripleNotNullColumn_error() {
        val e = Meta.employee
        val ex1 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.managerId, e.employeeId, e.addressId)
            }
            Unit
        }
        println(ex1.message)

        val ex2 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.employeeId, e.managerId, e.addressId)
            }
            Unit
        }
        println(ex2.message)

        val ex3 = assertFailsWith<IllegalStateException> {
            db.runQuery {
                QueryDsl.from(e).selectNotNull(e.employeeId, e.addressId, e.managerId)
            }
            Unit
        }
        println(ex3.message)
    }

    @Test
    fun selectAsEntity() {
        val a = Meta.address
        val e = Meta.employee

        val query = QueryDsl.from(e)
            .where { e.employeeName startsWith "S" }
            .selectAsEntity(a, e.employeeId, concat(e.employeeName, " STREET"), e.version)

        val list = db.runQuery(query)
        val expected = listOf(
            Address(1, "SMITH STREET", 1),
            Address(8, "SCOTT STREET", 1),
        )
        assertEquals(expected, list)
    }

    @Test
    fun selectAsEntity_union() {
        val a = Meta.address
        val e = Meta.employee

        val q1 = QueryDsl.from(e)
            .where { e.employeeName startsWith "S" }
            .selectAsEntity(a, e.employeeId, concat(e.employeeName, " STREET"), e.version)
        val q2 = QueryDsl.from(e)
            .where { e.employeeName startsWith "T" }
            .selectAsEntity(a, e.employeeId, concat(e.employeeName, " STREET"), e.version)

        val list = db.runQuery(q1.union(q2).orderBy(e.employeeId))
        val expected = listOf(
            Address(1, "SMITH STREET", 1),
            Address(8, "SCOTT STREET", 1),
            Address(10, "TURNER STREET", 1),
        )
        assertEquals(expected, list)
    }

    @Test
    fun selectAsEntity_typeSafe() {
        val e = Meta.employee

        val query = QueryDsl.from(e)
            .where { e.employeeName startsWith "S" }
            .selectAsAddress(
                version = e.version,
                addressId = e.employeeId,
                street = concat(e.employeeName, " STREET"),
            )

        val list = db.runQuery(query)
        val expected = listOf(
            Address(1, "SMITH STREET", 1),
            Address(8, "SCOTT STREET", 1),
        )
        assertEquals(expected, list)
    }

    @Test
    fun selectAsEntity_typeSafe_embeddable() {
        val e = Meta.employee

        val query = QueryDsl.from(e)
            .where { e.employeeName startsWith "S" }
            .orderBy(e.employeeName)
            .selectAsRobot(
                employeeId = e.employeeId,
                addressId = e.addressId,
                version = e.version,
                departmentId = e.departmentId,
                `info1#employeeName` = e.employeeName,
                `info1#employeeNo` = e.employeeNo,
                `info2#hiredate` = e.hiredate,
                `info2#salary` = e.salary,
                managerId = e.managerId,
            )

        val list = db.runQuery(query)
        assertEquals(2, list.size)
        assertEquals("SCOTT", list[0].info1.employeeName)
        assertEquals("SMITH", list[1].info1.employeeName)
    }

    @Test
    fun selectAsEntity_union_typeSafe() {
        val e = Meta.employee

        val q1 = QueryDsl.from(e)
            .where { e.employeeName startsWith "S" }
            .selectAsAddress(
                version = e.version,
                addressId = e.employeeId,
                street = concat(e.employeeName, " STREET"),
            )
        val q2 = QueryDsl.from(e)
            .where { e.employeeName startsWith "T" }
            .selectAsAddress(
                street = concat(e.employeeName, " STREET"),
                version = e.version,
                addressId = e.employeeId,
            )

        val list = db.runQuery(q1.union(q2).orderBy(e.employeeId))
        val expected = listOf(
            Address(1, "SMITH STREET", 1),
            Address(8, "SCOTT STREET", 1),
            Address(10, "TURNER STREET", 1),
        )
        assertEquals(expected, list)
    }

    @Test
    fun selectAsDto_projection() {
        val a = Meta.address

        val query = QueryDsl.from(a)
            .where { a.addressId eq 10 }
            .selectAsAddressDto(
                idValue = a.addressId,
                streetValue = a.street,
            ).single()

        val dto = db.runQuery(query)
        assertEquals(AddressDto(10, "STREET 10"), dto)
    }

    @Test
    fun selectAsDto_projectionDef() {
        val e = Meta.employee
        val d = Meta.department

        val query = QueryDsl.from(d)
            .innerJoin(e) { d.departmentId eq e.departmentId }
            .groupBy(d.departmentId, d.departmentName)
            .having { d.departmentId eq 1 }
            .selectAsDepartmentDto(
                department = d.departmentName,
                memberCount = count(),
            ).single()

        val dto = db.runQuery(query)
        assertEquals(3, dto.memberCount)
    }

    @Test
    fun selectAsDto_projection_transform() {
        val a = Meta.address

        val query = QueryDsl.from(a)
            .where { a.addressId eq 10 }
            .selectAsAddressDto(
                idValue = a.addressId,
                streetValue = a.addressId.transform { it.toString() },
            ).single()

        val dto = db.runQuery(query)
        assertEquals(AddressDto(10, "10"), dto)
    }
}
