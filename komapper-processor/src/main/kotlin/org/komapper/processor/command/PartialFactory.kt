package org.komapper.processor.command

import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Modifier
import org.komapper.annotation.KomapperUnused
import org.komapper.processor.Context
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue
import org.komapper.processor.hasAnnotation
import org.komapper.processor.report
import kotlin.reflect.KClass

internal class PartialFactory(
    private val context: Context,
    private val annotationClass: KClass<*>,
    private val symbol: KSAnnotated,
) {
    fun create(): Partial {
        val classDeclaration = symbol as? KSClassDeclaration
            ?: report("The annotated element is not a class.", symbol)
        validatePartialClass(classDeclaration, symbol)
        val properties = classDeclaration.getAllProperties()
            .filterNot { Modifier.PRIVATE in it.modifiers }
            .toList()
        val paramMap = properties.associate { it.simpleName.asString() to it.type.resolve() }
        val unusedParams = properties.filter { it.hasAnnotation(KomapperUnused::class) }.map { it.simpleName.asString() }.toSet()
        val annotation = classDeclaration.findAnnotation(annotationClass)
            ?: report("The annotation \"${annotationClass.simpleName}\" is not found.", symbol)
        val sql = annotation.findValue("sql")?.toString()?.trimIndent()
            ?: report("The annotation value \"sql\" is not found.", annotation)
        val disableValidation = annotation.findValue("disableValidation")?.toString()?.toBooleanStrict()
            ?: report("The annotation value \"disableValidation\" is not found.", annotation)
        return Partial(
            sql = sql,
            disableValidation = disableValidation,
            annotation = annotation,
            classDeclaration = classDeclaration,
            paramMap = paramMap,
            unusedParams = unusedParams,
        )
    }

    private fun validatePartialClass(classDeclaration: KSClassDeclaration, recipient: KSNode) {
        if (classDeclaration.typeParameters.isNotEmpty()) {
            report("The class \"${classDeclaration.simpleName.asString()}\" must not have type parameters.", recipient)
        }
        if (classDeclaration.isPrivate()) {
            report("The class \"${classDeclaration.simpleName.asString()}\" must not be private.", recipient)
        }
        validateEnclosingDeclaration(classDeclaration, classDeclaration.parentDeclaration, recipient)
    }

    private fun validateEnclosingDeclaration(enclosed: KSDeclaration, enclosing: KSDeclaration?, recipient: KSNode) {
        if (enclosing == null) return
        if (enclosing.isPrivate()) {
            val enclosingName = enclosing.simpleName.asString()
            val enclosedName = enclosed.simpleName.asString()
            report("The enclosing declaration \"$enclosingName\" of the class \"$enclosedName\" must not be private.", recipient)
        }
        validateEnclosingDeclaration(enclosed, enclosing.parentDeclaration, recipient)
    }
}
