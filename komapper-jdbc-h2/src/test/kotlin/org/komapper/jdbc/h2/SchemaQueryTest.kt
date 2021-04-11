package org.komapper.jdbc.h2

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmIdentityGenerator
import org.komapper.annotation.KmSequenceGenerator
import org.komapper.annotation.KmTable
import org.komapper.core.Database
import org.komapper.core.dsl.SchemaQuery
import org.komapper.core.dsl.execute

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
        SchemaQuery.create(metamodels).dryRun()
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
