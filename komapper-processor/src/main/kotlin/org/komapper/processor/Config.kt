package org.komapper.processor

import org.komapper.core.CamelToLowerSnakeCase
import org.komapper.core.CamelToUpperSnakeCase
import org.komapper.core.Implicit
import org.komapper.core.NamingStrategy

internal data class Config(
    val prefix: String,
    val suffix: String,
    val namingStrategy: NamingStrategy,
    val metaObject: String
) {
    companion object {
        private const val PREFIX = "komapper.prefix"
        private const val SUFFIX = "komapper.suffix"
        private const val NAMING_STRATEGY = "komapper.namingStrategy"
        private const val META_OBJECT = "komapper.metaObject"

        fun create(options: Map<String, String>): Config {
            val prefix = options.getOrDefault(PREFIX, "_")
            val suffix = options.getOrDefault(SUFFIX, "")
            val namingStrategy = options.getOrDefault(NAMING_STRATEGY, "lower_snake_case").let {
                when (it) {
                    "lower_snake_case" -> CamelToLowerSnakeCase
                    "UPPER_SNAKE_CASE" -> CamelToUpperSnakeCase
                    "implicit" -> Implicit
                    else -> error("'$it' is illegal value as a $NAMING_STRATEGY option.")
                }
            }
            val metaObject = options.getOrDefault(META_OBJECT, Symbols.Meta)
            return Config(prefix, suffix, namingStrategy, metaObject)
        }
    }
}
