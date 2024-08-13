package org.komapper.processor.command

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.sql.NoCacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import org.komapper.core.template.sql.SqlLocation
import org.komapper.core.template.sql.SqlNode
import org.komapper.processor.Context

internal class SqlValidator(context: Context, val command: Command) {

    private val intType = context.resolver.builtIns.intType
    private val booleanType = context.resolver.builtIns.booleanType
    private val iterableType = context.resolver.builtIns.iterableType

    private val nodeFactory = NoCacheSqlNodeFactory()
    private val exprValidator = ExprValidator(context)

    fun validate(): Set<String> {
        val node = nodeFactory.get(command.sql)
        visit(command.paramMap, node)
        return exprValidator.usedParams
    }

    private fun visit(paramMap: Map<String, KSType>, node: SqlNode): Map<String, KSType> = when (node) {
        is SqlNode.Statement -> node.nodeList.fold(paramMap, ::visit)
        is SqlNode.Set -> {
            visit(paramMap, node.left).let {
                visit(it, node.right)
            }
        }

        is SqlNode.Clause.Select -> {
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.Clause.From -> {
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.Clause.ForUpdate -> {
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.Clause -> {
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.BiLogicalOp -> {
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.Token -> {
            paramMap
        }

        is SqlNode.Paren -> {
            visit(paramMap, node.node)
        }

        is SqlNode.BindValueDirective -> {
            val evalResult = validateExpression(node.location, node.expression, paramMap)
            when (node.node) {
                is SqlNode.Paren -> {
                    if (!iterableType.isAssignableFrom(evalResult.type)) {
                        throw SqlException("The expression must be Iterable at ${evalResult.location} at ${node.location}")
                    }
                }

                else -> Unit
            }
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.EmbeddedValueDirective -> {
            validateExpression(node.location, node.expression, paramMap)
            paramMap
        }

        is SqlNode.LiteralValueDirective -> {
            validateExpression(node.location, node.expression, paramMap)
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.IfBlock -> {
            val ifEvalResult = validateExpression(node.ifDirective.location, node.ifDirective.expression, paramMap)
            if (ifEvalResult.type != booleanType) {
                throw SqlException("The expression eval result must be a Boolean at ${ifEvalResult.location} at ${node.ifDirective.location}.")
            }
            val ifParamMap = node.ifDirective.nodeList.fold(paramMap, ::visit)

            val elseifParamMap = node.elseifDirectives.fold(ifParamMap) { map, elseifDirective ->
                val elseifEvalResult = validateExpression(elseifDirective.location, elseifDirective.expression, map)
                if (elseifEvalResult.type != booleanType) {
                    throw SqlException("The expression eval result must be a Boolean at ${elseifEvalResult.location} at ${elseifDirective.location}.")
                }
                elseifDirective.nodeList.fold(map, ::visit)
            }

            node.elseDirective?.nodeList?.fold(elseifParamMap, ::visit) ?: elseifParamMap
        }

        is SqlNode.ForBlock -> {
            val forDirective = node.forDirective
            val id = forDirective.identifier
            val expression = forDirective.expression

            val evalResult = validateExpression(forDirective.location, expression, paramMap)
            if (!iterableType.isAssignableFrom(evalResult.type)) {
                throw SqlException("The expression must be Iterable at ${evalResult.location} at ${forDirective.location}.")
            }
            val resultDeclaration = evalResult.type.declaration as? KSClassDeclaration
                ?: throw SqlException("The expression must be a class at ${evalResult.location} at ${forDirective.location}.")
            val typeArgs = if (resultDeclaration.qualifiedName?.asString() == Iterable::class.qualifiedName) {
                evalResult.type.arguments
            } else {
                resultDeclaration.getAllSuperTypes().filter {
                    it.declaration.qualifiedName?.asString() == Iterable::class.qualifiedName
                }.map {
                    it.arguments
                }.firstOrNull() ?: emptyList()
            }

            if (typeArgs.isEmpty()) {
                throw SqlException("The Iterable expression must have a type argument at ${evalResult.location} at ${forDirective.location}.")
            }
            val typeArg = typeArgs.first().type?.resolve()
                ?: throw SqlException("The Iterable type argument is illegal at ${evalResult.location} at ${forDirective.location}.")

            val newParamMap = paramMap + mapOf(
                id to typeArg,
                id + "_index" to intType,
                id + "_has_next" to booleanType,
            )

            forDirective.nodeList.fold(newParamMap, ::visit)
        }

        is SqlNode.IfDirective,
        is SqlNode.ElseifDirective,
        is SqlNode.ElseDirective,
        is SqlNode.EndDirective,
        is SqlNode.ForDirective,
        is SqlNode.PartialDirective,
        -> error("unreachable")
    }

    private fun validateExpression(location: SqlLocation, expression: String, paramMap: Map<String, KSType>): ExprEvalResult {
        return try {
            exprValidator.validate(expression, paramMap)
        } catch (e: ExprException) {
            throw SqlException("The expression evaluation was failed. ${e.message} at $location. ", e)
        }
    }
}
