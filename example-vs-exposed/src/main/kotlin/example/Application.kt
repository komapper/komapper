package example

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.SchemaDsl
import org.komapper.core.dsl.operator.count
import org.komapper.core.dsl.operator.literal
import org.komapper.core.dsl.operator.substring
import org.komapper.core.dsl.operator.trim
import org.komapper.core.dsl.query.first
import org.komapper.jdbc.JdbcDatabase
import org.komapper.tx.jdbc.withTransaction

data class User(
    val id: String,
    val name: String,
    val cityId: Int?
)

data class City(
    val id: Int? = null,
    val name: String
)

@KomapperEntityDef(User::class)
@KomapperTable("Users")
data class UserDef(
    @KomapperId
    val id: Nothing
)

@KomapperEntityDef(City::class)
@KomapperTable("Cities")
data class CityDef(
    @KomapperId
    @KomapperAutoIncrement
    val id: Nothing
)

fun main() {
    val c = Meta.city
    val u = Meta.user

    val db = JdbcDatabase.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")

    db.withTransaction {
        db.runQuery {
            SchemaDsl.create(c, u)
        }

        val (saintPetersburg, munich) = db.runQuery {
            QueryDsl.insert(c).multiple(
                City(name = "St. Petersburg"),
                City(name = "Munich"),
            )
        }

        val (_, pragueId) = db.runQuery {
            QueryDsl.insert(c).values { c.name set substring(trim(literal("   Prague   ")), 1, 2) }
        }

        val prague = db.runQuery {
            QueryDsl.from(c).where { c.id eq pragueId }.first()
        }
        check(prague.name == "Pr") { prague.toString() }

        db.runQuery {
            QueryDsl.insert(u).multiple(
                User(id = "andrey", name = "Andrey", cityId = saintPetersburg.id),
                User(id = "sergey", name = "Sergey", cityId = munich.id),
                User(id = "eugene", name = "Eugene", cityId = munich.id),
                User(id = "alex", name = "Alex", cityId = null),
                User(id = "smth", name = "Something", cityId = null),
            )
        }

        db.runQuery {
            QueryDsl.update(u).set { u.name set "Alexey" }.where { u.id eq "alex" }
        }

        db.runQuery {
            QueryDsl.delete(u).where { u.name like "%thing" }
        }

        println("All cities:")

        for (city in db.runQuery { QueryDsl.from(c) }) {
            println("${city.id}: ${city.name}")
        }

        println("Manual join:")

        db.runQuery {
            QueryDsl.from(u)
                .innerJoin(c) {
                    u.cityId eq c.id
                }.where {
                    and {
                        u.id eq "andrey"
                        or { u.name eq "Sergey" }
                    }
                    u.id eq "sergey"
                    u.cityId eq c.id
                }.select(u.name, c.name)
        }.forEach { (userName, cityName) ->
            println("$userName lives in $cityName")
        }

        println("Join with foreign key:")

        db.runQuery {
            QueryDsl.from(u)
                .innerJoin(c) {
                    u.cityId eq c.id
                }.where {
                    c.name eq "St. Petersburg"
                    or { u.cityId.isNull() }
                }.select(u.name, u.cityId, c.name)
        }.forEach { (userName, cityId, cityName) ->
            if (cityId != null) {
                println("$userName lives in $cityName")
            } else {
                println("$userName lives nowhere")
            }
        }

        println("Functions and group by:")

        db.runQuery {
            QueryDsl.from(c)
                .innerJoin(u) {
                    c.id eq u.cityId
                }.groupBy(c.name)
                .select(c.name, count(u.id))
        }.forEach { (cityName, userCount) ->
            if (userCount != null && userCount > 0L) {
                println("$userCount user(s) live(s) in $cityName")
            } else {
                println("Nobody lives in $cityName")
            }
        }

        db.runQuery {
            SchemaDsl.drop(u, c)
        }
    }
}
