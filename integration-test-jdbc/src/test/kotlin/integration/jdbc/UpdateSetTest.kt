package integration.jdbc

import integration.core.Person
import integration.core.address
import integration.core.employee
import integration.core.identityStrategy
import integration.core.person
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

@ExtendWith(Env::class)
class UpdateSetTest(private val db: JdbcDatabase) {

    @Test
    fun test() {
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
    fun setIfNotNull() {
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
    fun arithmetic_add() {
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
    fun string_concat() {
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
    fun allowEmptyWhereClause_default() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.update(e).set {
                    e.employeeName eq "ABC"
                }
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_default_empty() {
        val e = Meta.employee
        val ex = assertFailsWith<IllegalStateException> {
            @Suppress("UNUSED_VARIABLE")
            val count = db.runQuery {
                QueryDsl.update(e).set {
                    e.employeeName eq "ABC"
                }.where { }
            }
        }
        assertEquals("Empty where clause is not allowed.", ex.message)
    }

    @Test
    fun allowEmptyWhereClause_true() {
        val e = Meta.employee
        val count = db.runQuery {
            QueryDsl.update(e).set {
                e.employeeName eq "ABC"
            }.options { it.copy(allowEmptyWhereClause = true) }
        }
        assertEquals(14, count)
    }

    @Test
    fun assignTimestamp_auto() {
        val p = Meta.person
        val person1 = db.runQuery {
            QueryDsl.insert(p).single(Person(1, "abc"))
        }
        Thread.sleep(10)
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
    fun assignTimestamp_manual() {
        val p = Meta.person
        val person1 = db.runQuery {
            QueryDsl.insert(p).single(Person(1, "abc"))
        }
        Thread.sleep(10)
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
    fun incrementVersion_auto() {
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
    fun incrementVersion_disabled() {
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
    fun dryRun_timestamp() {
        val p = Meta.person
        val query = QueryDsl.update(p).set {
            p.name eq "test"
        }.where {
            p.personId eq 1
        }
        val result = query.dryRun()
        val expected = "update person as t0_ set name = ?, updated_at = ? where t0_.person_id = ?"
        assertEquals(expected, result.sql)
    }

    @Test
    fun dryRun_version() {
        val a = Meta.address
        val query = QueryDsl.update(a).set {
            a.street eq "STREET 16"
        }.where {
            a.addressId eq 16
        }
        val result = query.dryRun()
        val expected = "update address as t0_ set street = ?, version = version + 1 where t0_.address_id = ?"
        assertEquals(expected, result.sql)
    }

    @Test
    fun dryRun_emptySetClause() {
        val i = Meta.identityStrategy
        val query = QueryDsl.update(i).set {
        }.where {
            i.id eq 1
        }
        val result = query.dryRun()
        println(result)
    }

    @Test
    fun emptySetClause() {
        val i = Meta.identityStrategy
        val query = QueryDsl.update(i).set {
        }.where {
            i.id eq 1
        }
        val count = db.runQuery { query }
        assertEquals(0, count)
    }
}
