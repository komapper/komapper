package integration.r2dbc

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmColumn
import org.komapper.annotation.KmCreatedAt
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmSequence
import org.komapper.annotation.KmTable
import org.komapper.annotation.KmUpdatedAt
import org.komapper.annotation.KmVersion
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

@KmEntity
@KmTable("ADDRESS")
data class VAddress(
    @KmId val addressId: IntId,
    val street: Street,
    @KmVersion val version: Version
) {
    companion object
}

@KmEntity
@KmTable("PERSON")
data class VPerson(
    @KmId @KmColumn("PERSON_ID") val personId: IntId,
    val name: String,
    @KmCreatedAt @KmColumn("CREATED_AT") val createdAt: Timestamp? = null,
    @KmUpdatedAt @KmColumn("UPDATED_AT") val updatedAt: Timestamp? = null
) {
    companion object
}

@KmEntity
@KmTable("IDENTITY_STRATEGY")
data class VIdentityStrategy(
    @KmId @KmAutoIncrement val id: IntId?,
    val value: String
) {
    companion object
}

@KmEntity
@KmTable("SEQUENCE_STRATEGY")
data class VSequenceStrategy(
    @KmId @KmSequence(name = "SEQUENCE_STRATEGY_ID", incrementBy = 100) val id: IntId,
    val value: String
) {
    companion object
}
