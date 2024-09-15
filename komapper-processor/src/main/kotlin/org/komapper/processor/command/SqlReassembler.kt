package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Modifier
import org.komapper.annotation.KomapperPartial
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.sql.NoCacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import org.komapper.core.template.sql.SqlLocation
import org.komapper.core.template.sql.SqlNode
import org.komapper.core.template.sql.SqlNodeFactory
import org.komapper.processor.Context
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue

/**
 * A class responsible for reassembling SQL template from partial SQL fragments.
 *
 * @property sql the initial SQL template
 * @property paramMap the parameter map
 * @property nodeFactory the SQL node factory
 * @property exprValidator the expression validator
 */
internal class SqlReassembler(
    context: Context,
    private val sql: String,
    private val paramMap: Map<String, KSType>,
    private val nodeFactory: SqlNodeFactory = NoCacheSqlNodeFactory(),
    private val exprValidator: ExprValidator = ExprValidator(context),
) {

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
            val classDeclaration = evalResult.type.declaration as? KSClassDeclaration
                ?: throw SqlException("The declaration of expression \"$expression\" must be a class at ${node.location}")
            if (classDeclaration.modifiers.contains(Modifier.SEALED)) {
                buf.append("/*%if $expression != null */")
                processSealedSubclasses(buf, node, classDeclaration)
                buf.append("/*%end */")
            } else {
                val qualifiedName = classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString()
                val sql = getPartialSql(classDeclaration, qualifiedName, node)
                buf.append("/*%if $expression != null *//*%with $expression */$sql/*%end *//*%end */")
            }
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

    private fun getPartialSql(classDeclaration: KSClassDeclaration, qualifiedName: String, node: SqlNode.PartialDirective): String {
        val partial = classDeclaration.findAnnotation(KomapperPartial::class)
            ?: throw SqlPartialAnnotationNotFoundException("The declaration \"${qualifiedName}\" of expression \"${node.expression}\" must be annotated with @KomapperPartial at ${node.location}")
        return partial.findValue("sql")?.toString()?.trimIndent()
            ?: throw SqlPartialAnnotationElementNotFoundException("The sql element of @KomapperPartial is not found at ${node.location}")
    }

    private fun processSealedSubclasses(buf: StringBuilder, node: SqlNode.PartialDirective, classDeclaration: KSClassDeclaration) {
        for (subclassDeclaration in classDeclaration.getSealedSubclasses()) {
            if (subclassDeclaration.modifiers.contains(Modifier.SEALED)) {
                processSealedSubclasses(buf, node, subclassDeclaration)
            } else {
                val packageName = subclassDeclaration.packageName.asString()
                val qualifiedName = subclassDeclaration.qualifiedName?.asString() ?: subclassDeclaration.simpleName.asString()
                val packageRemovedName = qualifiedName.removePrefix("$packageName.")
                val packageRemovedBinaryName = packageRemovedName.replace(".", "$")
                val binaryName = "$packageName.$packageRemovedBinaryName"

                val sql = getPartialSql(subclassDeclaration, qualifiedName, node)
                buf.append("/*%if ${node.expression} is @$binaryName@ *//*%with ${node.expression} as @$binaryName@ */$sql/*%end *//*%end */")
            }
        }
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
