package integration.r2dbc.oracle

import integration.core.DurationTest
import integration.core.PeriodTest
import integration.core.durationTest
import integration.core.periodTest
import integration.r2dbc.R2dbcEnv
import integration.r2dbc.inTransaction
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
    fun period() = inTransaction(db) {
        val m = Meta.periodTest
        val data = PeriodTest(1, Period.of(11, 2, 0))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun duration() = inTransaction(db) {
        val m = Meta.durationTest
        val data = DurationTest(1, Duration.ofDays(11))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
