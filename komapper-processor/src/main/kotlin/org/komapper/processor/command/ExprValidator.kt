package org.komapper.processor.command

import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.symbol.Variance
import org.komapper.core.template.expression.ExprException
import org.komapper.core.template.expression.ExprLocation
import org.komapper.core.template.expression.ExprNode
import org.komapper.core.template.expression.ExprNodeFactory
import org.komapper.core.template.expression.NoCacheExprNodeFactory
import org.komapper.processor.Context
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

internal class ExprValidator(private val context: Context) {

    private val stringType = context.resolver.builtIns.stringType
    private val booleanType = context.resolver.builtIns.booleanType
    private val unitType = context.resolver.builtIns.unitType

    private val comparableType by lazy {
        context.resolver.getKSNameFromString(Comparable::class.qualifiedName!!).let {
            context.resolver.getClassDeclarationByName(it)?.asStarProjectedType()
                ?: throw ExprException("Class not found: ${it.asString()}")
        }
    }

    private val templateExtensionsDeclaration by lazy {
        val name = context.config.templateExtensions
        context.resolver.getKSNameFromString(name).let {
            context.resolver.getClassDeclarationByName(it)
                ?: throw ExprException("Class not found: ${it.asString()}")
        }
    }

    private val arrayDeclaration by lazy {
        context.resolver.getKSNameFromString(Array::class.qualifiedName!!).let {
            context.resolver.getClassDeclarationByName(it)
                ?: throw ExprException("Class not found: ${it.asString()}")
        }
    }

    private val exprNodeFactory: ExprNodeFactory = NoCacheExprNodeFactory()

    private val referencedParams: MutableSet<String> = mutableSetOf()

    val usedParams: Set<String> get() = referencedParams

    fun validate(expression: String, paramMap: Map<String, KSType>): ExprEvalResult {
        val node = exprNodeFactory.get(expression)
        val type = visit(node, paramMap)
        return ExprEvalResult(node, type)
    }

    private fun visit(node: ExprNode, paramMap: Map<String, KSType>): KSType = when (node) {
        is ExprNode.Not -> perform(node.location, node.operand, paramMap)
        is ExprNode.And -> perform(node.location, node.left, node.right, paramMap)
        is ExprNode.Or -> perform(node.location, node.left, node.right, paramMap)
        is ExprNode.Eq -> equal(node.location, node.left, node.right, paramMap)
        is ExprNode.Ne -> equal(node.location, node.left, node.right, paramMap)
        is ExprNode.Ge -> compare(node.location, node.left, node.right, paramMap)
        is ExprNode.Gt -> compare(node.location, node.left, node.right, paramMap)
        is ExprNode.Le -> compare(node.location, node.left, node.right, paramMap)
        is ExprNode.Lt -> compare(node.location, node.left, node.right, paramMap)
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
            val types = node.nodeList.map { visit(it, paramMap) }
            KSTypeList(types)
        }

