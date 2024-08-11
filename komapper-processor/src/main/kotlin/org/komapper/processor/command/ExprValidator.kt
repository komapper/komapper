package org.komapper.processor.command

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import org.komapper.core.TemplateBuiltinExtensions
import org.komapper.processor.Context
import org.komapper.template.expression.ExprException
import org.komapper.template.expression.ExprLocation
import org.komapper.template.expression.ExprNode
import org.komapper.template.expression.ExprNodeFactory
import org.komapper.template.expression.NoCacheExprNodeFactory
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

internal class ExprValidator(private val context: Context, private val expression: String) {

    private val stringType = context.resolver.builtIns.stringType
    private val booleanType = context.resolver.builtIns.booleanType
    private val unitType = context.resolver.builtIns.unitType
    private val comparableType = context.resolver.getKSNameFromString("kotlin.Comparable").let {
        context.resolver.getClassDeclarationByName(it)?.asStarProjectedType()
            ?: error("Class not found: ${it.asString()}")
    }

    private val exprNodeFactory: ExprNodeFactory = NoCacheExprNodeFactory()

    fun validate(ctx: ExprContext): KSType {
        val node = exprNodeFactory.get(expression)
        return visit(node, ctx)
    }

    private fun visit(node: ExprNode, exprCtx: ExprContext): KSType = when (node) {
        is ExprNode.Not -> perform(node.location, node.operand, exprCtx)
        is ExprNode.And -> perform(node.location, node.left, node.right, exprCtx)
        is ExprNode.Or -> perform(node.location, node.left, node.right, exprCtx)
        is ExprNode.Eq -> equal(node.location, node.left, node.right, exprCtx)
        is ExprNode.Ne -> equal(node.location, node.left, node.right, exprCtx)
        is ExprNode.Ge -> compare(node.location, node.left, node.right, exprCtx)
        is ExprNode.Gt -> compare(node.location, node.left, node.right, exprCtx)
        is ExprNode.Le -> compare(node.location, node.left, node.right, exprCtx)
        is ExprNode.Lt -> compare(node.location, node.left, node.right, exprCtx)
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
            val types = node.nodeList.map { visit(it, exprCtx) }
            KSTypeList(types)
        }

