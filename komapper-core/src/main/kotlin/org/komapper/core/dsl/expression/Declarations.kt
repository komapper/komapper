package org.komapper.core.dsl.expression

import org.komapper.core.dsl.scope.AssignmentScope
import org.komapper.core.dsl.scope.ForUpdateScope
import org.komapper.core.dsl.scope.HavingScope
import org.komapper.core.dsl.scope.OnScope
import org.komapper.core.dsl.scope.OverScope
import org.komapper.core.dsl.scope.WhenScope
import org.komapper.core.dsl.scope.WhereScope

typealias OnDeclaration = OnScope.() -> Unit
typealias WhereDeclaration = WhereScope.() -> Unit
typealias HavingDeclaration = HavingScope.() -> Unit
typealias AssignmentDeclaration<ENTITY, META> = AssignmentScope<ENTITY>.(META) -> Unit
typealias WhenDeclaration = WhenScope.() -> Unit
typealias ForUpdateDeclaration = ForUpdateScope.() -> Unit
typealias OverDeclaration = OverScope.() -> Unit
