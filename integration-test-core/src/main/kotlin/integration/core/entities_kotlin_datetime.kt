package integration.core

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt

@KomapperEntity
@KomapperTable("instant_test")
data class KotlinInstantTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: Instant)

@KomapperEntity
@KomapperTable("local_date_test")
data class KotlinLocalDateTest(@KomapperId val id: Int, @KomapperColumn(alwaysQuote = true) val value: LocalDate)

@KomapperEntity
@KomapperTable("local_date_time_test")
data class KotlinLocalDateTimeTest(
    @KomapperId val id: Int,
    @KomapperColumn(alwaysQuote = true) val value: LocalDateTime
)

@KomapperEntity
@KomapperTable("human")
data class KotlinInstantPerson(
    @KomapperId @KomapperColumn("human_id") val humanId: Int,
    val name: String,
    @KomapperCreatedAt @KomapperColumn("created_at") val createdAt: Instant? = null,
    @KomapperUpdatedAt @KomapperColumn("updated_at") val updatedAt: Instant? = null
)

@KomapperEntity
@KomapperTable("person")
data class KotlinDatetimePerson(
    @KomapperId @KomapperColumn("person_id") val personId: Int,
    val name: String,
    @KomapperCreatedAt @KomapperColumn("created_at") val createdAt: LocalDateTime? = null,
    @KomapperUpdatedAt @KomapperColumn("updated_at") val updatedAt: LocalDateTime? = null
)
