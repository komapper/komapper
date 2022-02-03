package org.komapper.jdbc.dsl

import org.komapper.jdbc.dsl.query.MetadataQuery
import org.komapper.jdbc.dsl.query.MetadataQueryImpl

object MetadataDsl {

    fun tables(
        catalog: String? = null,
        schemaPattern: String? = null,
        tableNamePattern: String? = null,
        tableTypes: List<String> = listOf("TABLE")
    ): MetadataQuery {
        return MetadataQueryImpl(catalog, schemaPattern, tableNamePattern, tableTypes)
    }
}
