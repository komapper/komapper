package example.spring.boot.jdbc

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class JdbcSpringApplication

fun main(args: Array<String>) {
    SpringApplication.run(JdbcSpringApplication::class.java, *args)
}

data class Message(
    val id: Int? = null,
    val text: String,
)

@KomapperEntityDef(Message::class)
data class MessageDef(
    @KomapperId @KomapperAutoIncrement
    val id: Nothing,
)
