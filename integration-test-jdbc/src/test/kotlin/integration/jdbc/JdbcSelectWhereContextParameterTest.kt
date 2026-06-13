package integration.jdbc

import integration.core.Dbms
import integration.core.Run
import integration.core.employee
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.dsl.Meta
import org.komapper.core.dsl.QueryDsl
import org.komapper.core.dsl.expression.ColumnExpression
import org.komapper.core.dsl.expression.Operand
import org.komapper.core.dsl.scope.FilterScope
import org.komapper.jdbc.JdbcDatabase
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * User-defined operators implemented with context parameters (Kotlin 2.4).
 *
 * Unlike [JdbcSelectWhereTest.MyExtension], these operators are plain top-level
 * functions and can be used directly inside a `where { }` block without wrapping
 * them in an `extension { }` block. The implicit `FilterScope` receiver of the
 * block satisfies the `context(scope: FilterScope<*>)` parameter.
 */
@ExtendWith(JdbcEnv::class)
class JdbcSelectWhereContextParameterTest(private val db: JdbcDatabase) {
    @Test
    @Run(onlyIf = [Dbms.POSTGRESQL])
    fun regexOperators() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e).where {
                e.salary greaterEq BigDecimal(1000)
                e.employeeName `~` "S"
                e.employeeName `!~` "T"
            }.orderBy(e.employeeName)
        }
        assertEquals(listOf("ADAMS", "JONES"), list.map { it.employeeName })
    }

    @Test
    @Run(onlyIf = [Dbms.POSTGRESQL])
    fun regexOperators_or_and() {
        val e = Meta.employee
        val list = db.runQuery {
            QueryDsl.from(e).where {
                e.salary greaterEq BigDecimal(1000)
                e.employeeName `~` "S"
                or {
                    e.employeeName `~` "T"
                    and {
                        e.employeeName `~` "S"
                    }
                }
            }.orderBy(e.employeeName)
        }
        assertEquals(listOf("ADAMS", "JONES", "SCOTT", "SMITH"), list.map { it.employeeName })
    }

    @Test
    fun equivalentToBuiltinOperator() {
        val e = Meta.employee
        val custom = db.runQuery {
            QueryDsl.from(e).where {
                e.employeeName eqx "SMITH"
            }.orderBy(e.employeeId)
        }
        val builtin = db.runQuery {
            QueryDsl.from(e).where {
                e.employeeName eq "SMITH"
            }.orderBy(e.employeeId)
        }
        assertEquals(builtin, custom)
        assertTrue(custom.isNotEmpty())
    }

    @Test
    fun nesting() {
        val e = Meta.employee
        // The user-defined operator nested inside `or`/`and` must register its
        // criterion in the correct (innermost) group. We verify this by comparing
        // against the built-in `eq` operator used in the very same structure:
        // if the criterion leaked to the top-level group, the results would differ.
        val custom = db.runQuery {
            QueryDsl.from(e).where {
                e.employeeName eqx "SMITH"
                or {
                    e.employeeName eqx "SCOTT"
                    and {
                        e.employeeName eqx "KING"
                    }
                }
            }.orderBy(e.employeeId)
        }
        val builtin = db.runQuery {
            QueryDsl.from(e).where {
                e.employeeName eq "SMITH"
                or {
                    e.employeeName eq "SCOTT"
                    and {
                        e.employeeName eq "KING"
                    }
                }
            }.orderBy(e.employeeId)
        }
        assertEquals(builtin, custom)
        assertTrue(custom.isNotEmpty())
    }
}

/**
 * The PostgreSQL `~` (regex match) operator.
 */
context(scope: FilterScope<*>)
private infix fun <T : Any> ColumnExpression<T, String>.`~`(pattern: T?) {
    if (pattern == null) return
    val left = Operand.Column(this)
    val right = Operand.Argument(this, pattern)
    scope.criteriaContext.add {
        visit(left)
        append(" ~ ")
        visit(right)
    }
}

/**
 * The PostgreSQL `!~` (regex not-match) operator.
 */
context(scope: FilterScope<*>)
private infix fun <T : Any> ColumnExpression<T, String>.`!~`(pattern: T?) {
    if (pattern == null) return
    val left = Operand.Column(this)
    val right = Operand.Argument(this, pattern)
    scope.criteriaContext.add {
        visit(left)
        append(" !~ ")
        visit(right)
    }
}

/**
 * A database-agnostic `=` operator, defined purely to exercise the mechanism.
 */
context(scope: FilterScope<*>)
private infix fun <T : Any, S : Any> ColumnExpression<T, S>.eqx(value: T?) {
    if (value == null) return
    val left = Operand.Column(this)
    val right = Operand.Argument(this, value)
    scope.criteriaContext.add {
        visit(left)
        append(" = ")
        visit(right)
    }
}
