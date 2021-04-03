package org.komapper.jdbc.h2

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.core.Database
import org.komapper.core.KmEntity
import org.komapper.core.KmId
import org.komapper.core.KmIdentityGenerator
import org.komapper.core.KmSequenceGenerator
import org.komapper.core.KmTable
import org.komapper.core.dsl.SchemaQuery

@ExtendWith(Env::class)
class SchemaQueryTest(private val db: Database) {

    @KmEntity
    data class Aaa(
        @KmId val id: Int,
        val name: String,
    ) {
        companion object
    }

    @KmEntity
    @KmTable(schema = "test")
    data class Bbb(
        @KmId @KmIdentityGenerator val id: Int,
        val name: String?,
    ) {
        companion object
    }

    @KmEntity
    data class Ccc(
        @KmId @KmSequenceGenerator("CCC_SEQ", incrementBy = 50)
        val id: Int,
        val name: String,
    ) {
        companion object
    }

    private val metamodels = listOf(Aaa.metamodel(), Bbb.metamodel(), Ccc.metamodel())

    @Test
    fun create() {
        db.execute {
            SchemaQuery.create(metamodels)
        }
    }

    @Test
    fun drop() {
        db.execute {
            SchemaQuery.create(metamodels)
        }
        db.execute {
            SchemaQuery.drop(metamodels)
        }
    }

    @Test
    fun dropAll() {
        db.execute {
            SchemaQuery.create(metamodels)
        }
        db.execute {
            SchemaQuery.dropAll()
        }
    }
}
