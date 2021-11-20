package org.komapper.core.dsl.expression

import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.SetScope
import org.komapper.core.dsl.scope.ValuesScope
import org.komapper.core.dsl.scope.WhenScope
import org.komapper.core.dsl.scope.WhereScope

typealias OnDeclaration = OnScope.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias ValuesDeclaration<ENTITY> = ValuesScope<ENTITY>.() -> Unit
typealias SetDeclaration<ENTITY> = SetScope<ENTITY>.() -> Unit
typealias WhenDeclaration = WhenScope.() -> Unit
