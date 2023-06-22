package org.komapper.tx.r2dbc

import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperVersion

@KomapperEntity
data class Person(
    @KomapperId
    val personId: Int,
    val name: String,
    @KomapperVersion
    val version: Int = 0,
)
