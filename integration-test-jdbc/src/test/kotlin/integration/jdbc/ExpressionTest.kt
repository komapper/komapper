package integration.jdbc

import integration.Address
import integration.meta
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.SqlDsl
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
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class ExpressionTest(private val db: JdbcDatabase) {

    @Test
    fun plus() {
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
    fun plus_other_column() {
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
    fun minus() {
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
    fun minus_other_column() {
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
    fun div() {
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
    fun div_other_column() {
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
    fun rem() {
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
    fun rem_other_column() {
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
    fun concat() {
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
    fun concat_other_column() {
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
    fun lowerFunction() {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(lower(literal("TEST"))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun upperFunction() {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(upper(literal("test"))).first()
        }
        assertEquals("TEST", result)
    }

    @Test
    fun trimFunction() {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(trim(literal(" test "))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun ltrimFunction() {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(ltrim(literal(" test "))).first()
        }
        assertEquals("test ", result)
    }

    @Test
    fun rtrimFunction() {
        val a = Address.meta
        val result = db.runQuery {
            SqlDsl.from(a).select(rtrim(literal(" test "))).first()
        }
        assertEquals(" test", result)
    }
}
