package org.komapper.processor

import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import org.komapper.annotation.KomapperAssociation
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

    fun getAssociations(definitionSource: EntityDefinitionSource): List<Association> {
        return definitionSource.defDeclaration
            .findAnnotations(KomapperAssociation::class)
            .map { annotation ->
                val targetEntity = annotation.findValue("targetEntity").let {
                    if (it !is KSType) report("targetEntity is not KSType: $it", annotation)
                    it.declaration.accept(ClassDeclarationVisitor(), Unit) ?: report("targetEntity is not KSClassDeclaration: ${it.declaration}", annotation)
                }
                val name = annotation.findValue("name")?.toString()?.trim().let { name ->
                    if (name.isNullOrBlank()) {
                        targetEntity.simpleName.asString().replaceFirstChar { it.lowercaseChar() }
                    } else {
                        name
                    }
                }
                val kind = annotation.findValue("type").let {
                    when (val type = it?.toString()) {
                        Symbols.AssociationType_ONE_TO_ONE -> AssociationKind.ONE_TO_ONE
                        Symbols.AssociationType_ONE_TO_MANY -> AssociationKind.ONE_TO_MANY
                        Symbols.AssociationType_MANY_TO_ONE -> AssociationKind.MANY_TO_ONE
                        else -> report("Unknown association type is found: $type", annotation)
                    }
                }
                Association(targetEntity, kind, name)
            }
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
}
