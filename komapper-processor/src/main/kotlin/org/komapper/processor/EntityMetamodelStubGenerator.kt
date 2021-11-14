package org.komapper.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import org.komapper.processor.ClassNames.EntityMetamodelImplementor
import org.komapper.processor.ClassNames.EntityMetamodelStub
import org.komapper.processor.ClassNames.PropertyMetamodel
import org.komapper.processor.ClassNames.PropertyMetamodelStub
import java.io.PrintWriter
import java.time.ZonedDateTime

internal class EntityMetamodelStubGenerator(
    private val defDeclaration: KSClassDeclaration,
    private val declaration: KSClassDeclaration,
    private val packageName: String,
    private val entityTypeName: String,
    private val simpleName: String,
    private val w: PrintWriter
) : Runnable {
    private val constructorParamList = listOf(
        "table: String = \"\"",
        "catalog: String = \"\"",
        "schema: String = \"\"",
        "alwaysQuote: Boolean = false"
    ).joinToString(", ")

    override fun run() {
        if (packageName.isNotEmpty()) {
            w.println("package $packageName")
            w.println()
        }
        w.println("// generated at ${ZonedDateTime.now()}")
        w.println("@$EntityMetamodelImplementor")
        w.println("class $simpleName : $EntityMetamodelStub<$entityTypeName, $simpleName>() {")
        val parameters = declaration.primaryConstructor?.parameters
        if (parameters != null) {
            for (p in parameters) {
                val typeName = p.type.resolve().declaration.qualifiedName?.asString()
                w.println("    val $p: $PropertyMetamodel<$entityTypeName, $typeName, $typeName> = $PropertyMetamodelStub<$entityTypeName, $typeName>()")
            }
        }
        w.println("    companion object {")
        w.println("        val meta = $simpleName()")
        w.println("        fun newMeta($constructorParamList) = $simpleName()")
        w.println("    }")
        w.println("}")
        val companionObject = defDeclaration.getCompanionObject()
        if (companionObject != null) {
            val companionObjectName = (companionObject.qualifiedName ?: companionObject.simpleName).asString()
            w.println("")
            w.println("val $companionObjectName.meta get() = $simpleName.meta")
            w.println("fun $companionObjectName.newMeta($constructorParamList) = $simpleName.newMeta(table, catalog, schema, alwaysQuote)")
        }
    }
}
