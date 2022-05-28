package integration.r2dbc.oracle

import integration.core.DurationData
import integration.core.PeriodData
import integration.core.durationData
import integration.core.periodData
import integration.r2dbc.R2dbcEnv
import integration.r2dbc.inTransaction
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.time.Duration
import java.time.Period
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcOracleTypeTest(private val db: R2dbcDatabase) {

    @Test
    fun period(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.periodData
        val data = PeriodData(1, Period.of(11, 2, 0))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun period_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.periodData
        val data = PeriodData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun duration(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.durationData
        val data = DurationData(1, Duration.ofDays(11))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun duration_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.durationData
        val data = DurationData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
