package org.komapper.processor

import org.komapper.core.CamelToLowerSnakeCase
import org.komapper.core.CamelToUpperSnakeCase
import org.komapper.core.Implicit
import org.komapper.core.NamingStrategy

internal data class Config(
    val prefix: String,
    val suffix: String,
    val namingStrategy: NamingStrategy,
) {
    companion object {
        private const val prefixKeyName = "komapper.prefix"
        private const val suffixKeyName = "komapper.suffix"
        private const val namingStrategyKeyName = "komapper.namingStrategy"

        fun create(options: Map<String, String>): Config {
            val prefix = options.getOrDefault(prefixKeyName, "_")
            val suffix = options.getOrDefault(suffixKeyName, "")
            val namingStrategy = options.getOrDefault(namingStrategyKeyName, "lower_snake_case").let {
                when (it) {
                    "lower_snake_case" -> CamelToLowerSnakeCase
                    "UPPER_SNAKE_CASE" -> CamelToUpperSnakeCase
                    "implicit" -> Implicit
                    else -> error("'$it' is illegal value as a $namingStrategyKeyName option.")
                }
            }
            return Config(prefix, suffix, namingStrategy)
        }
    }
}
