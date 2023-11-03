package integration.r2dbc.postgresql

import integration.r2dbc.R2dbcEnv
import integration.r2dbc.inTransaction
import io.r2dbc.postgresql.codec.Box
import io.r2dbc.postgresql.codec.Circle
import io.r2dbc.postgresql.codec.Interval
import io.r2dbc.postgresql.codec.Json
import io.r2dbc.postgresql.codec.Line
import io.r2dbc.postgresql.codec.Lseg
import io.r2dbc.postgresql.codec.Path
import io.r2dbc.postgresql.codec.Point
import io.r2dbc.postgresql.codec.Polygon
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.r2dbc.R2dbcDatabase
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import java.time.Period
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcPostgreSqlTypeTest(val db: R2dbcDatabase) {

    @Test
    fun box(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.boxData
        val data = BoxData(
            1,
            Box.of(
                Point.of(1.0, 2.0),
                Point.of(3.0, 4.0),
            ),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun box_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.boxData
        val data = BoxData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun circle(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.circleData
        val data = CircleData(
            1,
            Circle.of(Point.of(1.0, 2.0), 3.0),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun circle_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.circleData
        val data = CircleData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun geometry(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.geometryData
        val factory = GeometryFactory()
        val data = GeometryData(
            1,
            factory.createPoint(Coordinate(1.0, 2.0)),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun geometry_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.geometryData
        val data = GeometryData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun line(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.lineData
        val data = LineData(
            1,
            Line.of(Point.of(1.0, 2.0), Point.of(3.0, 4.0)),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun line_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.lineData
        val data = LineData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun lseg(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.lsegData
        val data = LsegData(
            1,
            Lseg.of(Point.of(1.0, 2.0), Point.of(3.0, 4.0)),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun lseg_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.lsegData
        val data = LsegData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun interval(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.intervalData
        val data = IntervalData(1, Interval.of(Period.of(2022, 2, 5)))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun interval_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.intervalData
        val data = IntervalData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun json(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.jsonData
        val data = JsonData(
            1,
            Json.of(
                """
            {"a": 100, "b": "Hello"}
                """.trimIndent(),
            ),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value!!.asString(), data2.value!!.asString())

        val result = db.runQuery {
            QueryDsl.fromTemplate("select value->'b' as x from json_data")
                .select { it.get("x", Json::class)!! }
                .first()
        }
        assertEquals("\"Hello\"", result.asString())
    }

    @Test
    fun json_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.jsonData
        val data = JsonData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data, data2)
    }

    @Test
    fun path(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.pathData
        val data = PathData(
            1,
            Path.open(
                Point.of(1.0, 2.0),
                Point.of(3.0, 4.0),
            ),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun path_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.pathData
        val data = PathData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun point(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.pointData
        val data = PointData(1, Point.of(1.0, 2.0))
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun point_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.pointData
        val data = PointData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun polygon(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.polygonData
        val data = PolygonData(
            1,
            Polygon.of(
                Point.of(1.0, 2.0),
                Point.of(3.0, 4.0),
                Point.of(5.0, 6.0),
                Point.of(7.0, 8.0),
            ),
        )
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }

    @Test
    fun polygon_null(info: TestInfo) = inTransaction(db, info) {
        val m = Meta.polygonData
        val data = PolygonData(1, null)
        db.runQuery { QueryDsl.insert(m).single(data) }
        val data2 = db.runQuery {
            QueryDsl.from(m).where { m.id eq 1 }.first()
        }
        assertEquals(data.value, data2.value)
    }
}
