package integration.jdbc.postgresql

import integration.jdbc.JdbcEnv
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.single
import org.komapper.jdbc.JdbcDatabase
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(JdbcEnv::class)
class PostgreSqlInsertSingleTest(private val db: JdbcDatabase) {

    @Test
    fun dangerouslyOnDuplicateKeyUpdate() {
        val f = Meta.friend
        val friend = Friend(UUID.randomUUID(), UUID.randomUUID(), false)
        db.runQuery(QueryDsl.insert(f).single(friend))
        db.runQuery(
            QueryDsl.insert(f)
                .dangerouslyOnDuplicateKeyUpdate("(greatest(uuid1, uuid2), least(uuid1, uuid2))")
                .set {
                    f.pending eq true
                }.where {
                    f.pending eq false
                }
                .single(friend),
        )
        val result = db.runQuery(
            QueryDsl.from(f).where {
                f.uuid1 eq friend.uuid1
                f.uuid2 eq friend.uuid2
            }.single(),
        )
        assertTrue(result.pending)
    }

    @Test
    fun dangerouslyOnDuplicateKeyIgnore() {
        val f = Meta.friend
        val friend = Friend(UUID.randomUUID(), UUID.randomUUID(), false)
        db.runQuery(QueryDsl.insert(f).single(friend))
        val friend2 = friend.copy(uuid1 = friend.uuid2, uuid2 = friend.uuid1)
        db.runQuery(
            QueryDsl.insert(f)
                .dangerouslyOnDuplicateKeyIgnore("(greatest(uuid1, uuid2), least(uuid1, uuid2))")
                .single(friend2),
        )
        val result = db.runQuery(QueryDsl.from(f))
        assertEquals(1, result.size)
    }
}
