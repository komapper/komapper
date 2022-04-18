package example.micronaut.jdbc

import io.micronaut.runtime.Micronaut
import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId

fun main(args: Array<String>) {
    Micronaut.build()
        .args(*args)
        .packages("example.micronaut.jdbc")
        .start()
}

data class Message(
    val id: Int? = null,
    val text: String
)

@KomapperEntityDef(Message::class)
data class MessageDef(
    @KomapperId @KomapperAutoIncrement val id: Nothing,
)
