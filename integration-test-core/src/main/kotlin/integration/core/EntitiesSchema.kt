package integration.core

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence

@KomapperEntity
public data class Aaa(
    @KomapperId
    @KomapperColumn(alwaysQuote = true)
    val id: Int,
    val name: String,
)

@KomapperEntity
public data class Bbb(
    @KomapperId @KomapperAutoIncrement
    val id: Int,
    val name: String?,
)

@KomapperEntity
public data class Ccc(
    @KomapperId
    @KomapperSequence("ccc_seq", incrementBy = 50)
    val id: Int,
    val name: String,
)

@KomapperEntity
public data class CompositeKey(
    @KomapperId val addressId1: Int,
    @KomapperId val addressId2: Int,
    val street: String,
    val version: Int,
)

@KomapperEntity
public data class AutoIncrementTable(
    @KomapperId @KomapperAutoIncrement
    val id: Int?,
    @KomapperColumn(alwaysQuote = true) val value: String,
)

@KomapperEntity
public data class SequenceTable(
    @KomapperId
    @KomapperSequence(name = "sequence_table_id", incrementBy = 100)
    val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: String,
)
