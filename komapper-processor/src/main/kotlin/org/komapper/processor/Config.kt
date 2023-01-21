package org.komapper.processor

import org.komapper.core.CamelToLowerSnakeCase
import org.komapper.core.CamelToUpperSnakeCase
import org.komapper.core.Implicit
import org.komapper.core.NamingStrategy

internal data class Config(
    val prefix: String,
    val suffix: String,
    val enumStrategy: EnumStrategy,
    val namingStrategy: NamingStrategy,
    val metaObject: String,
    val alwaysQuote: Boolean,
    val enableEntityMetamodelListing: Boolean,
) {
    companion object {
        private const val PREFIX = "komapper.prefix"
        private const val SUFFIX = "komapper.suffix"
        private const val ENUM_STRATEGY = "komapper.enumStrategy"
        private const val NAMING_STRATEGY = "komapper.namingStrategy"
        private const val META_OBJECT = "komapper.metaObject"
        private const val ALWAYS_QUOTE = "komapper.alwaysQuote"
        private const val ENABLE_ENTITY_METAMODEL_LISTING = "komapper.enableEntityMetamodelListing"

        fun create(options: Map<String, String>): Config {
            val prefix = options.getOrDefault(PREFIX, "_")
            val suffix = options.getOrDefault(SUFFIX, "")
            val enumStrategy = options.getOrDefault(ENUM_STRATEGY, "name").let {
                when (it) {
                    "name" -> EnumStrategy.Name
                    "ordinal" -> EnumStrategy.Ordinal
                    else -> error("'$it' is illegal value as a $ENUM_STRATEGY option.")
                }
            }
            val namingStrategy = options.getOrDefault(NAMING_STRATEGY, "lower_snake_case").let {
                when (it) {
                    "lower_snake_case" -> CamelToLowerSnakeCase
                    "UPPER_SNAKE_CASE" -> CamelToUpperSnakeCase
                    "implicit" -> Implicit
                    else -> error("'$it' is illegal value as a $NAMING_STRATEGY option.")
                }
            }
            val metaObject = options.getOrDefault(META_OBJECT, Symbols.Meta)
            val alwaysQuote = options[ALWAYS_QUOTE]?.toBooleanStrict() ?: false
            val enableEntityMetamodelListing = options[ENABLE_ENTITY_METAMODEL_LISTING]?.toBooleanStrict() ?: false
            return Config(prefix, suffix, enumStrategy, namingStrategy, metaObject, alwaysQuote, enableEntityMetamodelListing)
        }
    }
}