        is ExprNode.ClassRef -> visitClassRef(node, exprCtx)
        is ExprNode.Value -> visitValue(node, exprCtx)
        is ExprNode.Property -> visitProperty(node, exprCtx)
        is ExprNode.Function -> visitFunction(node, exprCtx)
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
        return ctx.paramMap[node.name] // TODO ?: exprEnvironment.ctx[node.name]
            ?: throw ExprException("The template variable \"${node.name}\" is not bound to a value. Make sure the variable name is correct. expr location: ${node.location}")
    }

    private fun visitProperty(node: ExprNode.Property, ctx: ExprContext): KSType {
        val receiverType = visit(node.receiver, ctx)
        return findProperty(node.name, receiverType)
            ?: findExtensionProperty(node.name, receiverType)
            ?: throw ExprException("The property \"${node.name}\" is not found at ${node.location} ${receiverType.declaration}")
    }

    private fun findProperty(name: String, receiverType: KSType): KSType? {
        val classDeclaration = receiverType.declaration as? KSClassDeclaration
            ?: throw ExprException("The receiver type is not a class: ${receiverType.declaration}")
        return if (classDeclaration.classKind == ClassKind.ENUM_CLASS) {
            val entryDeclaration = classDeclaration.declarations.firstOrNull { it.simpleName.asString() == name }
            (entryDeclaration as? KSClassDeclaration)?.asType(emptyList())
        } else {
            classDeclaration.getAllProperties()
                .firstOrNull { it.simpleName.asString() == name }?.type?.resolve()
        }
    }

    private fun findExtensionProperty(name: String, receiverType: KSType): KSType? {
        // TODO
        val extensionClassName = "org.komapper.core.TemplateBuiltinExtensions"
        val extensionsDeclaration = context.resolver.getKSNameFromString(extensionClassName).let {
            context.resolver.getClassDeclarationByName(it)
        } ?: throw ExprException("The extension class \"${extensionClassName}\" is not found.")

        val property = extensionsDeclaration.getAllProperties()
            .filter { it.simpleName.asString() == name }
            .filter { it.extensionReceiver?.resolve()?.isAssignableFrom(receiverType) ?: false }
            .firstOrNull()
        return property?.type?.resolve()
    }

    private fun visitFunction(node: ExprNode.Function, ctx: ExprContext): KSType {
        val receiverType = visit(node.receiver, ctx)
        val args = when (val args = visit(node.args, ctx)) {
            is KSTypeList -> args.ksTypes
            else -> if (args == unitType) {
                emptyList()
            } else {
                listOf(args)
            }
        }
        return findFunction(node.name, receiverType, args, ctx)
            ?: findExtensionFunction(node.name, receiverType, args, ctx)
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
        args: List<KSType>,
        ctx: ExprContext,
    ): KSType? {
        val classDeclaration = receiverType.declaration as? KSClassDeclaration
            ?: throw ExprException("The receiver type is not a class: ${receiverType.declaration}")

        // TODO remove this workaround. See https://github.com/google/ksp/issues/829
        if (classDeclaration.classKind == ClassKind.ENUM_CLASS) {
            if (name == "valueOf" && args.size == 1 && args[0] == stringType) {
                return receiverType
            }
            if (name == "values" && args.isEmpty()) {
                val typeRef = context.resolver.createKSTypeReferenceFromKSType(receiverType)
                val typeArg = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
                return context.resolver.getKSNameFromString("kotlin.Array").let {
                    context.resolver.getClassDeclarationByName(it)?.asType(listOf(typeArg))
                        ?: error("Class not found: ${it.asString()}")
                }
            }
        }

        val function = classDeclaration.getAllFunctions()
            .onEach {
                // TODO
                // println("function: $it")
            }
            .filter {
                it.simpleName.asString() == name
            }.filter {
                it.parameters.size == args.size
            }.filter {
                it.parameters.zip(args).all { (param, arg) ->
                    param.type.resolve().isAssignableFrom(arg)
                }
            }.firstOrNull()
        return function?.returnType?.resolve()
    }

    private fun findExtensionFunction(
        name: String,
        receiverType: KSType,
        args: List<KSType>,
        ctx: ExprContext,
    ): KSType? {
        // TODO
        val extensionClassName = "org.komapper.core.TemplateBuiltinExtensions"
        val extensionsDeclaration = context.resolver.getKSNameFromString(extensionClassName).let {
            context.resolver.getClassDeclarationByName(it)
        } ?: throw ExprException("The extension class \"${extensionClassName}\" is not found.")

        val function = extensionsDeclaration.getAllFunctions()
            .onEach {
                // println("debug (function): ${it.simpleName} ${it.parameters}")
            }
            .filter { it.simpleName.asString() == name }
            .filter { it.extensionReceiver?.resolve()?.isAssignableFrom(receiverType) ?: false }
            .filter { it.parameters.size == args.size }
            .filter {
                it.parameters.zip(args).all { (param, arg) ->
                    param.type.resolve().isAssignableFrom(arg)
                }
            }.firstOrNull()
        return function?.returnType?.resolve()
    }

    data class ExprContext(
        val paramMap: Map<String, KSType>,
        val builtinExtensions: TemplateBuiltinExtensions,
    )

    data class KSTypeList(
        val ksTypes: List<KSType>,
    ) : KSType {
        override val annotations: Sequence<KSAnnotation>
            get() = throw UnsupportedOperationException()
        override val arguments: List<KSTypeArgument>
            get() = throw UnsupportedOperationException()
        override val declaration: KSDeclaration
            get() = throw UnsupportedOperationException()
        override val isError: Boolean
            get() = throw UnsupportedOperationException()
        override val isFunctionType: Boolean
            get() = throw UnsupportedOperationException()
        override val isMarkedNullable: Boolean
            get() = throw UnsupportedOperationException()
        override val isSuspendFunctionType: Boolean
            get() = throw UnsupportedOperationException()
        override val nullability: Nullability
            get() = throw UnsupportedOperationException()

        override fun isAssignableFrom(that: KSType): Boolean {
            throw UnsupportedOperationException()
        }

        override fun isCovarianceFlexible(): Boolean {
            throw UnsupportedOperationException()
        }

        override fun isMutabilityFlexible(): Boolean {
            throw UnsupportedOperationException()
        }

        override fun makeNotNullable(): KSType {
            throw UnsupportedOperationException()
        }

        override fun makeNullable(): KSType {
            throw UnsupportedOperationException()
        }

        override fun replace(arguments: List<KSTypeArgument>): KSType {
            throw UnsupportedOperationException()
        }

        override fun starProjection(): KSType {
            throw UnsupportedOperationException()
        }
    }
}