        is ExprNode.ClassRef -> visitClassRef(node, paramMap)
        is ExprNode.Value -> visitValue(node, paramMap)
        is ExprNode.Property -> visitProperty(node, paramMap)
        is ExprNode.Function -> visitFunction(node, paramMap)
        is ExprNode.Empty -> context.resolver.builtIns.unitType
    }

    private fun perform(
        location: ExprLocation,
        operand: ExprNode,
        paramMap: Map<String, KSType>,
    ): KSType {
        val result = visit(operand, paramMap)
        if (result != booleanType) {
            throw ExprException(
                "Cannot perform the logical operator because the operand is not Boolean at $location",
            )
        }
        return booleanType
    }

    private fun perform(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        paramMap: Map<String, KSType>,
    ): KSType {
        val left = visit(leftNode, paramMap)
        val right = visit(rightNode, paramMap)
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
        paramMap: Map<String, KSType>,
    ): KSType {
        visit(leftNode, paramMap)
        visit(rightNode, paramMap)
        return booleanType
    }

    private fun compare(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        paramMap: Map<String, KSType>,
    ): KSType {
        val left = visit(leftNode, paramMap).makeNullable()
        val right = visit(rightNode, paramMap).makeNullable()
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

    private fun visitClassRef(node: ExprNode.ClassRef, @Suppress("UNUSED_PARAMETER") paramMap: Map<String, KSType>): KSType {
        val classDeclaration = context.resolver.getKSNameFromString(node.name).let {
            context.resolver.getClassDeclarationByName(it)
        } ?: throw ExprException("The class \"${node.name}\" is not found at ${node.location}")
        val companionObject = classDeclaration.declarations
            .mapNotNull { it as? KSClassDeclaration }
            .filter { it.isCompanionObject }
            .firstOrNull()
        return companionObject?.asStarProjectedType()
            ?: classDeclaration.asStarProjectedType()
    }

    private fun visitValue(node: ExprNode.Value, paramMap: Map<String, KSType>): KSType {
        referencedParams.add(node.name)
        return paramMap[node.name]
            ?: throw ExprException("The variable \"${node.name}\" is not found at ${node.location}. Available variables are: ${paramMap.keys}.")
    }

    private fun visitProperty(node: ExprNode.Property, paramMap: Map<String, KSType>): KSType {
        val receiver = visit(node.receiver, paramMap)
        return findProperty(node.name, receiver)
            ?: findExtensionProperty(node.name, receiver)
            ?: throw ExprException("The property \"${node.name}\" is not found at ${node.location}")
    }

    private fun findProperty(name: String, receiver: KSType): KSType? {
        val receiverDeclaration = receiver.declaration as? KSClassDeclaration
            ?: throw ExprException("The receiver type is not a class: ${receiver.declaration}")
        return if (receiverDeclaration.classKind == ClassKind.ENUM_CLASS) {
            val entryDeclaration = receiverDeclaration.declarations.firstOrNull { it.simpleName.asString() == name }
            (entryDeclaration as? KSClassDeclaration)?.asType(emptyList())
        } else {
            receiverDeclaration.getAllProperties()
                .firstOrNull { it.simpleName.asString() == name }?.type?.resolve()
        }
    }

    private fun findExtensionProperty(name: String, receiver: KSType): KSType? {
        return templateExtensionsDeclaration.getAllProperties()
            .filter { it.simpleName.asString() == name }
            .filter { it.extensionReceiver?.resolve()?.isAssignableFrom(receiver) ?: false }
            .firstOrNull()?.type?.resolve()
    }

    private fun visitFunction(node: ExprNode.Function, paramMap: Map<String, KSType>): KSType {
        val receiver = visit(node.receiver, paramMap)
        val argList = when (val args = visit(node.args, paramMap)) {
            is KSTypeList -> args.argList
            else -> if (args == unitType) {
                emptyList()
            } else {
                listOf(args)
            }
        }
        return findFunction(node.name, receiver, argList)
            ?: findExtensionFunction(node.name, receiver, argList)
            ?: throw ExprException("The function \"${node.name}\" is not found at ${node.location}")
    }

    private fun findFunction(
        name: String,
        receiver: KSType,
        argList: List<KSType>,
    ): KSType? {
        val receiverDeclaration = receiver.declaration as? KSClassDeclaration
            ?: throw ExprException("The receiver type is not a class: ${receiver.declaration}")

        // TODO remove this workaround in the future. See https://github.com/google/ksp/issues/829
        if (receiverDeclaration.classKind == ClassKind.ENUM_CLASS) {
            if (name == "valueOf" && argList.size == 1 && argList[0] == stringType) {
                return receiver
            }
            if (name == "values" && argList.isEmpty()) {
                val typeRef = context.resolver.createKSTypeReferenceFromKSType(receiver)
                val typeArg = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
                return arrayDeclaration.asType(listOf(typeArg))
            }
        }

        return receiverDeclaration.getAllFunctions()
            .filter {
                it.simpleName.asString() == name
            }.filter {
                it.parameters.size == argList.size
            }.filter {
                it.parameters.zip(argList).all { (param, arg) ->
                    param.type.resolve().isAssignableFrom(arg)
                }
            }.firstOrNull()?.returnType?.resolve()
    }

    private fun findExtensionFunction(
        name: String,
        receiver: KSType,
        argList: List<KSType>,
    ): KSType? {
        return templateExtensionsDeclaration.getAllFunctions()
            .filter { it.simpleName.asString() == name }
            .filter { it.extensionReceiver?.resolve()?.isAssignableFrom(receiver) ?: false }
            .filter { it.parameters.size == argList.size }
            .filter {
                it.parameters.zip(argList).all { (param, arg) ->
                    param.type.resolve().isAssignableFrom(arg)
                }
            }.firstOrNull()?.returnType?.resolve()
    }

    data class KSTypeList(
        val argList: List<KSType>,
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