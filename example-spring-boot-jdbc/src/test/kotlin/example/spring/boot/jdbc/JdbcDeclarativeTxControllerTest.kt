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
import kotlin.test.assertNotNull

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JdbcDeclarativeTxControllerTest {
    private val baseUri = "http://localhost/declarative"
    private val restTemplate = TestRestTemplate()
    private val typedReference: ParameterizedTypeReference<List<Message>> =
        object : ParameterizedTypeReference<List<Message>>() {}

    @Value("\${local.server.port}")
    val port = 0

    @Test
    fun test() {
        val id = add()
        delete(id)
    }

    private fun add(): Int {
        // add
        val (id, text) = restTemplate.getForObject(
            UriComponentsBuilder.fromUriString(baseUri).port(port).queryParam("text", "Hi!").build().toUri(),
            Message::class.java,
        )
        assertNotNull(id)
        assertEquals("Hi!", text)

        // list
        val messages = restTemplate.exchange(
            UriComponentsBuilder.fromUriString(baseUri).port(port).build().toUri(),
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typedReference,
        ).body!!
        assertEquals(3, messages.size)
        assertEquals(listOf("Hi!", "World", "Hello"), messages.map { it.text })

        return id
    }

    private fun delete(id: Int) {
        // delete
        restTemplate.exchange(
            UriComponentsBuilder.fromUriString("$baseUri/{id}").port(port).build(id),
            HttpMethod.DELETE,
            HttpEntity.EMPTY,
            Void::class.java,
        )

        // list
        val messages = restTemplate.exchange(
            UriComponentsBuilder.fromUriString(baseUri).port(port).build().toUri(),
            HttpMethod.GET,
            HttpEntity.EMPTY,
            typedReference,
        ).body!!
        assertEquals(2, messages.size)
        assertEquals(
            listOf(
                Message(2, "World"),
                Message(1, "Hello"),
            ),
            messages,
        )
    }
}
