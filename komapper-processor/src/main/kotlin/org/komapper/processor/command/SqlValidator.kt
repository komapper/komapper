package org.komapper.processor.command

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
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

    private val intType = context.resolver.builtIns.intType
    private val booleanType = context.resolver.builtIns.booleanType
    private val iterableType = context.resolver.builtIns.iterableType

    fun validate() {
        val nodeFactory = NoCacheSqlNodeFactory()
        val node = nodeFactory.get(command.sql)
        val paramMap = command.parameters.asSequence()
            .filter { it.name != null }
            .associate { it.name!!.asString() to it.type.resolve() }
        visit(ExprContext(paramMap, TemplateBuiltinExtensions { it }), node)
    }

    private fun visit(exprCtx: ExprContext, node: SqlNode): ExprContext = when (node) {
        is SqlNode.Statement -> node.nodeList.fold(exprCtx, ::visit)
        is SqlNode.Set -> {
            visit(exprCtx, node.left)
            visit(exprCtx, node.right)
            exprCtx
        }

        is SqlNode.Clause.Select -> {
            node.nodeList.fold(exprCtx, ::visit)
        }

        is SqlNode.Clause.From -> {
            node.nodeList.fold(exprCtx, ::visit)
        }

        is SqlNode.Clause.ForUpdate -> {
            node.nodeList.fold(exprCtx, ::visit)
        }

        is SqlNode.Clause -> {
            node.nodeList.fold(exprCtx, ::visit)
            exprCtx
        }

        is SqlNode.BiLogicalOp -> {
            node.nodeList.fold(exprCtx, ::visit)
        }

        is SqlNode.Token -> {
            exprCtx
        }

        is SqlNode.Paren -> {
            visit(exprCtx, node.node)
        }

        is SqlNode.BindValueDirective -> {
            val result = validateExpression(node.location, node.expression, exprCtx)
            when (node.node) {
                is SqlNode.Paren -> {
                    if (!iterableType.isAssignableFrom(result)) {
                        TODO("The expression must be Iterable at ${node.location}")
                    }
                }

                else -> Unit
            }
            node.nodeList.fold(exprCtx, ::visit)
        }

        is SqlNode.EmbeddedValueDirective -> {
            validateExpression(node.location, node.expression, exprCtx)
            exprCtx
        }

        is SqlNode.LiteralValueDirective -> {
            validateExpression(node.location, node.expression, exprCtx)
            node.nodeList.fold(exprCtx, ::visit)
        }

        is SqlNode.IfBlock -> {
            val result = validateExpression(node.ifDirective.location, node.ifDirective.expression, exprCtx)
            if (result != booleanType) {
                throw SqlException("The expression evaluation result must be a Boolean at <${node.ifDirective.expression}> at ${node.ifDirective.location}.")
            }
            node.ifDirective.nodeList.fold(exprCtx, ::visit)
            node.elseifDirectives.forEach {
                val result = validateExpression(it.location, it.expression, exprCtx)
                if (result != booleanType) {
                    throw SqlException("The expression evaluation result must be a Boolean at ${it.location}.")
                }
                it.nodeList.fold(exprCtx, ::visit)
            }
            node.elseDirective?.nodeList?.fold(exprCtx, ::visit)
            exprCtx
        }

        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            val id = forDirective.identifier
            val expression = forDirective.expression

            val resultType = validateExpression(forDirective.location, expression, exprCtx)
            if (!iterableType.isAssignableFrom(resultType)) {
                throw SqlException("The expression must be Iterable at ${forDirective.location}.")
            }
            val resultDeclaration = resultType.declaration as? KSClassDeclaration
                ?: throw SqlException("The expression must be a class at ${forDirective.location}.")
            val typeArgs = if (resultDeclaration.qualifiedName?.asString() == Iterable::class.qualifiedName) {
                resultType.arguments
            } else {
                resultDeclaration.getAllSuperTypes().filter {
                    it.declaration.qualifiedName?.asString() == Iterable::class.qualifiedName
                }.map {
                    it.arguments
                }.firstOrNull() ?: emptyList()
            }

            if (typeArgs.isEmpty()) {
                throw SqlException("The Iterable expression must have a type argument at ${forDirective.location}.")
            }
            val typeArg = typeArgs.first()

            val typeArgType = typeArg.type
                ?: throw SqlException("The Iterable type argument must have a non-null type at ${forDirective.location}.")

            val newExprCtx = exprCtx.copy(
                paramMap = exprCtx.paramMap + mapOf(
                    id to typeArgType.resolve(),
                    id + "_index" to intType,
                    id + "_has_next" to booleanType,
                ),
            )

            forDirective.nodeList.fold(newExprCtx, ::visit)
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
