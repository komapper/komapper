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
@KomapperTable("ADDRESS")
data class VAddress(
    @KomapperId val addressId: IntId,
    val street: Street,
    @KomapperVersion val version: Version
) {
    companion object
}

@KomapperEntity
@KomapperTable("PERSON")
data class VPerson(
    @KomapperId @KomapperColumn("PERSON_ID") val personId: IntId,
    val name: String,
    @KomapperCreatedAt @KomapperColumn("CREATED_AT") val createdAt: Timestamp? = null,
    @KomapperUpdatedAt @KomapperColumn("UPDATED_AT") val updatedAt: Timestamp? = null
) {
    companion object
}

@KomapperEntity
@KomapperTable("IDENTITY_STRATEGY")
data class VIdentityStrategy(
    @KomapperId @KomapperAutoIncrement val id: IntId?,
    val value: String
) {
    companion object
}

@KomapperEntity
@KomapperTable("SEQUENCE_STRATEGY")
data class VSequenceStrategy(
    @KomapperId @KomapperSequence(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: IntId,
    val value: String
) {
    companion object
}
