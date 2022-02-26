package example.spring.boot.r2dbc

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.r2dbc.R2dbcDatabase
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Suppress("unused")
@RestController
@RequestMapping("/imperative")
class R2dbcImperativeTxController(private val db: R2dbcDatabase) {

    @GetMapping("")
    fun list(): Flow<Message> = db.flowTransaction {
        val value = db.flowQuery {
            val m = Meta.message
            QueryDsl.from(m).orderBy(m.id.desc())
        }
        emitAll(value)
    }

    @GetMapping(value = [""], params = ["text"])
    suspend fun add(@RequestParam text: String): Message = db.withTransaction {
        val message = Message(text = text)
        db.runQuery {
            val m = Meta.message
            QueryDsl.insert(m).single(message)
        }
    }

    @DeleteMapping(value = ["/{id}"])
    suspend fun delete(@PathVariable id: Int) = db.withTransaction {
        db.runQuery {
            val m = Meta.message
            QueryDsl.delete(m).where { m.id eq id }
        }
    }
}
