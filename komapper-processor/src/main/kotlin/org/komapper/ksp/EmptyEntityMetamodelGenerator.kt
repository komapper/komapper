package org.komapper.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import java.io.PrintWriter

internal class EmptyEntityMetamodelGenerator(
    private val classDeclaration: KSClassDeclaration,
    private val packageName: String,
    private val simpleQualifiedName: String,
    private val fileName: String,
    private val w: PrintWriter
) : Runnable {
    override fun run() {
        w.println("package $packageName")
        w.println()
        w.println("import org.komapper.core.metamodel.*")
        w.println()
        w.println("@Suppress(\"ClassName\")")
        w.println("class $fileName : EmptyEntityMetamodel<$simpleQualifiedName>() {")
        val parameters = classDeclaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                w.println("    val $p = EmptyPropertyMetamodel<$simpleQualifiedName, ${p.type.resolve().declaration.qualifiedName?.asString()}>()")
            }
        }
        w.println("}")
        if (classDeclaration.hasCompanionObject()) {
            w.println("")
            w.println("fun $simpleQualifiedName.Companion.metamodel() = $fileName()")
        }
    }
}
