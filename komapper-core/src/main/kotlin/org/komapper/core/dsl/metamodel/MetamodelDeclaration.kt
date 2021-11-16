package org.komapper.core.dsl.metamodel

typealias MetamodelDeclaration<ENTITY, ID, META> = MetamodelScope<ENTITY, ID, META>.(META) -> Unit
