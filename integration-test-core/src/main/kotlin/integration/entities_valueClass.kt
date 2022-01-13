package integration

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
value class IntId(val value: Int) : Comparable<IntId> {
    override fun compareTo(other: IntId): Int {
        return value.compareTo(other.value)
    }
}

@JvmInline
value class Street(val value: String)

@JvmInline
value class Version(val value: Int)

@JvmInline
value class Timestamp(val value: LocalDateTime)

@KomapperEntity
@KomapperTable("address")
data class VAddress(
    @KomapperId val addressId: IntId,
    val street: Street,
    @KomapperVersion val version: Version
)

@KomapperEntity
@KomapperTable("person")
data class VPerson(
    @KomapperId @KomapperColumn("person_id") val personId: IntId,
    val name: String,
    @KomapperCreatedAt @KomapperColumn("created_at") val createdAt: Timestamp? = null,
    @KomapperUpdatedAt @KomapperColumn("updated_at") val updatedAt: Timestamp? = null
)

@KomapperEntity
@KomapperTable("identity_strategy")
data class VIdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: IntId?,
    @KomapperColumn(alwaysQuote = true)val value: String
)

@KomapperEntity
@KomapperTable("sequence_strategy")
data class VSequenceStrategy(
    @KomapperId @KomapperSequence(name = "sequence_strategy_id", incrementBy = 100) val id: IntId,
    @KomapperColumn(alwaysQuote = true)val value: String
)
