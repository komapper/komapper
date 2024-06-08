package org.komapper.processor

internal object Symbols {
    // types
    const val EntityMetamodelDeclaration = "org.komapper.core.dsl.metamodel.EntityMetamodelDeclaration"
    const val EntityMetamodelFactory = "org.komapper.core.dsl.metamodel.EntityMetamodelFactory"
    const val EntityMetamodelFactorySpi = "org.komapper.core.spi.EntityMetamodelFactory"
    const val EnumType_NAME = "org.komapper.annotation.EnumType.NAME"
    const val EnumType_ORDINAL = "org.komapper.annotation.EnumType.ORDINAL"
    const val EnumType_PROPERTY = "org.komapper.annotation.EnumType.PROPERTY"
    const val EnumType_TYPE = "org.komapper.annotation.EnumType.TYPE"
    const val Instant = "java.time.Instant"
    const val KomapperStub = "KomapperStub"
    const val KotlinInstant = "kotlinx.datetime.Instant"
    const val KotlinLocalDateTime = "kotlinx.datetime.LocalDateTime"
    const val LocalDateTime = "java.time.LocalDateTime"
    const val Meta = "org.komapper.core.dsl.Meta"
    const val OffsetDateTime = "java.time.OffsetDateTime"
    const val ProjectionMeta = "org.komapper.core.dsl.ProjectionMeta"
    const val Void = "java.lang.Void"

    // functions
    const val checkMetamodelVersion = "org.komapper.core.dsl.metamodel.checkMetamodelVersion"
    const val toKotlinInstant = "kotlinx.datetime.toKotlinInstant"
    const val toKotlinLocalDateTime = "kotlinx.datetime.toKotlinLocalDateTime"
}
