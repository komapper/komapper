package example.spring.boot.r2dbc

import kotlinx.coroutines.flow.Flow
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Suppress("unused")
@RestController
@Transactional
@RequestMapping("/declarative")
class R2dbcDeclarativeTxController(private val db: R2dbcDatabase) {

    @GetMapping("")
    fun list(): Flow<Message> {
        return db.flowQuery {
            val m = Meta.message
            QueryDsl.from(m).orderBy(m.id.desc())
        }
    }

    @GetMapping(value = [""], params = ["text"])
    suspend fun add(@RequestParam text: String): Message {
        val message = Message(text = text)
        return db.runQuery {
            val m = Meta.message
            QueryDsl.insert(m).single(message)
        }
    }

    @DeleteMapping(value = ["/{id}"])
    suspend fun delete(@PathVariable id: Int) {
        db.runQuery {
            val m = Meta.message
            QueryDsl.delete(m).where { m.id eq id }
        }
    }
}
