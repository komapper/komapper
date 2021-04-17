package integration

import org.komapper.annotation.KmEntity
import org.komapper.annotation.KmId
import org.komapper.annotation.KmIdentityGenerator
import org.komapper.annotation.KmSequenceGenerator
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
    @KmId @KmIdentityGenerator val id: Int,
    val name: String?,
) {
    companion object
}

@KmEntity
data class Ccc(
    @KmId @KmSequenceGenerator("CCC_SEQ", incrementBy = 50)
    val id: Int,
    val name: String,
) {
    companion object
}

@KmEntity
@KmTable(schema = "test")
data class Ddd(
    @KmId @KmIdentityGenerator val id: Int,
    val name: String?,
) {
    companion object
}
