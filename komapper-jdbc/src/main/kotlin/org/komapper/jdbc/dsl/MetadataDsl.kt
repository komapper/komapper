package org.komapper.jdbc.dsl

import org.komapper.core.dsl.Dsl
import org.komapper.jdbc.dsl.query.MetadataQuery
import org.komapper.jdbc.dsl.query.MetadataQueryImpl

object MetadataDsl : Dsl {

    fun tables(
        catalog: String? = null,
        schemaPattern: String? = null,
        tableNamePattern: String? = null,
        tableTypes: List<String> = listOf("TABLE")
    ): MetadataQuery {
        return MetadataQueryImpl(catalog, schemaPattern, tableNamePattern, tableTypes)
    }
}
