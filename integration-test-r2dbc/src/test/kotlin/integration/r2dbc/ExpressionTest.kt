package integration.r2dbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
import org.komapper.core.dsl.concat
import org.komapper.core.dsl.div
import org.komapper.core.dsl.literal
import org.komapper.core.dsl.lower
import org.komapper.core.dsl.ltrim
import org.komapper.core.dsl.minus
import org.komapper.core.dsl.plus
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.rem
import org.komapper.core.dsl.rtrim
import org.komapper.core.dsl.trim
import org.komapper.core.dsl.upper
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.dsl.query.dryRun

@ExtendWith(Env::class)
class ExpressionTest(private val db: R2dbcDatabase) {

    @Test
    fun plus() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId + 1)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(11, result)
    }

    @Test
    fun plus_other_column() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId + a.addressId)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(20, result)
    }

    @Test
    fun minus() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId - 10)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(0, result)
    }

    @Test
    fun minus_other_column() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId - a.addressId)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(0, result)
    }

    @Test
    fun div() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId / 2)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(5, result)
    }

    @Test
    fun div_other_column() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId / a.addressId)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(1, result)
    }

    @Test
    fun rem() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId % 3)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(1, result)
    }

    @Test
    fun rem_other_column() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(a.addressId % a.addressId)
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals(0, result)
    }

    @Test
    fun concat() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(concat(concat("[", a.street), "]"))
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals("[STREET 10]", result)
    }

    @Test
    fun concat_other_column() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a)
                .where {
                    a.addressId eq 10
                }
                .select(concat(concat(a.street, a.street), a.street))
                .also {
                    println(it.dryRun())
                }.first()
        }
        assertEquals("STREET 10STREET 10STREET 10", result)
    }

    @Test
    fun lowerFunction() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(lower(literal("TEST"))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun upperFunction() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(upper(literal("test"))).first()
        }
        assertEquals("TEST", result)
    }

    @Test
    fun trimFunction() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(trim(literal(" test "))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun ltrimFunction() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(ltrim(literal(" test "))).first()
        }
        assertEquals("test ", result)
    }

    @Test
    fun rtrimFunction() = inTransaction(db) {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(rtrim(literal(" test "))).first()
        }
        assertEquals(" test", result)
    }
}
