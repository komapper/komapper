package example.spring.boot.jdbc

import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.operator.desc
import org.komapper.jdbc.JdbcDatabase
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Suppress("unused")
@RestController
@RequestMapping("/imperative")
class JdbcImperativeTxController(val db: JdbcDatabase) {
    @GetMapping("")
    fun list(): List<Message> = db.withTransaction {
        db.runQuery {
            val m = Meta.message
            QueryDsl.from(m).orderBy(m.id.desc())
        }
    }

    @GetMapping(value = [""], params = ["text"])
    fun add(
        @RequestParam text: String
    ): Message = db.withTransaction {
        val message = Message(text = text)
        db.runQuery {
            val m = Meta.message
            QueryDsl.insert(m).single(message)
        }
    }

    @DeleteMapping(value = ["/{id}"])
    fun delete(
        @PathVariable id: Int
    ) = db.withTransaction {
        db.runQuery {
            val m = Meta.message
            QueryDsl.delete(m).where { m.id eq id }
        }
    }
}
