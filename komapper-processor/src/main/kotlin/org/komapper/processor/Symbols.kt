package org.komapper.processor

internal object Symbols {
    const val ConcurrentHashMap = "java.util.concurrent.ConcurrentHashMap"
    const val EmbeddableMetamodel = "org.komapper.core.dsl.metamodel.EmbeddableMetamodel"
    const val EmbeddedMetamodel = "org.komapper.core.dsl.metamodel.EmbeddedMetamodel"
    const val EntityMetamodel = "org.komapper.core.dsl.metamodel.EntityMetamodel"
    const val EntityMetamodelStub = "org.komapper.core.dsl.metamodel.EntityMetamodelStub"
    const val EntityMetamodelImplementor = "org.komapper.core.dsl.metamodel.EntityMetamodelImplementor"
    const val IdGenerator = "org.komapper.core.dsl.metamodel.IdGenerator"
    const val AutoIncrement = "org.komapper.core.dsl.metamodel.IdGenerator.AutoIncrement"
    const val Sequence = "org.komapper.core.dsl.metamodel.IdGenerator.Sequence"
    const val IdContext = "org.komapper.core.dsl.metamodel.IdContext"
    const val UUID = "java.util.UUID"
    const val PropertyDescriptor = "org.komapper.core.dsl.metamodel.PropertyDescriptor"
    const val PropertyMetamodel = "org.komapper.core.dsl.metamodel.PropertyMetamodel"
    const val PropertyMetamodelImpl = "org.komapper.core.dsl.metamodel.PropertyMetamodelImpl"
    const val PropertyMetamodelStub = "org.komapper.core.dsl.metamodel.PropertyMetamodelStub"
    const val Clock = "java.time.Clock"
    const val EntityDescriptor = "__EntityDescriptor"
    const val EntityMetamodelDeclaration = "org.komapper.core.dsl.metamodel.EntityMetamodelDeclaration"
    const val Meta = "org.komapper.core.dsl.Meta"
    const val KomapperStub = "KomapperStub"
    const val Operand = "org.komapper.core.dsl.expression.Operand"
    const val Argument = "$Operand.Argument"
    const val checkMetamodelVersion = "org.komapper.core.dsl.metamodel.checkMetamodelVersion"
    const val Instant = "java.time.Instant"
    const val LocalDateTime = "java.time.LocalDateTime"
    const val OffsetDateTime = "java.time.OffsetDateTime"
    const val KotlinInstant = "kotlinx.datetime.Instant"
    const val KotlinLocalDateTime = "kotlinx.datetime.LocalDateTime"
    const val toKotlinInstant = "kotlinx.datetime.toKotlinInstant"
    const val toKotlinLocalDateTime = "kotlinx.datetime.toKotlinLocalDateTime"
    const val EnumType_NAME = "org.komapper.annotation.EnumType.NAME"
    const val EnumType_ORDINAL = "org.komapper.annotation.EnumType.ORDINAL"
    const val DefaultUnit = "org.komapper.annotation.DefaultUnit"
}
