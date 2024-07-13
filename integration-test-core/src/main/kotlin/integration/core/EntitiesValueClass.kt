package integration.core

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import java.time.LocalDateTime

@JvmInline
public value class IntId(public val value: Int) : Comparable<IntId> {
    override fun compareTo(other: IntId): Int {
        return value.compareTo(other.value)
    }
}

@JvmInline
public value class Street(public val value: String)

@JvmInline
public value class Version(public val value: Int)

@JvmInline
public value class Timestamp(public val value: LocalDateTime)

@KomapperEntity
@KomapperTable("address")
public data class VAddress(
    @KomapperId val addressId: IntId,
    val street: Street,
    @KomapperVersion val version: Version,
)

@KomapperEntity
@KomapperTable("person")
public data class VPerson(
    @KomapperId
    @KomapperColumn("person_id")
    val personId: IntId,
    val name: String,
    @KomapperCreatedAt
    @KomapperColumn("created_at")
    val createdAt: Timestamp? = null,
    @KomapperUpdatedAt
    @KomapperColumn("updated_at")
    val updatedAt: Timestamp? = null,
)

@KomapperEntity
@KomapperTable("identity_strategy")
public data class VIdentityStrategy(
    @KomapperId @KomapperAutoIncrement
    val id: IntId?,
    @KomapperColumn(alwaysQuote = true)val value: String,
)

@KomapperEntity
@KomapperTable("sequence_strategy")
public data class VSequenceStrategy(
    @KomapperId
    @KomapperSequence(name = "sequence_strategy_id", incrementBy = 100)
    val id: IntId,
    @KomapperColumn(alwaysQuote = true)val value: String,
)
