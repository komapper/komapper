package integration.jdbc

import integration.core.Place
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntityDef
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperTable
import org.komapper.annotation.KomapperVersion

@KomapperEntityDef(Place::class)
@KomapperTable("address")
data class PlaceDef(
    @KomapperId @KomapperColumn(name = "address_id")
    val id: Nothing,
    @KomapperVersion
    val version: Nothing
)
