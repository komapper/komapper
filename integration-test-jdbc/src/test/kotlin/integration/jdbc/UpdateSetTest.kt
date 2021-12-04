package integration.jdbc

import integration.Person
import integration.address
import integration.employee
import integration.identityStrategy
import integration.person
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
                a.street set "STREET 16"
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
                a.street setIfNotNull null
                a.version set 10
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
                a.version set (a.version + 10)
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
                a.street set (concat(concat("[", a.street), "]"))
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
                    e.employeeName set "ABC"
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
                    e.employeeName set "ABC"
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
                e.employeeName set "ABC"
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
                p.name set "ABC"
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
                p.name set "ABC"
                p.updatedAt set person1.updatedAt
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
                a.street set "STREET 16"
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
                a.street set "STREET 16"
                a.version set 10
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
            p.name set "test"
        }.where {
            p.personId eq 1
        }
        val result = query.dryRun()
        val expected = "update PERSON as t0_ set NAME = ?, UPDATED_AT = ? where t0_.PERSON_ID = ?"
        assertEquals(expected, result.sql)
    }

    @Test
    fun dryRun_version() {
        val a = Meta.address
        val query = QueryDsl.update(a).set {
            a.street set "STREET 16"
        }.where {
            a.addressId eq 16
        }
        val result = query.dryRun()
        val expected = "update ADDRESS as t0_ set STREET = ?, VERSION = VERSION + 1 where t0_.ADDRESS_ID = ?"
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
