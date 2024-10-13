package integration.r2dbc

import integration.core.Man
import integration.core.Person
import integration.core.address
import integration.core.employee
import integration.core.identityStrategy
import integration.core.man
import integration.core.person
import integration.core.robot
import kotlinx.coroutines.delay
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.operator.times
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

@ExtendWith(R2dbcEnv::class)
class R2dbcUpdateSetTest(private val db: R2dbcDatabase) {
    @Test
    fun test(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals("STREET 16", address.street)
    }

    @Test
    fun setIfNotNull(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street eqIfNotNull null
                a.version eq 10
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals("STREET 1", address.street)
        assertEquals(10, address.version)
    }

    @Test
    fun arithmetic_add(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.version eq (a.version + 10)
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(11, address.version)
    }

    @Test
    fun string_concat(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq (concat(concat("[", a.street), "]"))
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)
        val address = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals("[STREET 1]", address.street)
    }

    @Test
    fun allowMissingWhereClause_default(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.update(e).set {
                    e.employeeName eq "ABC"
                }
            }
        }
        println(ex)
    }

    @Test
    fun missingEmptyWhereClause_true(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.update(e).set {
                e.employeeName eq "ABC"
            }.options { it.copy(allowMissingWhereClause = true) }
        }
        assertEquals(14, count)
    }

    @Test
    fun assignTimestamp_auto(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        db.runQuery {
            QueryDsl.insert(p).single(Person(1, "abc"))
        }
        val person1 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        delay(10)
        val count = db.runQuery {
            QueryDsl.update(p).set {
                p.name eq "ABC"
            }.where {
                p.personId eq 1
            }
        }
        val person2 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        assertEquals(1, count)
        assertNotEquals(person1.updatedAt, person2.updatedAt)
    }

    @Test
    fun assignTimestamp_manual(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.person
        db.runQuery {
            QueryDsl.insert(p).single(Person(1, "abc"))
        }
        val person1 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        delay(10)
        val count = db.runQuery {
            QueryDsl.update(p).set {
                p.name eq "ABC"
                p.updatedAt eq person1.updatedAt
            }.where {
                p.personId eq 1
            }
        }
        val person2 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId eq 1
            }.first()
        }
        assertEquals(1, count)
        assertEquals(person1.updatedAt, person2.updatedAt)
    }

    @Test
    fun incrementVersion_auto(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.first() }
        assertEquals(1, address1.version)

        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)

        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(2, address2.version)
    }

    @Test
    fun incrementVersion_disabled(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val address1 = db.runQuery { QueryDsl.from(a).where { a.addressId eq 1 }.first() }
        assertEquals(1, address1.version)

        val count = db.runQuery {
            QueryDsl.update(a).set {
                a.street eq "STREET 16"
                a.version eq 10
            }.where {
                a.addressId eq 1
            }
        }
        assertEquals(1, count)

        val address2 = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 1
            }.first()
        }
        assertEquals(10, address2.version)
    }

    @Test
    fun nonUpdatableColumn_updateSetManual(info: TestInfo) = inTransaction(db, info) {
        val p = Meta.man
        val findQuery = QueryDsl.from(p).where { p.manId eq 1 }.first()
        val person1 = Man(manId = 1, name = "Alice", createdBy = "nobody", updatedBy = "nobody")
        db.runQuery {
            QueryDsl.insert(p).single(person1)
        }
        val person2 = db.runQuery {
            QueryDsl.update(p)
                .set {
                    p.createdBy eq "somebody"
                    p.updatedBy eq "somebody"
                }
                .where { p.manId eq 1 }
                .andThen(findQuery)
        }
        assertEquals("somebody", person2.createdBy)
        assertEquals("somebody", person2.updatedBy)
    }

    @Test
    fun emptySetClause(info: TestInfo) = inTransaction(db, info) {
        val i = Meta.identityStrategy
        val query = QueryDsl.update(i).set {
        }.where {
            i.id eq 1
        }
        val count = db.runQuery { query }
        assertEquals(0, count)
    }

    @Test
    fun embedded_CompositeColumnExpression(info: TestInfo) = inTransaction(db, info) {
        val r = Meta.robot
        val query = QueryDsl.update(r).set {
            r.info1 eq r.info1
            r.info2.salary eq r.info2.salary * BigDecimal(2)
        }.where {
            r.employeeId eq 1
        }
        val count = db.runQuery(query)
        assertEquals(1, count)
        val sql = query.dryRun(db.config)
        assertTrue(sql.sql.contains("employee_no"))
        assertTrue(sql.sql.contains("employee_name"))
    }
}
