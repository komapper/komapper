package integration.jdbc

import integration.core.KotlinDatetimePerson
import integration.core.KotlinInstantPerson
import integration.core.KotlinInstantTest
import integration.core.KotlinLocalDateTest
import integration.core.KotlinLocalDateTimeTest
import integration.core.kotlinDatetimePerson
import integration.core.kotlinInstantPerson
import integration.core.kotlinInstantTest
import integration.core.kotlinLocalDateTest
import integration.core.kotlinLocalDateTimeTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(JdbcEnv::class)
class JdbcKotlinDatetimeTest(val db: JdbcDatabase) {

    @Test
    fun instant() {
        val m = Meta.kotlinInstantTest
        val datetime = LocalDateTime(2019, 6, 1, 12, 11, 10)
        val instant = datetime.toInstant(TimeZone.UTC)
        val data = KotlinInstantTest(1, instant)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate() {
        val m = Meta.kotlinLocalDateTest
        val data = KotlinLocalDateTest(
            1,
            LocalDate(2019, 6, 1)
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime() {
        val m = Meta.kotlinLocalDateTimeTest
        val data = KotlinLocalDateTimeTest(
            1,
            LocalDateTime(2019, 6, 1, 12, 11, 10)
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun createdAt_instant() {
        val p = Meta.kotlinInstantPerson
        val person1 = KotlinInstantPerson(1, "ABC")
        val id = db.runQuery { QueryDsl.insert(p).single(person1) }.humanId
        val person2 = db.runQuery { QueryDsl.from(p).where { p.humanId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.humanId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Test
    fun updatedAt_instance() {
        val p = Meta.kotlinInstantPerson
        val findQuery = QueryDsl.from(p).where { p.humanId eq 1 }.first()
        val person1 = KotlinInstantPerson(1, "ABC")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1).andThen(findQuery)
        }
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(name = "DEF")).andThen(findQuery)
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
    }

    @Test
    fun createdAt_localDateTime() {
        val p = Meta.kotlinDatetimePerson
        val person1 = KotlinDatetimePerson(1, "ABC")
        val id = db.runQuery { QueryDsl.insert(p).single(person1) }.personId
        val person2 = db.runQuery { QueryDsl.from(p).where { p.personId eq id }.first() }
        assertNotNull(person2.createdAt)
        assertNotNull(person2.updatedAt)
        assertEquals(person2.createdAt, person2.updatedAt)
        val person3 = db.runQuery {
            QueryDsl.from(p).where {
                p.personId to 1
            }.first()
        }
        assertEquals(person2, person3)
    }

    @Test
    fun updatedAt_localDateTime() {
        val p = Meta.kotlinDatetimePerson
        val findQuery = QueryDsl.from(p).where { p.personId eq 1 }.first()
        val person1 = KotlinDatetimePerson(1, "ABC")
        val person2 = db.runQuery {
            QueryDsl.insert(p).single(person1).andThen(findQuery)
        }
        val person3 = db.runQuery {
            QueryDsl.update(p).single(person2.copy(name = "DEF")).andThen(findQuery)
        }
        assertNotNull(person2.updatedAt)
        assertNotNull(person3.updatedAt)
    }
}
