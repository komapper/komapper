package example

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmEntityDef
import org.komapper.annotation.KmId
import org.komapper.core.dsl.EntityDsl
import org.komapper.core.dsl.desc
import org.komapper.jdbc.Database
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@RestController
@Transactional
class Application(private val database: Database) {

    @RequestMapping("/")
    fun list(): List<Message> {
        return database.runQuery {
            val m = MessageDef.meta
            EntityDsl.from(m).orderBy(m.id.desc())
        }
    }

    @RequestMapping(value = ["/"], params = ["text"])
    fun add(@RequestParam text: String): Message {
        val message = Message(text = text)
        return database.runQuery {
            val m = MessageDef.meta
            EntityDsl.insert(m).single(message)
        }
    }
}

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}

data class Message(
    val id: Int? = null,
    val text: String
)

@KmEntityDef(Message::class)
data class MessageDef(
    @KmId @KmAutoIncrement val id: Nothing,
) {
    companion object
}
