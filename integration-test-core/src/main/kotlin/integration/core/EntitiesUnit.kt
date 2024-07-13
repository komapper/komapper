package integration.core

import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import java.time.LocalDateTime

public object MyMeta

@KomapperEntity(unit = MyMeta::class)
@KomapperTable("address")
public data class MyAddress(
    @KomapperId
    @KomapperColumn(name = "address_id")
    val addressId: Int,
    val street: String,
    @KomapperVersion val version: Int,
)

public data class MyPerson(
    val personId: Int,
    val name: String,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
)

@KomapperEntityDef(entity = MyPerson::class, unit = MyMeta::class)
@KomapperTable("person")
public data class MyPersonDef(
    @KomapperId
    @KomapperColumn("person_id")
    val personId: Nothing,
    @KomapperCreatedAt
    @KomapperColumn("created_at")
    val createdAt: Nothing,
    @KomapperUpdatedAt
    @KomapperColumn("updated_at")
    val updatedAt: Nothing,
)
