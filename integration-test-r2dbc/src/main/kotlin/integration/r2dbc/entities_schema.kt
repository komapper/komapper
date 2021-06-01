package integration.r2dbc

import org.komapper.annotation.KmAutoIncrement
import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmSequence
import org.komapper.annotation.KmTable

@KmEntity
data class Aaa(
    @KmId val id: Int,
    val name: String,
) {
    companion object
}

@KmEntity
data class Bbb(
    @KmId @KmAutoIncrement val id: Int,
    val name: String?,
) {
    companion object
}

@KmEntity
data class Ccc(
    @KmId @KmSequence("CCC_SEQ", incrementBy = 50)
    val id: Int,
    val name: String,
) {
    companion object
}

@KmEntity
@KmTable(schema = "test")
data class Ddd(
    @KmId @KmAutoIncrement val id: Int,
    val name: String?,
) {
    companion object
}
