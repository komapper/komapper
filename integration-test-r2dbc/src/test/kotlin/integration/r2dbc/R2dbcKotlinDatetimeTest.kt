package integration.r2dbc

import integration.core.KotlinDatetimePerson
import integration.core.KotlinInstantData
import integration.core.KotlinInstantPerson
import integration.core.KotlinLocalDateData
import integration.core.KotlinLocalDateTimeData
import integration.core.kotlinDatetimePerson
import integration.core.kotlinInstantData
import integration.core.kotlinInstantPerson
import integration.core.kotlinLocalDateData
import integration.core.kotlinLocalDateTimeData
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.andThen
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@ExtendWith(R2dbcEnv::class)
class R2dbcKotlinDatetimeTest(val db: R2dbcDatabase) {
    @Test
    fun instant(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.kotlinInstantData
        val datetime = LocalDateTime(2019, 6, 1, 12, 11, 10)
        val instant = datetime.toInstant(TimeZone.UTC)
        val data = KotlinInstantData(1, instant)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDate(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.kotlinLocalDateData
        val data = KotlinLocalDateData(
            1,
            LocalDate(2019, 6, 1),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun localDateTime(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.kotlinLocalDateTimeData
        val data = KotlinLocalDateTimeData(
            1,
            LocalDateTime(2019, 6, 1, 12, 11, 10),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun createdAt_instant(info: TestInfo) = inTransaction(db, info) {
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
    fun updatedAt_instance(info: TestInfo) = inTransaction(db, info) {
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
    fun createdAt_localDateTime(info: TestInfo) = inTransaction(db, info) {
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
    fun updatedAt_localDateTime(info: TestInfo) = inTransaction(db, info) {
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
