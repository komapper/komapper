package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.visitor.KSEmptyVisitor

internal fun <T> Sequence<T>.anyDuplicates(predicate: (T) -> Boolean): Boolean {
    return this.filter(predicate).take(2).count() == 2
}

internal fun KSClassDeclaration.getCompanionObject(): KSClassDeclaration? {
    return declarations.firstNotNullOfOrNull { declaration ->
        declaration.accept(
            object : KSEmptyVisitor<Unit, KSClassDeclaration?>() {
                override fun defaultHandler(node: KSNode, data: Unit): KSClassDeclaration? {
                    return null
                }

                override fun visitClassDeclaration(
                    classDeclaration: KSClassDeclaration,
                    data: Unit
                ): KSClassDeclaration? {
                    return if (classDeclaration.isCompanionObject) classDeclaration else null
                }
            },
            Unit
        )
    }
}

internal fun KSClassDeclaration.isValueClass(): Boolean {
    return this.findAnnotation("JvmInline") != null
}

internal fun KSAnnotation.findValue(name: String): Any? {
    return this.arguments
        .filter { it.name?.asString() == name }
        .map { it.value }
        .firstOrNull()
}

internal fun KSAnnotated.findAnnotation(shortName: String): KSAnnotation? {
    return this.annotations.firstOrNull { it.shortName.asString() == shortName }
}
