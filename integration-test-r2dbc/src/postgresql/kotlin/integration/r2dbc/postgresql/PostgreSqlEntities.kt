package integration.r2dbc.postgresql

import io.r2dbc.postgresql.codec.Box
import io.r2dbc.postgresql.codec.Circle
import io.r2dbc.postgresql.codec.Interval
import io.r2dbc.postgresql.codec.Json
import io.r2dbc.postgresql.codec.Line
import io.r2dbc.postgresql.codec.Lseg
import io.r2dbc.postgresql.codec.Path
import io.r2dbc.postgresql.codec.Point
import io.r2dbc.postgresql.codec.Polygon
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable

@KomapperEntity
@KomapperTable("box_data")
data class BoxData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Box?)

@KomapperEntity
@KomapperTable("circle_data")
data class CircleData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Circle?)

@KomapperEntity
@KomapperTable("line_data")
data class LineData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Line?)

@KomapperEntity
@KomapperTable("lseg_data")
data class LsegData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Lseg?)

@KomapperEntity
@KomapperTable("interval_data")
data class IntervalData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Interval?)

@KomapperEntity
@KomapperTable("json_data")
data class JsonData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Json?)

@KomapperEntity
@KomapperTable("path_data")
data class PathData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Path?)

@KomapperEntity
@KomapperTable("point_data")
data class PointData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Point?)

@KomapperEntity
@KomapperTable("polygon_data")
data class PolygonData(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Polygon?)
