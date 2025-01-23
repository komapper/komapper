package org.komapper.spring.boot.autoconfigure.jdbc

import org.komapper.jdbc.JdbcDataType
import org.komapper.jdbc.spi.JdbcUserDefinedDataType
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.context.annotation.ConfigurationCondition

internal class ConditionalOnAnyJdbcDataTypeBean : AnyNestedCondition(ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN) {
    @ConditionalOnBean(JdbcDataType::class)
    class OnJdbcDataType

    @ConditionalOnBean(JdbcUserDefinedDataType::class)
    class OnJdbcUserDefinedDataType
}
