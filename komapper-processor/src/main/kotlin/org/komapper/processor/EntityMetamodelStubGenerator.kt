package org.komapper.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.komapper.processor.Symbols.EntityMetamodelImplementor
import org.komapper.processor.Symbols.EntityMetamodelStub
import org.komapper.processor.Symbols.PropertyMetamodel
import org.komapper.processor.Symbols.PropertyMetamodelStub
import java.io.PrintWriter
import java.time.ZonedDateTime

internal class EntityMetamodelStubGenerator(
    @Suppress("unused")
    private val logger: KSPLogger,
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
        w.println("@file:Suppress(\"ClassName\", \"PrivatePropertyName\", \"UNUSED_PARAMETER\", \"unused\", \"RemoveRedundantQualifierName\", \"MemberVisibilityCanBePrivate\", \"RedundantNullableReturnType\", \"RemoveRedundantBackticks\")")

        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@$EntityMetamodelImplementor($entityTypeName::class)")
        w.println("class $simpleName : $EntityMetamodelStub<$entityTypeName, $simpleName>() {")
        val parameters = declaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                val typeName = p.type.resolve().declaration.qualifiedName?.asString()
                w.println("    val `$p`: $PropertyMetamodel<$entityTypeName, $typeName, $typeName> = $PropertyMetamodelStub<$entityTypeName, $typeName>()")
            }
        }
        w.println("    fun clone($constructorParamList) = $simpleName()")
        w.println("    companion object {")
        for (alias in aliases) {
            w.println("        val `$alias` = $simpleName()")
        }
        w.println("    }")
        w.println("}")
        w.println("")
        for (alias in aliases) {
            w.println("val $unitTypeName.`$alias` get() = $simpleName.`$alias`")
        }
    }
}
