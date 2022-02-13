package example.spring.boot.jdbc

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.web.util.UriComponentsBuilder
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ApplicationTest {

    private val restTemplate = TestRestTemplate()
    private val typedReference: ParameterizedTypeReference<List<Message>> =
        object : ParameterizedTypeReference<List<Message>>() {}

    @Value("\${local.server.port}")
    val port = 0

    @Test
    fun test() {
        // add
        val (id, text) = restTemplate.getForObject(
            UriComponentsBuilder.fromUriString("http://localhost").port(port)
                .queryParam("text", "Hi!").build().toUri(),
            Message::class.java
        )
        assertEquals(3, id)
        assertEquals("Hi!", text)

        // list
        val messages = restTemplate.exchange(
            UriComponentsBuilder.fromUriString("http://localhost").port(port)
                .build().toUri(),
            HttpMethod.GET, HttpEntity.EMPTY,
            typedReference
        ).body!!
        assertEquals(3, messages.size)
        assertEquals(
            listOf
            (
                Message(3, "Hi!"),
                Message(2, "World"),
                Message(1, "Hello"),
            ),
            messages
        )
    }
}
