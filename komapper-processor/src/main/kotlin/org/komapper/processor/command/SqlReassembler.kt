package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSType
import org.komapper.annotation.KomapperPartial
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.sql.NoCacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import org.komapper.core.template.sql.SqlLocation
import org.komapper.core.template.sql.SqlNode
import org.komapper.processor.Context
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue

/**
 * A class responsible for reassembling SQL template from partial SQL fragments.
 *
 * @property sql the initial SQL template
 * @property paramMap the parameter map
 */
internal class SqlReassembler(context: Context, private val sql: String, private val paramMap: Map<String, KSType>) {

    private val nodeFactory = NoCacheSqlNodeFactory()
    private val exprValidator = ExprValidator(context)

    fun assemble(): String {
        val node = nodeFactory.get(sql)
        val buf = StringBuilder(sql.length + 100)
        visit(buf, node)
        return buf.toString()
    }

    private fun visit(buf: StringBuilder, node: SqlNode): StringBuilder = when (node) {
        is SqlNode.Statement -> {
            node.nodeList.fold(buf, ::visit)
        }

        is SqlNode.Set -> {
            visit(buf, node.left)
                .let {
                    it.append(node.keyword)
                }.let {
                    visit(it, node.right)
                }
        }

        is SqlNode.Clause -> {
            buf.append(node.keyword).let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.BiLogicalOp -> {
            buf.append(node.keyword).let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.Token -> {
            buf.append(node.token)
        }

        is SqlNode.Paren -> {
            buf.append("(").let {
                visit(it, node.node)
            }.let {
                it.append(")")
            }
        }

        is SqlNode.BindValueDirective -> {
            buf.append(node.token).let {
                visit(it, node.node)
            }.let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.EmbeddedValueDirective -> {
            buf.append(node.token)
        }

        is SqlNode.PartialDirective -> {
            val expression = node.expression
            val evalResult = validateExpression(node.location, expression, paramMap)
            val partial = evalResult.type.declaration.findAnnotation(KomapperPartial::class)
                ?: throw SqlPartialAnnotationNotFoundException("The declaration of expression \"$expression\" must be annotated with @KomapperPartial at ${node.location}")
            val sql = partial.findValue("sql")?.toString()
                ?: throw SqlPartialAnnotationElementNotFoundException("The sql element of @KomapperCommand is not found at ${node.location}")
            buf.append("/*%if $expression != null *//*%with $expression */$sql/*%end *//*%end */")
        }

        is SqlNode.LiteralValueDirective -> {
            buf.append(node.token).let {
                visit(it, node.node)
            }.let {
                node.nodeList.fold(it, ::visit)
            }
        }

        is SqlNode.IfBlock -> {
            val ifBuf = buf.append(node.ifDirective.token).let {
                node.ifDirective.nodeList.fold(it, ::visit)
            }

            val elseIfBuf = node.elseifDirectives.fold(ifBuf) { s, elseIfDirective ->
                s.append(elseIfDirective.token).let {
                    elseIfDirective.nodeList.fold(it, ::visit)
                }
            }

            val elseBuf = elseIfBuf.append(node.elseDirective?.token ?: "").let {
                node.elseDirective?.nodeList?.fold(it, ::visit)
            }

            (elseBuf ?: elseIfBuf).append(node.endDirective.token)
        }

        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            buf.append(forDirective.token).let {
                forDirective.nodeList.fold(it, ::visit)
            }.let {
                it.append(node.endDirective.token)
            }
        }

        is SqlNode.WithBlock -> {
            val withDirective = node.withDirective
            buf.append(withDirective.token).let {
                withDirective.nodeList.fold(it, ::visit)
            }.let {
                it.append(node.endDirective.token)
            }
        }

        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective,
        is SqlNode.WithDirective,
        -> error("unreachable")
    }

    private fun validateExpression(location: SqlLocation, expression: String, paramMap: Map<String, KSType>): ExprEvalResult {
        return try {
            exprValidator.validate(expression, paramMap)
        } catch (e: ExprException) {
            throw SqlPartialEvaluationException("The expression evaluation was failed. ${e.message} at $location. ", e)
        }
    }

    class SqlPartialEvaluationException(message: String, cause: Throwable) : SqlException(message, cause)
    class SqlPartialAnnotationNotFoundException(message: String) : SqlException(message)
    class SqlPartialAnnotationElementNotFoundException(message: String) : SqlException(message)
}
