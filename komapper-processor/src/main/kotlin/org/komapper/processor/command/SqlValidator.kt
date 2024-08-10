package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSType
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.processor.Context
import org.komapper.processor.command.ExprValidator.ExprContext
import org.komapper.template.expression.ExprException
import org.komapper.template.sql.NoCacheSqlNodeFactory
import org.komapper.template.sql.SqlException
import org.komapper.template.sql.SqlLocation
import org.komapper.template.sql.SqlNode

internal class SqlValidator(private val context: Context, private val command: Command) {

    private val booleanType = context.resolver.builtIns.booleanType
    private val iterableType = context.resolver.builtIns.iterableType

    fun validate() {
        val nodeFactory = NoCacheSqlNodeFactory()
        val node = nodeFactory.get(command.sql)
        val paramMap = command.parameters.associateBy { it.name!!.asString() }
        visit(ExprContext(paramMap, TemplateBuiltinExtensions { it }), node)
    }

    private fun visit(ctx: ExprContext, node: SqlNode): ExprContext = when (node) {
        is SqlNode.Statement -> node.nodeList.fold(ctx, ::visit)
        is SqlNode.Set -> {
            visit(ctx, node.left)
            visit(ctx, node.right)
            ctx
        }

        is SqlNode.Clause.Select -> {
            node.nodeList.fold(ctx, ::visit)
        }

        is SqlNode.Clause.From -> {
            node.nodeList.fold(ctx, ::visit)
        }

        is SqlNode.Clause.ForUpdate -> {
            node.nodeList.fold(ctx, ::visit)
        }

        is SqlNode.Clause -> {
            node.nodeList.fold(ctx, ::visit)
            ctx
        }

        is SqlNode.BiLogicalOp -> {
            node.nodeList.fold(ctx, ::visit)
        }

        is SqlNode.Token -> {
            ctx
        }

        is SqlNode.Paren -> {
            visit(ctx, node.node)
        }

        is SqlNode.BindValueDirective -> {
            val result = validateExpression(node.location, node.expression, ctx)
            when (node.node) {
                is SqlNode.Paren -> {
                    if (!iterableType.isAssignableFrom(result)) {
                        TODO("The expression must be Iterable at ${node.location}")
                    }
                }

                else -> Unit
            }
            node.nodeList.fold(ctx, ::visit)
        }

        is SqlNode.EmbeddedValueDirective -> {
            validateExpression(node.location, node.expression, ctx)
            ctx
        }

        is SqlNode.LiteralValueDirective -> {
            validateExpression(node.location, node.expression, ctx)
            node.nodeList.fold(ctx, ::visit)
        }

        is SqlNode.IfBlock -> {
            val result = validateExpression(node.ifDirective.location, node.ifDirective.expression, ctx)
            if (result != booleanType) {
                throw SqlException("The expression evaluation result must be a Boolean at ${node.ifDirective.location}.")
            }
            node.ifDirective.nodeList.fold(ctx, ::visit)
            node.elseifDirectives.forEach {
                val result = validateExpression(it.location, it.expression, ctx)
                if (result != booleanType) {
                    throw SqlException("The expression evaluation result must be a Boolean at ${it.location}.")
                }
                it.nodeList.fold(ctx, ::visit)
            }
            node.elseDirective?.nodeList?.fold(ctx, ::visit)
            ctx
        }

        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            val id = forDirective.identifier
            TODO()
        }

        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective,
        -> error("unreachable")
    }

    private fun validateExpression(location: SqlLocation, expression: String, ctx: ExprContext): KSType {
        return try {
            ExprValidator(context, expression).validate(ctx)
        } catch (e: ExprException) {
            throw SqlException("The expression evaluation was failed. ${e.message} at $location. ", e)
        }
    }
}
