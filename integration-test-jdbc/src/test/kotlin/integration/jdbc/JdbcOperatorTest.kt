package integration.jdbc

import integration.core.address
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.coalesce
import org.komapper.core.dsl.operator.concat
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.div
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.locate
import org.komapper.core.dsl.operator.lower
import org.komapper.core.dsl.operator.ltrim
import org.komapper.core.dsl.operator.minus
import org.komapper.core.dsl.operator.plus
import org.komapper.core.dsl.operator.random
import org.komapper.core.dsl.operator.rem
import org.komapper.core.dsl.operator.rtrim
import org.komapper.core.dsl.operator.substring
import org.komapper.core.dsl.operator.trim
import org.komapper.core.dsl.operator.upper
import org.komapper.core.dsl.query.dryRun
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class JdbcOperatorTest(private val db: JdbcDatabase) {

    @Test
    fun plus() {
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
    fun plus_other_column() {
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
    fun minus() {
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
    fun minus_other_column() {
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
    fun div() {
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
    fun div_other_column() {
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
    fun rem() {
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
    fun rem_other_column() {
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
    fun concat() {
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
    fun concat_other_column() {
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
    fun locateFunction_2parameters() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(locate(literal("bar"), literal("foobarbar"))).first()
        }
        assertEquals(4, result)
    }

    @Test
    fun locateFunction_2parameters_when_1st_parameter_is_value() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(locate("bar", literal("foobarbar"))).first()
        }
        assertEquals(4, result)
    }

    @Test
    fun locateFunction_2parameters_when_2nd_parameter_is_value() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(locate(literal("bar"), "foobarbar")).first()
        }
        assertEquals(4, result)
    }

    @Test
    fun locateFunction_3parameters() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(locate(literal("bar"), literal("foobarbar"), 5)).first()
        }
        assertEquals(7, result)
    }

    @Test
    fun locateFunction_3parameters_when_1st_parameter_is_value() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(locate("bar", literal("foobarbar"), 5)).first()
        }
        assertEquals(7, result)
    }

    @Test
    fun locateFunction_3parameters_when_2nd_parameter_is_value() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(locate(literal("bar"), "foobarbar", 5)).first()
        }
        assertEquals(7, result)
    }

    @Test
    fun lowerFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(lower(literal("TEST"))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun upperFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(upper(literal("test"))).first()
        }
        assertEquals("TEST", result)
    }

    @Test
    fun substringFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(substring(literal("hello world"), 7)).first()
        }
        assertEquals("world", result)
    }

    @Test
    fun substringFunction_length() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(substring(literal("hello world"), 7, 1)).first()
        }
        assertEquals("w", result)
    }

    @Test
    fun trimFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(trim(literal(" test "))).first()
        }
        assertEquals("test", result)
    }

    @Test
    fun ltrimFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(ltrim(literal(" test "))).first()
        }
        assertEquals("test ", result)
    }

    @Test
    fun rtrimFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).select(rtrim(literal(" test "))).first()
        }
        assertEquals(" test", result)
    }

    @Test
    fun randomFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).selectNotNull(random()).first()
        }
        assertTrue(result < BigDecimal.ONE)
    }

    @Test
    fun randomFunction_getFromRecord() {
        val a = Meta.address
        val record = db.runQuery {
            QueryDsl.from(a).selectAsRecord(random()).first()
        }
        val result = record[random()]
        assertNotNull(result)
        assertTrue(result < BigDecimal.ONE)
    }

    @Test
    fun orderByRandomFunction() {
        val a = Meta.address
        val result = db.runQuery {
            QueryDsl.from(a).orderBy(random())
        }
        println(result)
    }

    @Test
    fun coalesceFunction_select() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e).where { e.managerId.isNull() }.select(coalesce(e.managerId, literal(-1)))
        }
        assertEquals(1, list.size)
        assertEquals(-1, list[0])
    }

    @Test
    fun coalesceFunction_select_3_args() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e).where { e.managerId.isNull() }.select(coalesce(e.managerId, e.managerId, literal(-1)))
        }
        assertEquals(1, list.size)
        assertEquals(-1, list[0])
    }

    @Test
    fun coalesceFunction_update() {
        val e = Meta.employee
        val affectedRows = db.runQuery {
            QueryDsl.update(e).set {
                e.managerId eq coalesce(e.managerId, literal(-1))
            }.where {
                e.managerId.isNull()
            }
        }
        assertEquals(1, affectedRows)
        val result = db.runQuery {
            QueryDsl.from(e).where { e.managerId eq -1 }.select(count())
        }
        assertEquals(1, result)
    }
}
