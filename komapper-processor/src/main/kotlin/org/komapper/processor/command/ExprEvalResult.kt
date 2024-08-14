package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSType
import org.komapper.core.template.expression.ExprNode

internal data class ExprEvalResult(val node: ExprNode, val type: KSType) {
    val location get() = node.location
}
