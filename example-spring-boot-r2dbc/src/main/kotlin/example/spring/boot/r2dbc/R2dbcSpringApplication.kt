package example.spring.boot.r2dbc

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication

@SpringBootApplication
class R2dbcSpringApplication

fun main(args: Array<String>) {
    SpringApplication.run(R2dbcSpringApplication::class.java, *args)
}

data class Message(
    val id: Int? = null,
    val text: String
)

@KomapperEntityDef(Message::class)
data class MessageDef(
    @KomapperId @KomapperAutoIncrement val id: Nothing,
)
