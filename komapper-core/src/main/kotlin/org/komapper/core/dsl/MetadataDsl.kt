package org.komapper.core.dsl

import org.komapper.core.dsl.query.MetadataQuery
import org.komapper.core.dsl.query.MetadataQueryImpl

object MetadataDsl {

    fun tables(
        schemaPattern: String? = null,
        tableNamePattern: String? = null,
        tableTypes: List<String> = listOf("TABLE")
    ): MetadataQuery {
        return MetadataQueryImpl(schemaPattern, tableNamePattern, tableTypes)
    }
}
