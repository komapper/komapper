package integration

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperColumn
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable

@KomapperEntity
data class Aaa(
    @KomapperId @KomapperColumn(alwaysQuote = true) val id: Int,
    val name: String,
)

@KomapperEntity
data class Bbb(
    @KomapperId @KomapperAutoIncrement val id: Int,
    val name: String?,
)

@KomapperEntity
data class Ccc(
    @KomapperId @KomapperSequence("ccc_seq", incrementBy = 50)
    val id: Int,
    val name: String,
)

@KomapperEntity
@KomapperTable(schema = "test")
data class Ddd(
    @KomapperId @KomapperAutoIncrement val id: Int,
    val name: String?,
)
