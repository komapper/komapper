package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.Variance
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.core.template.expression.ExprArgList
import org.komapper.processor.Context
import org.komapper.template.expression.ExprException
import org.komapper.template.expression.ExprLocation
import org.komapper.template.expression.ExprNode
import org.komapper.template.expression.ExprNodeFactory
import org.komapper.template.expression.NoCacheExprNodeFactory
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

internal class ExpressionValidator(private val context: Context, private val expression: String) {

    private val booleanType = context.resolver.builtIns.booleanType
    private val unitType = context.resolver.builtIns.unitType
    private val comparableType = context.resolver.extensions.comparableType
    private val exprArgListDeclaration = context.resolver.getKSNameFromString(ExprArgList::class.qualifiedName!!).let {
        context.resolver.getClassDeclarationByName(it) ?: error("Class not found: ${it.asString()}")
    }

    private val exprNodeFactory: ExprNodeFactory = NoCacheExprNodeFactory()

    fun validate(ctx: ExprContext): KSType {
        val node = exprNodeFactory.get(expression)
        return visit(node, ctx)
    }

    private fun visit(node: ExprNode, ctx: ExprContext): KSType = when (node) {
        is ExprNode.Not -> perform(node.location, node.operand, ctx)
        is ExprNode.And -> perform(node.location, node.left, node.right, ctx)
        is ExprNode.Or -> perform(node.location, node.left, node.right, ctx)
        is ExprNode.Eq -> equal(node.location, node.left, node.right, ctx)
        is ExprNode.Ne -> equal(node.location, node.left, node.right, ctx)
        is ExprNode.Ge -> compare(node.location, node.left, node.right, ctx)
        is ExprNode.Gt -> compare(node.location, node.left, node.right, ctx)
        is ExprNode.Le -> compare(node.location, node.left, node.right, ctx)
        is ExprNode.Lt -> compare(node.location, node.left, node.right, ctx)
        is ExprNode.Literal -> {
            when (node.type) {
                typeOf<Byte>() -> context.resolver.builtIns.byteType
                typeOf<Short>() -> context.resolver.builtIns.shortType
                typeOf<Int>() -> context.resolver.builtIns.intType
                typeOf<Long>() -> context.resolver.builtIns.longType
                typeOf<Float>() -> context.resolver.builtIns.floatType
                typeOf<Double>() -> context.resolver.builtIns.doubleType
                typeOf<Char>() -> context.resolver.builtIns.charType
                typeOf<Boolean>() -> context.resolver.builtIns.booleanType
                typeOf<String>() -> context.resolver.builtIns.stringType
                else -> {
                    val klass = node.type.classifier as? KClass<*>
                    klass?.qualifiedName?.let {
                        context.resolver.getKSNameFromString(it)
                    }?.let {
                        context.resolver.getClassDeclarationByName(it)?.asStarProjectedType()
                    } ?: throw ExprException("The type \"${node.type}\" is not found at ${node.location}")
                }
            }
        }

        is ExprNode.Comma -> {
            val typeArguments = node.nodeList
                .map {
                    visit(it, ctx)
                }.map {
                    val refType = context.resolver.createKSTypeReferenceFromKSType(it)
                    context.resolver.getTypeArgument(refType, Variance.INVARIANT)
                }
            exprArgListDeclaration.asType(typeArguments)
        }

        is ExprNode.ClassRef -> visitClassRef(node, ctx)
        is ExprNode.Value -> visitValue(node, ctx)
        is ExprNode.Property -> visitProperty(node, ctx)
        is ExprNode.Function -> visitFunction(node, ctx)
        is ExprNode.Empty -> context.resolver.builtIns.unitType
    }

    private fun perform(
        location: ExprLocation,
        operand: ExprNode,
        ctx: ExprContext,
    ): KSType {
        val value = visit(operand, ctx)
        if (value != booleanType) {
            throw ExprException(
                "Cannot perform the logical operator because the operands is not Boolean at $location",
            )
        }
        return booleanType
    }

    private fun perform(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        ctx: ExprContext,
    ): KSType {
        val left = visit(leftNode, ctx)
        val right = visit(rightNode, ctx)
        if (left != booleanType || right != booleanType) {
            throw ExprException(
                "Cannot perform the logical operator because either operands is not Boolean at $location",
            )
        }
        return booleanType
    }

    @Suppress("UNUSED_PARAMETER")
    private fun equal(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        ctx: ExprContext,
    ): KSType {
        visit(leftNode, ctx)
        visit(rightNode, ctx)
        return booleanType
    }

    @Suppress("UNCHECKED_CAST")
    private fun compare(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        ctx: ExprContext,
    ): KSType {
        val left = visit(leftNode, ctx).makeNullable()
        val right = visit(rightNode, ctx).makeNullable()
        if (left != right) {
            throw ExprException(
                "Cannot compare because the operands are not the same type at $location",
            )
        }
        if (!comparableType.isAssignableFrom(left)) {
            throw ExprException(
                "Cannot compare because the left operand is not Comparable type at $location",
            )
        }
        if (!comparableType.isAssignableFrom(right)) {
            throw ExprException(
                "Cannot compare because the left operand is not Comparable type at $location",
            )
        }
        return booleanType
    }

