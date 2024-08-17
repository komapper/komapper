package org.komapper.processor.entity

import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.komapper.processor.BackquotedSymbols.EntityMetamodelImplementor
import org.komapper.processor.BackquotedSymbols.EntityMetamodelStub
import org.komapper.processor.BackquotedSymbols.PropertyMetamodel
import org.komapper.processor.BackquotedSymbols.PropertyMetamodelStub
import org.komapper.processor.Context
import org.komapper.processor.Symbols
import java.io.PrintWriter
import java.time.ZonedDateTime

internal class EntityMetamodelStubGenerator(
    @Suppress("unused")
    private val context: Context,
    private val declaration: KSClassDeclaration,
    private val unitTypeName: String,
    private val aliases: List<String>,
    private val packageName: String,
    private val simpleName: String,
    private val entityTypeName: String,
    private val w: PrintWriter,
) : Runnable {

    private val constructorParamList = listOf(
        "table: String = \"\"",
        "catalog: String = \"\"",
        "schema: String = \"\"",
        "alwaysQuote: Boolean = false",
        "disableSequenceAssignment: Boolean = false",
        "declarations: List<${Symbols.EntityMetamodelDeclaration}<$simpleName>> = emptyList()",
    ).joinToString(", ")

    override fun run() {
        w.println("@file:Suppress(\"warnings\")")

        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@$EntityMetamodelImplementor($entityTypeName::class)")
        w.println("public class $simpleName : $EntityMetamodelStub<$entityTypeName, $simpleName>() {")
        val parameters = declaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                val typeName = p.type.resolve().declaration.qualifiedName?.asString()
                w.println("    public val `$p`: $PropertyMetamodel<$entityTypeName, $typeName, $typeName> = $PropertyMetamodelStub<$entityTypeName, $typeName>()")
            }
        }
        w.println("    public fun clone($constructorParamList): $simpleName = $simpleName()")
        w.println("    public companion object {")
        for (alias in aliases) {
            w.println("        public val `$alias` = $simpleName()")
        }
        w.println("    }")
        w.println("}")
        w.println("")
        for (alias in aliases) {
            w.println("val $unitTypeName.`$alias`: $simpleName get() = $simpleName.`$alias`")
        }
    }
}
