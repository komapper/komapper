package integration.r2dbc.oracle

import integration.core.IntervalYearTest
import integration.core.intervalYearTest
import integration.r2dbc.Env
import integration.r2dbc.inTransaction
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import java.time.Period
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(Env::class)
class OracleDataTypeTest(private val db: R2dbcDatabase) {

    @Test
    fun period() = inTransaction(db) {
        val m = Meta.intervalYearTest
        val data = IntervalYearTest(1, Period.of(11, 2, 0))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }
}