    private fun visitClassRef(node: ExprNode.ClassRef, @Suppress("UNUSED_PARAMETER") ctx: ExprContext): KSType {
        return context.resolver.getKSNameFromString(node.name).let {
            context.resolver.getClassDeclarationByName(it)?.asStarProjectedType()
        } ?: throw ExprException("The class \"${node.name}\" is not found at ${node.location}")
    }

    private fun visitValue(node: ExprNode.Value, ctx: ExprContext): KSType {
        val param = ctx.valueMap[node.name] // TODO ?: exprEnvironment.ctx[node.name]
            ?: throw ExprException("The template variable \"${node.name}\" is not bound to a value. Make sure the variable name is correct. expr location: ${node.location}")
        return param.type.resolve()
    }

    private fun visitProperty(node: ExprNode.Property, ctx: ExprContext): KSType {
        val receiverType = visit(node.receiver, ctx)
        // TODO
//        if (receiver is ClassRef.EnumRef) {
//            val enum = receiver.clazz.enumConstants.first { it.name == node.name }
//            return Value(enum, receiver.clazz.kotlin.createType())
//        }
        return findProperty(node.name, receiverType)
            ?: throw ExprException("The property \"${node.name}\" is not found at ${node.location}")
    }

    private fun findProperty(name: String, receiverType: KSType): KSType? {
        val classDeclaration = receiverType.declaration as? KSClassDeclaration
        val propertyDeclaration = classDeclaration?.getAllProperties()?.firstOrNull { it.simpleName.asString() == name }
        return propertyDeclaration?.type?.resolve()
    }

    private fun visitFunction(node: ExprNode.Function, ctx: ExprContext): KSType {
        val receiverType = visit(node.receiver, ctx)
        val args = visit(node.args, ctx)
        return findFunction(node.name, receiverType, args, ctx)
            ?: throw ExprException("The function \"${node.name}\" is not found at ${node.location}")

//        return if (receiver is ClassRef) {
//            findStaticFunction(node.name, receiverType, args)
//                ?.let { (function, arguments) -> call(function, arguments) }
//                ?: throw ExprException("The static function \"${node.name}\" is not found at ${node.location}")
//        } else {
//            findFunction(node.name, receiverType, args, ctx)
//                ?.let { (function, arguments) ->
//                    if (receiver == null && node.safeCall) {
//                        Value(null, function.returnType.withNullability(false))
//                    } else {
//                        call(function, arguments)
//                    }
//                }
//                ?: throw ExprException("The function \"${node.name}\" is not found at ${node.location}")
//        }
    }

//    private fun findStaticFunction(
//        name: String,
//        receiverType: KType,
//        args: Any?,
//    ): Pair<KFunction<*>, List<Any?>>? {
//        fun Collection<KFunction<*>>.pick(arguments: List<Any?>): Pair<KFunction<*>, List<Any?>>? {
//            return this.filter { function ->
//                if (name == function.name && arguments.size == function.parameters.size) {
//                    arguments.zip(function.parameters).all { (argument, param) ->
//                        argument == null || argument::class.isSubclassOf(param.type.jvmErasure)
//                    }
//                } else {
//                    false
//                }
//            }.map { it to arguments }.firstOrNull()
//        }
//
//        val arguments = when (args) {
//            Unit -> emptyList()
//            is ArgList -> args
//            else -> listOf(args)
//        }
//        return (receiverType.classifier as KClass<*>).staticFunctions.pick(arguments)
//    }

    // TODO
    private fun findFunction(
        name: String,
        receiverType: KSType,
        args: KSType,
        ctx: ExprContext,
    ): KSType? {
        val classDeclaration = receiverType.declaration as? KSClassDeclaration // TODO
        if (classDeclaration == null) {
            return null
        }

        val arguments: List<KSType> = when (args) {
            unitType -> listOf(receiverType)
            else -> {
                if (args.declaration == exprArgListDeclaration) {
                    listOf(receiverType) + args.arguments.map {
                        it.type?.resolve() ?: context.resolver.builtIns.nothingType
                    }
                } else {
                    listOf(receiverType, args)
                }
            }
        }

        val function = classDeclaration.getAllFunctions()
            .filter {
                it.simpleName.asString() == name
            }.filter {
                it.parameters.size == arguments.size
            }.filter {
                it.parameters.zip(arguments).all { (param, arg) ->
                    param.type.resolve() == arg
                }
            }.firstOrNull()
        return function?.returnType?.resolve()
    }

    data class ExprContext(
        val valueMap: Map<String, KSValueParameter>,
        val builtinExtensions: TemplateBuiltinExtensions,
    )
}
