package integration.r2dbc

import integration.core.address
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.div
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.lower
import org.komapper.core.dsl.operator.ltrim
import org.komapper.core.dsl.operator.minus
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.operator.rem
import org.komapper.core.dsl.operator.rtrim
import org.komapper.core.dsl.operator.trim
import org.komapper.core.dsl.operator.upper
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcOperatorTest(private val db: R2dbcDatabase) {
    @Test
    fun plus(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun plus_other_column(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun minus(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun minus_other_column(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun div(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun div_other_column(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun rem(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun rem_other_column(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun concat(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun concat_other_column(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a)
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
    fun lowerFunction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 10
            }.select(lower(literal("TEST"))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun upperFunction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 10
            }.select(upper(literal("test"))).first()
        }
        assertEquals("TEST", result)
    }

    @Test
    fun trimFunction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 10
            }.select(trim(literal(" test "))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun ltrimFunction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 10
            }.select(ltrim(literal(" test "))).first()
        }
        assertEquals("test ", result)
    }

    @Test
    fun rtrimFunction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).where {
                a.addressId eq 10
            }.select(rtrim(literal(" test "))).first()
        }
        assertEquals(" test", result)
    }
}
