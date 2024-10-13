package org.komapper.processor.command

import com.google.devtools.ksp.getDeclaredFunctions
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
import org.komapper.processor.getClassDeclaration
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

internal class ExprValidator(
    private val context: Context,
    private val exprNodeFactory: ExprNodeFactory = NoCacheExprNodeFactory(),
) {
    private val stringType = context.resolver.builtIns.stringType
    private val booleanType = context.resolver.builtIns.booleanType
    private val unitType = context.resolver.builtIns.unitType

    private val comparableType by lazy {
        context.getClassDeclaration(Comparable::class) {
            throw ExprException("Class not found: $it")
        }.asStarProjectedType()
    }

    private val templateExtensionsDeclaration by lazy {
        context.getClassDeclaration(context.config.templateExtensions) { throw ExprException("Class not found: $it") }
    }

    private val arrayDeclaration by lazy {
        context.getClassDeclaration(Array::class) { throw ExprException("Class not found: $it") }
    }

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
        is ExprNode.Is -> visitIs(node.location, node.left, node.right, paramMap)
        is ExprNode.As -> visitAs(node.location, node.left, node.right, paramMap)
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
        is ExprNode.CallableValue -> visitCallableValue(node, paramMap)
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
            throw NonBooleanTypeException(
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
            throw EitherOperandNonBooleanException(
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
        val left = visit(leftNode, paramMap)
        val right = visit(rightNode, paramMap)
        if (left != right) {
            throw NonSameTypeException(
                "Cannot compare because the operands(left=$left, right=$right) are not the same type at $location",
            )
        }
        if (!comparableType.isAssignableFrom(left)) {
            throw NonComparableTypeException(
                "Cannot compare because operands(left=$left, right=$right) are not Comparable type at $location",
            )
        }
        return booleanType
    }

    private fun visitIs(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        paramMap: Map<String, KSType>,
    ): KSType {
        @Suppress("UNUSED_VARIABLE")
        val left = visit(leftNode, paramMap)
        rightNode as? ExprNode.ClassRef
            ?: throw NotClassRefNodeException("The right operand of the \"is\" operator must be a class reference at $location")
        // validate the rightNode
        visitClassRef(rightNode, paramMap)
        return booleanType
    }

    private fun visitAs(
        location: ExprLocation,
        leftNode: ExprNode,
        rightNode: ExprNode,
        paramMap: Map<String, KSType>,
    ): KSType {
        @Suppress("UNUSED_VARIABLE")
        val left = visit(leftNode, paramMap)
        rightNode as? ExprNode.ClassRef
            ?: throw NotClassRefNodeException("The right operand of the \"as\" operator must be a class reference at $location")
        return visitClassRef(rightNode, paramMap)
    }

    private fun visitClassRef(
        node: ExprNode.ClassRef,
        @Suppress("UNUSED_PARAMETER") paramMap: Map<String, KSType>
    ): KSType {
        // convert the binary class name to the kotlin class name
        val name = node.name.replace("$", ".")
        val classDeclaration = context.getClassDeclaration(name) {
            throw ClassNotFoundException("The class \"${node.name}\" is not found at ${node.location}")
        }
        val companionObject = classDeclaration.declarations
            .mapNotNull { it as? KSClassDeclaration }
            .filter { it.isCompanionObject }
            .firstOrNull()
        return companionObject?.asStarProjectedType()
            ?: classDeclaration.asStarProjectedType()
    }

    private fun visitValue(node: ExprNode.Value, paramMap: Map<String, KSType>): KSType {
        return getParamType(node.name, node.location, paramMap)
    }

    private fun visitCallableValue(node: ExprNode.CallableValue, paramMap: Map<String, KSType>): KSType {
        val type = getParamType(node.name, node.location, paramMap)
        // kotlin.Function
        val classDeclaration = type.declaration as? KSClassDeclaration
            ?: throw ExprException("The variable \"${node.name}\" is not a class at ${node.location}.")
        val functionDeclaration = classDeclaration.getDeclaredFunctions()
            .filter {
                it.simpleName.asString() == "invoke"
            }.firstOrNull()
            ?: throw InvokeFunctionNotFoundException("The variable \"${node.name}\" does not have a invoke function at ${node.location}.")
        val argList = when (val args = visit(node.args, paramMap)) {
            is KSTypeList -> args.argList
            else -> if (args == unitType) {
                emptyList()
            } else {
                listOf(args)
            }
        }
        if (functionDeclaration.parameters.size != argList.size) {
            throw ArgumentCountMismatchException("The number of arguments is not matched at ${node.location}")
        }
        return type.arguments.firstOrNull()?.type?.resolve()
            ?: throw ReturnTypeNotFoundException("The return type is not found at ${node.location}")
    }

    private fun getParamType(name: String, location: ExprLocation, paramMap: Map<String, KSType>): KSType {
        referencedParams.add(name)
        return paramMap[name]
            ?: throw ParameterNotFoundException(
                "The parameter \"${name}\" is not found at $location. Available parameters are: ${paramMap.keys}."
            )
    }

    private fun visitProperty(node: ExprNode.Property, paramMap: Map<String, KSType>): KSType {
        val receiver = visit(node.receiver, paramMap)
        return findProperty(node.name, receiver)
            ?: findExtensionProperty(node.name, receiver)
            ?: throw PropertyNotFoundException("The property \"${node.name}\" is not found at ${node.location}")
    }

    private fun findProperty(name: String, receiver: KSType): KSType? {
        val receiverDeclaration = receiver.declaration as? KSClassDeclaration
            ?: throw ExprException("The receiver type is not a class: ${receiver.declaration}")
        val propertyType = receiverDeclaration.getAllProperties()
            .firstOrNull { it.simpleName.asString() == name }?.type?.resolve()
        return propertyType
            ?: if (receiverDeclaration.classKind == ClassKind.ENUM_CLASS) {
                receiverDeclaration.declarations
                    .filterIsInstance<KSClassDeclaration>()
                    .filter { it.classKind == ClassKind.ENUM_ENTRY }
                    .firstOrNull { it.simpleName.asString() == name }
                    ?.asType(emptyList())
            } else {
                null
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
            ?: throw FunctionNotFoundException(
                "The function \"${node.name}\" (parameter size is ${argList.size}) is not found at ${node.location}"
            )
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

    class EitherOperandNonBooleanException(message: String) : ExprException(message)
    class NonBooleanTypeException(message: String) : ExprException(message)
    class NonSameTypeException(message: String) : ExprException(message)
    class NonComparableTypeException(message: String) : ExprException(message)
    class InvokeFunctionNotFoundException(message: String) : ExprException(message)
    class ArgumentCountMismatchException(message: String) : ExprException(message)
    class ReturnTypeNotFoundException(message: String) : ExprException(message)
    class FunctionNotFoundException(message: String) : ExprException(message)
    class PropertyNotFoundException(message: String) : ExprException(message)
    class ParameterNotFoundException(message: String) : ExprException(message)
    class ClassNotFoundException(message: String) : ExprException(message)
    class NotClassRefNodeException(message: String) : ExprException(message)
}
