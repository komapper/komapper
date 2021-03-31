package org.komapper.ksp

internal data class Config(
    val prefix: String,
    val suffix: String,
    val namingStrategy: NamingStrategy
) {
    companion object {
        private const val prefixKeyName = "komapper.prefix"
        private const val suffixKeyName = "komapper.suffix"
        private const val namingStrategyKeyName = "komapper.namingStrategy"

        fun create(options: Map<String, String>): Config {
            val prefix = options[prefixKeyName] ?: ""
            val suffix = options[suffixKeyName] ?: "_"
            val namingStrategy = options[namingStrategyKeyName].let {
                when (it) {
                    null -> CamelToLowerSnakeCase
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
