package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Variance
import org.komapper.annotation.KomapperCommand
import org.komapper.core.Execute
import org.komapper.core.Many
import org.komapper.core.One
import org.komapper.processor.ClassDeclarationVisitor
import org.komapper.processor.Context
import org.komapper.processor.findAnnotation
import org.komapper.processor.findValue

internal class CommandFactory(
    private val context: Context,
    private val symbol: KSAnnotated,
) {
    fun create(): Command {
        val classDeclaration = symbol.accept(ClassDeclarationVisitor(), Unit) ?: TODO()
        val parameters = classDeclaration.primaryConstructor?.parameters ?: TODO()
        val annotation = classDeclaration.findAnnotation(KomapperCommand::class) ?: TODO()
        val sql = annotation.findValue("sql")?.toString() ?: TODO()
        val result = createCommandResult(classDeclaration)
        // TODO
        val (packageName, fileName) = createFileName(context, classDeclaration)
        return Command(sql, classDeclaration, parameters, result, packageName, fileName)
    }

    private fun createCommandResult(classDeclaration: KSClassDeclaration): CommandResult {
        val superTypes = classDeclaration.superTypes.map { it.resolve() }.toList()
        for (type in superTypes) {
            val name = type.declaration.qualifiedName?.asString()
            when (name) {
                One::class.qualifiedName!! -> {
                    val returnType = context.resolver.getKSNameFromString("org.komapper.core.dsl.query.Query").let {
                        context.resolver.getClassDeclarationByName(it)?.asType(type.arguments) ?: error("${it.asString()}")
                    }
                    return CommandResult(CommandKind.One, returnType)
                }
                Many::class.qualifiedName!! -> {
                    val returnType = context.resolver.getKSNameFromString("org.komapper.core.dsl.query.ListQuery").let {
                        context.resolver.getClassDeclarationByName(it)?.asType(type.arguments) ?: error("${it.asString()}")
                    }
                    return CommandResult(CommandKind.Many, returnType)
                }
                Execute::class.qualifiedName!! -> {
                    val returnType = context.resolver.getKSNameFromString("org.komapper.core.dsl.query.Query").let {
                        val longType = context.resolver.builtIns.longType
                        val typeRef = context.resolver.createKSTypeReferenceFromKSType(longType)
                        val argument = context.resolver.getTypeArgument(typeRef, Variance.INVARIANT)
                        context.resolver.getClassDeclarationByName(it)?.asType(listOf(argument)) ?: error("${it.asString()}")
                    }
                    return CommandResult(CommandKind.Execute, returnType)
                }
                else -> Unit
            }
        }
        error("Unknown command type: ${classDeclaration.qualifiedName?.asString()}, $superTypes ")
    }

    private fun createFileName(context: Context, classDeclaration: KSClassDeclaration): Pair<String, String> {
        val packageName = classDeclaration.packageName.asString()
        val qualifiedName = classDeclaration.qualifiedName?.asString() ?: ""
        val packageRemovedQualifiedName = qualifiedName.removePrefix("$packageName.")
        val simpleName = context.config.prefix + packageRemovedQualifiedName.replace(".", "_") + context.config.suffix
        return packageName to simpleName
    }
}
