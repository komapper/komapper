package org.komapper.spring.boot.autoconfigure.r2dbc

import org.komapper.r2dbc.R2dbcDataType
import org.komapper.r2dbc.spi.R2dbcUserDefinedDataType
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.ConfigurationCondition

internal class ConditionalOnAnyR2dbcDataTypeBean : AnyNestedCondition(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {
    @ConditionalOnBean(R2dbcDataType::class)
    class OnR2dbcDataType

    @ConditionalOnBean(R2dbcUserDefinedDataType::class)
    class OnR2dbcUserDefinedDataType
}
