package integration

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperTable

@KomapperEntity
data class Aaa(
    @KomapperId val id: Int,
    val name: String,
) {
    companion object
}

@KomapperEntity
data class Bbb(
    @KomapperId @KomapperAutoIncrement val id: Int,
    val name: String?,
) {
    companion object
}

@KomapperEntity
data class Ccc(
    @KomapperId @KomapperSequence("CCC_SEQ", incrementBy = 50)
    val id: Int,
    val name: String,
) {
    companion object
}

@KomapperEntity
@KomapperTable(schema = "test")
data class Ddd(
    @KomapperId @KomapperAutoIncrement val id: Int,
    val name: String?,
) {
    companion object
}
