package integration.core

import org.komapper.annotation.KomapperAutoIncrement
import org.komapper.annotation.KomapperCreatedAt
import org.komapper.annotation.KomapperEntity
import org.komapper.annotation.KomapperId
import org.komapper.annotation.KomapperSequence
import org.komapper.annotation.KomapperUpdatedAt
import org.komapper.annotation.KomapperVersion
import java.time.LocalDateTime

@KomapperEntity
data class Keywords(
    @KomapperId val `as`: Int,
    val `break`: String,
    @KomapperCreatedAt
    val `class`: LocalDateTime,
    @KomapperUpdatedAt
    val `continue`: LocalDateTime,
    @KomapperVersion
    val `do`: Int,
)

@KomapperEntity
data class KeywordsSingleId(
    @KomapperId val `fun`: Int,
    val `class`: String,
)

@KomapperEntity
data class KeywordsMultipleId(
    @KomapperId val `fun`: Int,
    @KomapperId val `this`: Int,
    val `class`: String,
)

@KomapperEntity
data class KeywordsAutoIncrementId(
    @KomapperId @KomapperAutoIncrement val `fun`: Int,
    val `class`: String,
)

@KomapperEntity
data class KeywordsSequenceId(
    @KomapperId @KomapperSequence(name = "SEQ") val `fun`: Int,
    val `class`: String,
)
