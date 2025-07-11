package org.komapper.processor.entity

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
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
import org.komapper.annotation.KomapperProjection
import org.komapper.annotation.KomapperTable
import org.komapper.core.NamingStrategy
import org.komapper.processor.Context
import org.komapper.processor.EnumStrategy
import org.komapper.processor.Exit
import org.komapper.processor.Symbols
import org.komapper.processor.backquotedName
import org.komapper.processor.findAnnotation
import org.komapper.processor.findAnnotations
import org.komapper.processor.findValue
import org.komapper.processor.isValueClass
import org.komapper.processor.name
import org.komapper.processor.report
import org.komapper.processor.resolveLiteralTag

internal class AnnotationSupport(
    private val context: Context,
) {
    private val namingStrategy: NamingStrategy = context.config.namingStrategy

    fun getTable(definitionSource: EntityDefinitionSource): Table {
        val annotation = definitionSource.defDeclaration.findAnnotation(KomapperTable::class)
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(definitionSource.entityDeclaration.simpleName.asString())
        val catalog = annotation?.findValue("catalog")?.toString()?.trim() ?: KomapperTable.CATALOG
        val schema = annotation?.findValue("schema")?.toString()?.trim() ?: KomapperTable.SCHEMA
        val alwaysQuote =
            annotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: context.config.alwaysQuote
        return Table(name, catalog, schema, alwaysQuote)
    }

    fun getProjection(definitionSource: EntityDefinitionSource): Projection? {
        return if (definitionSource.projection != null) {
            return definitionSource.projection
        } else {
            val annotation = definitionSource.defDeclaration.findAnnotation(KomapperProjection::class)
            if (annotation != null || context.config.enableEntityProjection) {
                createProjection(annotation, definitionSource.entityDeclaration)
            } else {
                null
            }
        }
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
                    val linkAnnotation = node as? KSAnnotation
                    linkAnnotation?.let { annotation ->
                        val source = annotation.findValue("source")?.toString().let {
                            when (it) {
                                KomapperLink.SOURCE, null -> defaultSource
                                !in sourceEntity.names ->
                                    report(
                                        "@KomapperLink.source \"$it\" is invalid. It must be one of ${sourceEntity.names}.",
                                        node,
                                    )

                                else -> it
                            }
                        }
                        val target = annotation.findValue("target")?.toString().let {
                            when (it) {
                                KomapperLink.TARGET, null -> defaultTarget
                                !in targetEntity.names ->
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
        val selfDefinitionSourceResolver = SelfDefinitionSourceResolver(context)
        val separateDefinitionSourceResolver = SeparateDefinitionSourceResolver(context)
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
            columnAnnotation?.findValue("alwaysQuote")?.toString()?.toBooleanStrict() ?: context.config.alwaysQuote
        val masking =
            columnAnnotation?.findValue("masking")?.toString()?.toBooleanStrict() ?: KomapperColumn.MASKING
        val updatable =
            columnAnnotation?.findValue("updatable")?.toString()?.toBooleanStrict() ?: KomapperColumn.UPDATABLE
        val insertable =
            columnAnnotation?.findValue("insertable")?.toString()?.toBooleanStrict() ?: KomapperColumn.INSERTABLE
        val alternateType = columnAnnotation?.findValue("alternateType")?.let { type ->
            if (type !is KSType) report("The alternateType is not KSType.", columnAnnotation)
            val classDeclaration = type.declaration as? KSClassDeclaration
            when {
                classDeclaration == null ->
                    report("The alternateType property is illegal.", columnAnnotation)

                classDeclaration.qualifiedName?.asString() == Symbols.Void ->
                    null

                !classDeclaration.isValueClass() ->
                    report(
                        "The alternateType property must be a value class. class=${classDeclaration.qualifiedName?.asString()}, modifiers=${classDeclaration.modifiers}",
                        columnAnnotation,
                    )

                else -> {
                    val constructor = classDeclaration.primaryConstructor
                    val isPublic = constructor?.isPublic() ?: false
                    if (!isPublic) {
                        report(
                            "The constructor of \"${classDeclaration.qualifiedName?.asString()}\" must be public.",
                            columnAnnotation,
                        )
                    }
                    val parameter = constructor?.parameters?.firstOrNull()
                        ?: error("No parameter is found in the class \"${classDeclaration.qualifiedName?.asString()}\"")
                    val declaration =
                        classDeclaration.getDeclaredProperties().firstOrNull { it.simpleName == parameter.name }
                            ?: error("No property is found in the class \"${classDeclaration.qualifiedName?.asString()}\"")
                    if (!declaration.isPublic()) {
                        report(
                            "The property parameter of \"${classDeclaration.qualifiedName?.asString()}\" must be public.",
                            columnAnnotation,
                        )
                    }
                    val propertyType = declaration.type.resolve()
                    if (propertyType.isMarkedNullable) {
                        report(
                            "The property parameter of \"${classDeclaration.qualifiedName?.asString()}\" must not be nullable.",
                            columnAnnotation,
                        )
                    }
                    val typeName = propertyType.name
                    val backquotedTypeName = propertyType.backquotedName
                    val literalTag = resolveLiteralTag(typeName)
                    val nullability = propertyType.nullability
                    val property =
                        ValueClassProperty(propertyType, parameter, declaration, typeName, backquotedTypeName, literalTag, nullability)
                    ValueClass(type, property, null)
                }
            }
        }
        return Column(name, alwaysQuote, masking, updatable, insertable, alternateType)
    }

    fun getColumns(parameter: KSValueParameter): List<Triple<String, Column, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperColumnOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val columnAnnotation = it.findValue("column") as? KSAnnotation
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
        if (annotation == null) return context.config.enumStrategy
        val declaration = when (val type = annotation.findValue("type")) {
            is KSType -> type.declaration // KSP 1
            is KSDeclaration -> type // KSP 2
            else -> null
        }
        return when (declaration?.parentDeclaration?.qualifiedName?.asString()) {
            Symbols.EnumType -> {
                when (declaration.simpleName.asString()) {
                    Symbols.EnumType_NAME -> return EnumStrategy.Name
                    Symbols.EnumType_TYPE -> return EnumStrategy.Type
                    Symbols.EnumType_ORDINAL -> return EnumStrategy.Ordinal
                    Symbols.EnumType_PROPERTY -> {
                        val hint = annotation.findValue("hint")?.toString()
                            ?: report("The hint property is missing", annotation)
                        EnumStrategy.Property(hint, annotation)
                    }
                    else -> context.config.enumStrategy
                }
            }
            else -> context.config.enumStrategy
        }
    }

    fun getEnumStrategies(parameter: KSValueParameter): List<Triple<String, EnumStrategy, KSAnnotation>> {
        return parameter.annotations
            .filter { it.shortName.asString() == KomapperEnumOverride::class.simpleName }
            .map {
                val name = it.findValue("name")?.toString()
                val enumAnnotation = it.findValue("enum") as? KSAnnotation
                Triple(name, enumAnnotation, it)
            }.filter {
                it.first != null && it.second != null
            }.map {
                val enumStrategy = getEnumStrategy(it.second)
                Triple(it.first!!, enumStrategy, it.third)
            }.toList()
    }

    companion object {
        fun createProjection(annotation: KSAnnotation?, declaration: KSClassDeclaration): Projection {
            val defaultFunction = "selectAs${declaration.simpleName.asString()}"
            return if (annotation == null) {
                Projection(defaultFunction)
            } else {
                annotation.findValue("function")?.toString()?.trim()
                    .let { if (it.isNullOrBlank()) defaultFunction else it }
                    .let { Projection(it) }
            }
        }
    }
}
