package org.komapper.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.symbol.KSValueParameter

internal class EntityDefFactory(
    config: Config,
    private val definitionSource: EntityDefinitionSource
) {
    private val namingStrategy: NamingStrategy = config.namingStrategy
    private val defDeclaration = definitionSource.defDeclaration

    fun create(): EntityDef {
        val table = getTable()
        val allProperties = createAllProperties()
        return EntityDef(definitionSource, table, allProperties)
    }

    private fun getTable(): Table {
        val annotation = defDeclaration.findAnnotation("KmTable")
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(definitionSource.entityDeclaration.simpleName.asString())
        val catalog = annotation?.findValue("catalog")?.toString()?.trim() ?: ""
        val schema = annotation?.findValue("schema")?.toString()?.trim() ?: ""
        val alwaysQuote = annotation?.findValue("alwaysQuote")?.toString()?.let { it == "true" } ?: false
        return Table(name, catalog, schema, alwaysQuote)
    }

    private fun getColumn(parameter: KSValueParameter): Column {
        val annotation = parameter.findAnnotation("KmColumn")
        val name = annotation?.findValue("name")?.toString()?.trim().let {
            if (it.isNullOrBlank()) null else it
        } ?: namingStrategy.apply(parameter.toString())
        val alwaysQuote = annotation?.findValue("alwaysQuote")?.toString()?.let { it == "true" } ?: false
        return Column(name, alwaysQuote)
    }

    private fun createAllProperties(): List<PropertyDef> {
        val propertyDeclarationMap = defDeclaration.getDeclaredProperties().associateBy { it.simpleName }
        return defDeclaration.primaryConstructor?.parameters
            ?.asSequence()
            ?.map { parameter ->
                val declaration = propertyDeclarationMap[parameter.name]
                    ?: report("The corresponding property declaration is not found.", parameter)
                val column = getColumn(parameter)
                val kind = createPropertyKind(parameter)
                val generatorKind = createIdGeneratorKind(parameter, kind)
                PropertyDef(parameter, declaration, column, kind, generatorKind)
            }?.also {
                validateProperties(it)
            }?.toList() ?: emptyList()
    }

    private fun validateProperties(properties: Sequence<PropertyDef>) {
        if (properties.anyDuplicates { it.kind is PropertyKind.Version }) {
            report("Multiple @KmVersion cannot coexist in a single class.", defDeclaration)
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.CreatedAt }) {
            report("Multiple @KmCreatedAt cannot coexist in a single class.", defDeclaration)
        }
        if (properties.anyDuplicates { it.kind is PropertyKind.UpdatedAt }) {
            report("Multiple @KmUpdatedAt cannot coexist in a single class.", defDeclaration)
        }
    }

    private fun createPropertyKind(parameter: KSValueParameter): PropertyKind? {
        var id: PropertyKind.Id? = null
        var version: PropertyKind.Version? = null
        var createdAt: PropertyKind.CreatedAt? = null
        var updatedAt: PropertyKind.UpdatedAt? = null
        var ignore: PropertyKind.Ignore? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                "KmId" -> id = PropertyKind.Id(a)
                "KmVersion" -> version = PropertyKind.Version(a)
                "KmCreatedAt" -> createdAt = PropertyKind.CreatedAt(a)
                "KmUpdatedAt" -> updatedAt = PropertyKind.UpdatedAt(a)
                "KmIgnore" -> ignore = PropertyKind.Ignore(a)
            }
        }
        val kinds = listOfNotNull(id, version, createdAt, updatedAt, ignore)
        if (kinds.size > 1) {
            val iterator = kinds.iterator()
            val a1 = iterator.next().annotation
            val a2 = iterator.next().annotation
            report("$a1 and $a2 cannot coexist on the same property.", parameter)
        }
        return kinds.firstOrNull()
    }

    private fun createIdGeneratorKind(parameter: KSValueParameter, propertyKind: PropertyKind?): IdGeneratorKind? {
        var identity: IdGeneratorKind.Identity? = null
        var sequence: IdGeneratorKind.Sequence? = null
        for (a in parameter.annotations) {
            when (a.shortName.asString()) {
                "KmIdentityGenerator" -> identity = IdGeneratorKind.Identity(a)
                "KmSequenceGenerator" -> sequence = let {
                    val name = a.findValue("name")?.toString()?.trim()
                        ?: report("@KmSequenceGenerator.name is not found.", a)
                    val incrementBy = a.findValue("incrementBy")
                        ?: report("@KmSequenceGenerator.incrementBy is not found.", a)
                    val catalog = a.findValue("catalog")?.toString()?.trim() ?: ""
                    val schema = a.findValue("schema")?.toString()?.trim() ?: ""
                    val alwaysQuote = a.findValue("alwaysQuote")?.toString()?.let { it == "true" } ?: false
                    IdGeneratorKind.Sequence(a, name, incrementBy, catalog, schema, alwaysQuote)
                }
            }
        }
        val idGeneratorKinds = listOfNotNull(identity, sequence)
        if (idGeneratorKinds.size > 1) {
            val iterator = idGeneratorKinds.iterator()
            val a1 = iterator.next().annotation
            val a2 = iterator.next().annotation
            report("$a1 and $a2 cannot coexist on the same property.", parameter)
        }
        val idGeneratorKind = idGeneratorKinds.firstOrNull() ?: return null
        if (propertyKind !is PropertyKind.Id) {
            report("${idGeneratorKind.annotation} and @KmId must coexist on the same property.", parameter)
        }
        return idGeneratorKind
    }
}
