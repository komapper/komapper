package org.komapper.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSValueParameter
import org.komapper.annotation.KomapperAggregateRoot
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperColumnOverride
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperEnum
import org.komapper.annotation.KomapperEnumOverride
import org.komapper.annotation.KomapperLink
import org.komapper.annotation.KomapperManyToOne
import org.komapper.annotation.KomapperOneToMany
import org.komapper.annotation.KomapperOneToOne
import org.komapper.annotation.KomapperTable
import org.komapper.core.NamingStrategy

internal class AnnotationSupport(
    @Suppress("unused") private val logger: KSPLogger,
    private val config: Config,
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

    fun getAggregateRoot(definitionSource: EntityDefinitionSource): AggregateRoot? {
        val annotation = definitionSource.defDeclaration.findAnnotation(KomapperAggregateRoot::class)
        return if (annotation == null) {
            null
        } else {
            val target = definitionSource.names.first()
            val navigator = annotation.findValue("navigator")?.toString()?.trim().let { navigator ->
                if (navigator.isNullOrBlank()) target else navigator
            }
            AggregateRoot(navigator, definitionSource, target)
        }
    }

    fun getAssociations(definitionSource: EntityDefinitionSource): List<Association> {
        val pairs = listOf(
            KomapperOneToOne::class to AssociationKind.ONE_TO_ONE,
            KomapperOneToMany::class to AssociationKind.ONE_TO_MANY,
            KomapperManyToOne::class to AssociationKind.MANY_TO_ONE,
        )
        return pairs.flatMap { (klass, kind) ->
            definitionSource.defDeclaration
                .findAnnotations(klass)
                .map { createAssociation(definitionSource, it, kind) }
        }
    }

    private fun createAssociation(
        sourceEntity: EntityDefinitionSource,
        annotation: KSAnnotation,
        kind: AssociationKind,
    ): Association {
        val targetEntity = annotation.findValue("targetEntity").let { type ->
            if (type !is KSType) report("targetEntity is not KSType: $type", annotation)
            resolveEntityDefinitionSource(type.declaration) ?: report(
                "The targetEntity must be annotated with either @KomapperEntity or @KomapperEntityDef.",
                annotation,
            )
        }
        val defaultSource = sourceEntity.names.first()
        val defaultTarget = targetEntity.names.first()
        val link = annotation.findValue("link")?.let { node ->
            when (node) {
                is KSNode -> {
                    val linkAnnotation = node.accept(AnnotationVisitor(), Unit)
                    linkAnnotation?.let { annotation ->
                        val source = annotation.findValue("source")?.toString().let {
                            when (it) {
                                KomapperLink.SOURCE -> defaultSource
                                null, !in sourceEntity.names ->
                                    report(
                                        "@KomapperLink.source \"$it\" is invalid. It must be one of ${sourceEntity.names}.",
                                        node,
                                    )
                                else -> it
                            }
                        }
                        val target = annotation.findValue("target")?.toString().let {
                            when (it) {
                                KomapperLink.TARGET -> defaultTarget
                                null, !in targetEntity.names ->
                                    report(
                                        "@KomapperLink.target \"$it\" is invalid. It must be one of ${targetEntity.names}.",
                                        node,
                                    )
                                else -> it
                            }
                        }
                        Link(source, target)
                    }
                }
                else -> null
            }
        } ?: report("Cannot get a value from the link property", annotation)
        val navigator = annotation.findValue("navigator")?.toString()?.trim().let { navigator ->
            if (navigator.isNullOrBlank()) link.target else navigator
        }
        return Association(annotation, navigator, sourceEntity, targetEntity, link, kind)
    }

    private fun resolveEntityDefinitionSource(declaration: KSDeclaration): EntityDefinitionSource? {
        val selfDefinitionSourceResolver = SelfDefinitionSourceResolver(config)
        val separateDefinitionSourceResolver = SeparateDefinitionSourceResolver(config)
        val definitionSource = declaration.annotations.firstNotNullOfOrNull { annotation ->
            when (annotation.shortName.asString()) {
                KomapperEntity::class.simpleName -> {
                    try {
                        selfDefinitionSourceResolver.resolve(declaration)
                    } catch (e: Exit) {
                        null
                    }
                }
                KomapperEntityDef::class.simpleName -> {
                    try {
                        separateDefinitionSourceResolver.resolve(declaration)
                    } catch (e: Exit) {
                        null
                    }
                }
                else -> null
            }
        }
        return definitionSource
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
