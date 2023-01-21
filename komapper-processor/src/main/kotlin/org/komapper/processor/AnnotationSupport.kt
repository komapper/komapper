package org.komapper.processor

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import org.komapper.annotation.KomapperAlternate
import org.komapper.annotation.KomapperAlternateOverride
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperEnumOverride
import org.komapper.annotation.KomapperTable
import org.komapper.core.NamingStrategy

internal class AnnotationSupport(
    @Suppress("unused") private val config: Config,
) {

    private val namingStrategy: NamingStrategy = config.namingStrategy

    fun getTable(definitionSource: EntityDefinitionSource): Table {
        val annotation = definitionSource.defDeclaration.findAnnotation(KomapperTable::class)
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(definitionSource.entityDeclaration.simpleName.asString())
        val catalog = annotation?.findValue("catalog")?.toString()?.trim() ?: KomapperTable.CATALOG
        val schema = annotation?.findValue("schema")?.toString()?.trim() ?: KomapperTable.SCHEMA
        val alwaysQuote =
            annotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: config.alwaysQuote
        return Table(name, catalog, schema, alwaysQuote)
    }

    fun getColumn(parameter: KSValueParameter): Column {
        val annotation = parameter.findAnnotation(KomapperColumn::class)
        return getColumn(annotation, parameter.toString())
    }

    fun getColumn(columnAnnotation: KSAnnotation?, propertyName: String): Column {
        val name = columnAnnotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(propertyName)
        val alwaysQuote =
            columnAnnotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: config.alwaysQuote
        val masking =
            columnAnnotation?.findValue("masking")?.toString()?.toBooleanStrict() ?: KomapperColumn.MASKING
        return Column(name, alwaysQuote, masking)
    }

    fun getColumns(parameter: KSValueParameter): List<Triple<String, Column, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperColumnOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val columnNode = it.findValue("column") as? KSNode
                val columnAnnotation = columnNode?.accept(AnnotationVisitor(), Unit)
                Triple(name, columnAnnotation, it)
            }.filter {
                it.first != null && it.second != null
            }.map {
                val column = getColumn(it.second!!, it.first!!)
                Triple(it.first!!, column, it.third)
            }.toList()
    }

    fun getEnumStrategy(parameter: KSValueParameter): EnumStrategy {
        val annotation = parameter.findAnnotation(KomapperEnum::class)
        return getEnumStrategy(annotation)
    }

    private fun getEnumStrategy(annotation: KSAnnotation?): EnumStrategy {
        return when (annotation?.findValue("type")?.toString()) {
            Symbols.EnumType_NAME -> EnumStrategy.Name
            Symbols.EnumType_ORDINAL -> EnumStrategy.Ordinal
            Symbols.EnumType_PROPERTY -> {
                val hint = annotation.findValue("hint")?.toString()
                    ?: report("The hint property is missing", annotation)
                EnumStrategy.Property(hint, annotation)
            }
            else -> config.enumStrategy
        }
    }

    fun getEnumStrategies(parameter: KSValueParameter): List<Triple<String, EnumStrategy, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperEnumOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val enumNode = it.findValue("enum") as? KSNode
                val enumAnnotation = enumNode?.accept(AnnotationVisitor(), Unit)
                Triple(name, enumAnnotation, it)
            }.filter {
                it.first != null && it.second != null
            }.map {
                val enumStrategy = getEnumStrategy(it.second)
                Triple(it.first!!, enumStrategy, it.third)
            }.toList()
    }

    fun getAlternate(parameter: KSValueParameter): ValueClass? {
        val annotation = parameter.findAnnotation(KomapperAlternate::class)
        return getAlternate(annotation)
    }

    private fun getAlternate(annotation: KSAnnotation?): ValueClass? {
        if (annotation == null) return null
        val type = annotation.findValue("valueClass")
        if (type !is KSType) report("The valueClass is not KSType.", annotation)
        val classDeclaration = type.declaration.accept(ClassDeclarationVisitor(), Unit)
        if (classDeclaration == null || !classDeclaration.isValueClass()) {
            report("The valueClass property must be a value class.", annotation)
        }
        val constructor = classDeclaration.primaryConstructor
        val isPublic = constructor?.isPublic() ?: false
        if (!isPublic) report("The constructor of \"${classDeclaration.qualifiedName?.asString()}\" must be public.", annotation)
        val parameter = constructor?.parameters?.firstOrNull()
            ?: error("No parameter is found in the class \"${classDeclaration.qualifiedName?.asString()}\"")
        val declaration = classDeclaration.getDeclaredProperties().firstOrNull()
            ?: error("No property is found in the class \"${classDeclaration.qualifiedName?.asString()}\"")
        if (!declaration.isPublic()) report("The property parameter of \"${classDeclaration.qualifiedName?.asString()}\" must be public.", annotation)
        val propertyType = declaration.type.resolve()
        if (propertyType.isMarkedNullable) report("The property parameter of \"${classDeclaration.qualifiedName?.asString()}\" must not be nullable.", annotation)
        val typeName = propertyType.name
        val literalTag = resolveLiteralTag(typeName)
        val nullability = propertyType.nullability
        val property = ValueClassProperty(propertyType, parameter, declaration, typeName, literalTag, nullability)
        return ValueClass(type, property, null)
    }

    fun getAlternates(parameter: KSValueParameter): List<Triple<String, ValueClass, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperAlternateOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val alternateNode = it.findValue("alternate") as? KSNode
                val alternateAnnotation = alternateNode?.accept(AnnotationVisitor(), Unit)
                Triple(name, alternateAnnotation, it)
            }.filter {
                it.first != null && it.second != null
            }.map {
                val alternate = getAlternate(it.second)
                if (alternate == null) null else Triple(it.first!!, alternate, it.third)
            }.filterNotNull().toList()
    }
}
