package integration.r2dbc

import integration.core.Address
import integration.core.Dbms
import integration.core.Run
import integration.core.address
import integration.core.employee
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.toList
import org.junit.jupiter.api.TestInfo
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.query.first
import org.komapper.core.dsl.query.int
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.tx.core.TransactionAttribute
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(R2dbcEnv::class)
class R2dbcFlowTest(val db: R2dbcDatabase) {
    @Test
    fun singleEntity(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId inList listOf(1, 2) }.orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleEntity_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a).where { a.addressId eq 1 }.union(
                QueryDsl.from(a).where { a.addressId eq 2 },
            ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList().map { it.addressId })
    }

    @Test
    fun singleColumn(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleNotNullColumn(info: TestInfo) = inTransaction(db, info) {
        val flow: Flow<Int> = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun singleColumn_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId).union(
                    QueryDsl.from(Meta.address)
                        .where { a.addressId eq 2 }
                        .select(a.addressId),
                ).orderBy(a.addressId)
        }
        assertEquals(listOf(1, 2), flow.toList())
    }

    @Test
    fun pairColumns(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2",
            ),
            flow.toList(),
        )
    }

    @Test
    fun pairNotNullColumns(info: TestInfo) = inTransaction(db, info) {
        val flow: Flow<Pair<Int, String>> = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2",
            ),
            flow.toList(),
        )
    }

    @Test
    fun pairColumns_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street).union(
                    QueryDsl.from(Meta.address)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street),
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                1 to "STREET 1",
                2 to "STREET 2",
            ),
            flow.toList(),
        )
    }

    @Test
    fun tripleColumns(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            flow.toList(),
        )
    }

    @Test
    fun tripleNotNullColumns(info: TestInfo) = inTransaction(db, info) {
        val flow: Flow<Triple<Int, String, Int>> = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .selectNotNull(a.addressId, a.street, a.version)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            flow.toList(),
        )
    }

    @Test
    fun tripleColumns_union(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            val a = Meta.address
            QueryDsl.from(a)
                .where { a.addressId eq 1 }
                .select(a.addressId, a.street, a.version).union(
                    QueryDsl.from(a)
                        .where { a.addressId eq 2 }
                        .select(a.addressId, a.street, a.version),
                ).orderBy(a.addressId)
        }
        assertEquals(
            listOf(
                Triple(1, "STREET 1", 1),
                Triple(2, "STREET 2", 1),
            ),
            flow.toList(),
        )
    }

    @Test
    fun multipleColumns(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val flow = db.flowQuery {
            QueryDsl.from(a)
                .where { a.addressId inList listOf(1, 2) }
                .orderBy(a.addressId)
                .select(a.addressId, a.street, a.version, a.addressId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][a.addressId])
        assertEquals(2, list[1][a.addressId])
    }

    @Test
    fun multipleColumns_union(info: TestInfo) = inTransaction(db, info) {
        val e = Meta.employee
        val flow = db.flowQuery {
            QueryDsl.from(e)
                .where { e.employeeId eq 1 }
                .select(e.employeeId, e.employeeNo, e.employeeName, e.salary).union(
                    QueryDsl.from(e)
                        .where { e.employeeId eq 2 }
                        .select(e.employeeId, e.employeeNo, e.employeeName, e.salary),
                ).orderBy(e.employeeId)
        }
        val list = flow.toList()
        assertEquals(2, list.size)
        assertEquals(1, list[0][e.employeeId])
        assertEquals(2, list[1][e.employeeId])
    }

    @Test
    fun template(info: TestInfo) = inTransaction(db, info) {
        val flow = db.flowQuery {
            QueryDsl.fromTemplate("select address_id from address order by address_id")
                .select { it.int("address_id") }
        }
        assertEquals((1..15).toList(), flow.toList())
    }

    @Test
    fun flowTransaction(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }.first()
        val flow: Flow<Address> = db.flowTransaction {
            val address = db.runQuery(query)
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
            val addressFlow = db.flowQuery { QueryDsl.from(a).orderBy(a.addressId) }
            emitAll(addressFlow)
        }
        val list = flow.toList()
        assertEquals(15, list.size)
        assertEquals(Address(15, "TOKYO", 2), list.last())
        val address = db.runQuery(query)
        assertEquals(Address(15, "TOKYO", 2), address)
    }

    /*
    # Description

    When running on SQL Server, the following error occurs, and subsequent tests may also start failing.
    Since the issue doesn’t reproduce consistently, it appears to be related to multithreading. From the error message,
    it seems that a parameterized query—one that is not issued by the application itself—is being generated
    unintentionally due to some internal defect, and executed without the required parameter (`@p2`) being supplied.

    ## Stacktrace

    The parameterized query '(@p1 int,@p2 int)select t0_.address_id, t0_.street, t0_.version ' expects the parameter '@p2', which was not supplied.
io.r2dbc.mssql.ExceptionFactory$MssqlNonTransientException: [8178] [S0001] The parameterized query '(@p1 int,@p2 int)select t0_.address_id, t0_.street, t0_.version ' expects the parameter '@p2', which was not supplied.
	at io.r2dbc.mssql.ExceptionFactory.createException(ExceptionFactory.java:154)
	at io.r2dbc.mssql.DefaultMssqlResult.lambda$doMap$3(DefaultMssqlResult.java:229)
	at reactor.core.publisher.FluxHandleFuseable$HandleFuseableSubscriber.onNext(FluxHandleFuseable.java:179)
	at reactor.core.publisher.FluxWindowPredicate$WindowFlux.drainRegular(FluxWindowPredicate.java:670)
	at reactor.core.publisher.FluxWindowPredicate$WindowFlux.drain(FluxWindowPredicate.java:748)
	at reactor.core.publisher.FluxWindowPredicate$WindowFlux.subscribe(FluxWindowPredicate.java:823)
	at reactor.core.publisher.Flux.subscribe(Flux.java:8773)
	at kotlinx.coroutines.reactive.PublisherAsFlow.collectImpl(ReactiveFlow.kt:90)
	at kotlinx.coroutines.reactive.PublisherAsFlow.collect(ReactiveFlow.kt:75)
	at org.komapper.r2dbc.R2dbcExecutor$executeQuery$1$1$1$1.emit(R2dbcExecutor.kt:44)
	at org.komapper.r2dbc.R2dbcExecutor$executeQuery$1$1$1$1.emit(R2dbcExecutor.kt:40)
	at kotlinx.coroutines.reactive.PublisherAsFlow.collectImpl(ReactiveFlow.kt:96)
	at kotlinx.coroutines.reactive.PublisherAsFlow.access$collectImpl(ReactiveFlow.kt:44)
	at kotlinx.coroutines.reactive.PublisherAsFlow$collectImpl$1.invokeSuspend(ReactiveFlow.kt)
	at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:34)
	at kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:100)
	at kotlinx.coroutines.EventLoopImplBase.processNextEvent(EventLoop.common.kt:263)
	at kotlinx.coroutines.BlockingCoroutine.joinBlocking(Builders.kt:94)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking(Builders.kt:70)
	at kotlinx.coroutines.BuildersKt.runBlocking(Unknown Source)
	at kotlinx.coroutines.BuildersKt__BuildersKt.runBlocking$default(Builders.kt:48)
	at kotlinx.coroutines.BuildersKt.runBlocking$default(Unknown Source)
	at integration.r2dbc.R2dbcTestUtilityKt.runBlockingWithTimeout(R2dbcTestUtility.kt:22)
	at integration.r2dbc.R2dbcTestUtilityKt.runBlockingWithTimeout$default(R2dbcTestUtility.kt:21)
	at integration.r2dbc.R2dbcTestUtilityKt.inTransaction(R2dbcTestUtility.kt:11)
	at integration.r2dbc.R2dbcFlowTest.flowTransaction_setRollbackOnly(R2dbcFlowTest.kt:255)
	at java.base/java.lang.reflect.Method.invoke(Method.java:569)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)

    ## Workaround

    Skip execution on SQL Server.
     */
    @Run(unless = [Dbms.SQLSERVER])
    @Test
    fun flowTransaction_setRollbackOnly(info: TestInfo) = inTransaction(db, info) {
        val a = Meta.address
        val query = QueryDsl.from(a).where { a.addressId eq 15 }.first()
        val flow: Flow<Address> = db.flowTransaction(TransactionAttribute.REQUIRES_NEW) { tx ->
            val address = db.runQuery(query)
            db.runQuery { QueryDsl.update(a).single(address.copy(street = "TOKYO")) }
            tx.setRollbackOnly()
            val addressFlow = db.flowQuery { QueryDsl.from(a).orderBy(a.addressId) }
            emitAll(addressFlow)
        }
        val list = flow.toList()
        assertEquals(15, list.size)
        assertEquals(Address(15, "TOKYO", 2), list.last())
        val address = db.runQuery(query)
        assertEquals(Address(15, "STREET 15", 1), address)
    }
}
