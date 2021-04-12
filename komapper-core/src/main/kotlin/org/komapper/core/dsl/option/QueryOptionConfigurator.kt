package org.komapper.core.dsl.option

fun interface QueryOptionConfigurator<OPTION : QueryOption> {
    fun apply(option: OPTION): OPTION
}
