package example.micronaut.jdbc

import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.jdbc.JdbcDatabase
import javax.transaction.Transactional

@Transactional
@Controller("/")
class JdbcDeclarativeTxController(private val db: JdbcDatabase) {

    @Get(produces = [MediaType.APPLICATION_JSON])
    fun list(): List<Message> {
        val list = db.runQuery {
            val m = Meta.message
            QueryDsl.from(m).orderBy(m.id.desc())
        }
        return list
    }

    @Get("/add", produces = [MediaType.APPLICATION_JSON])
    fun add(@QueryValue text: String): Message {
        val message = Message(text = text)
        return db.runQuery {
            val m = Meta.message
            QueryDsl.insert(m).single(message)
        }
    }
}
