package org.komapper.processor.command

import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Variance
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.sql.NoCacheSqlNodeFactory
import org.komapper.core.template.sql.SqlException
import org.komapper.core.template.sql.SqlLocation
import org.komapper.core.template.sql.SqlNode
import org.komapper.core.template.sql.SqlNodeFactory
import org.komapper.processor.Context
import org.komapper.processor.resolveTypeArgumentsOfAncestor

internal class SqlValidator(
    context: Context,
    private val sql: String,
    private val paramMap: Map<String, KSType>,
    private val nodeFactory: SqlNodeFactory = NoCacheSqlNodeFactory(),
    private val exprValidator: ExprValidator = ExprValidator(context),
) {

    private val intType = context.resolver.builtIns.intType
    private val booleanType = context.resolver.builtIns.booleanType
    private val iterableType = context.resolver.builtIns.iterableType

    fun validate(): Set<String> {
        val node = nodeFactory.get(sql)
        visit(paramMap, node)
        return exprValidator.usedParams
    }

    private fun visit(paramMap: Map<String, KSType>, node: SqlNode): Map<String, KSType> = when (node) {
        is SqlNode.Statement -> {
            node.nodeList.fold(paramMap, ::visit)
        }

        is SqlNode.Set -> {
            visit(paramMap, node.left).let {
                visit(it, node.right)
            }
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
                        throw ExprMustBeIterableException("The expression must be Iterable at ${evalResult.location} at ${node.location}")
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
                throw ExprMustBeBooleanException("The expression must be a Boolean at ${ifEvalResult.location} at ${node.ifDirective.location}.")
            }
            val ifParamMap = node.ifDirective.nodeList.fold(paramMap, ::visit)

            val elseifParamMap = node.elseifDirectives.fold(ifParamMap) { map, elseifDirective ->
                val elseifEvalResult = validateExpression(elseifDirective.location, elseifDirective.expression, map)
                if (elseifEvalResult.type != booleanType) {
                    throw ExprMustBeBooleanException("The expression must be a Boolean at ${elseifEvalResult.location} at ${elseifDirective.location}.")
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

            val typeArgs = resolveTypeArgumentsOfAncestor(evalResult.type, iterableType)
            if (typeArgs.isEmpty()) {
                throw ExprMustBeIterableException("The expression must be Iterable at ${evalResult.location} at ${forDirective.location}.")
            }
            val typeArg = typeArgs.first()
            if (typeArg.variance == Variance.STAR) {
                throw StarProjectionNotSupportedException("Specifying a star projection for Iterable is not supported at ${evalResult.location} at ${forDirective.location}.")
            }
            val type = typeArg.type?.resolve()
                ?: throw CannotResolveTypeArgumentException("Cannot resolve type argument of Iterable at ${evalResult.location} at ${forDirective.location}.")
            val newParamMap = paramMap + mapOf(
                id to type,
                id + "_index" to intType,
                id + "_has_next" to booleanType,
            )
            forDirective.nodeList.fold(newParamMap, ::visit)
        }

        is SqlNode.WithBlock -> {
            val withDirective = node.withDirective
            val expression = withDirective.expression
            val evalResult = validateExpression(withDirective.location, expression, paramMap)
            val classDeclaration = evalResult.type.declaration as? KSClassDeclaration
                ?: error("The expression must be a class at ${evalResult.location}.")
            val propertyMap = classDeclaration.getAllProperties()
                .filter { it.isPublic() }
                .associate { it.simpleName.asString() to it.type.resolve() }
            val newParamMap = paramMap + propertyMap
            withDirective.nodeList.fold(newParamMap, ::visit)
        }

        is SqlNode.PartialDirective -> {
            throw PartialDirectiveNotSupportedException("The partial directive \"${node.token}\" is not supported at ${node.location}.")
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
            throw SqlException("The expression evaluation was failed. ${e.message} at $location. ", e)
        }
    }

    class ExprMustBeIterableException(message: String) : SqlException(message)
    class ExprMustBeBooleanException(message: String) : SqlException(message)
    class StarProjectionNotSupportedException(message: String) : SqlException(message)
    class CannotResolveTypeArgumentException(message: String) : SqlException(message)
    class PartialDirectiveNotSupportedException(message: String) : SqlException(message)
}
