package org.komapper.processor.command

import com.google.devtools.ksp.symbol.KSAnnotated
import org.komapper.core.ThreadSafe
import org.komapper.core.template.sql.SqlException
import org.komapper.processor.Context
import org.komapper.processor.Exit
import org.komapper.processor.report
import kotlin.reflect.KClass

@ThreadSafe
internal class PartialAnalyzer(private val context: Context, private val annotationClass: KClass<*>) {

    fun analyze(symbol: KSAnnotated): PartialAnalysisResult {
        val partial = try {
            PartialFactory(context, annotationClass, symbol).create()
        } catch (e: Exit) {
            return PartialAnalysisResult.Error(e)
        }
        if (!partial.disableValidation) {
            try {
                validatePartial(partial)
            } catch (e: Exit) {
                return PartialAnalysisResult.Failure(partial, e)
            }
        }
        return PartialAnalysisResult.Success(partial)
    }

    private fun validatePartial(partial: Partial) {
        val usedParams = try {
            SqlValidator(context, partial.sql, partial.paramMap).validate()
        } catch (e: SqlException) {
            report("SQL validation error: ${e.message}", partial.annotation)
        }
        val unusedParams = partial.paramMap.keys - usedParams - partial.unusedParams
        if (unusedParams.isNotEmpty()) {
            context.logger.warn("Unused SQL params: $unusedParams. You can suppress this warning message by specifying @KomapperUnused for the param properties.", partial.classDeclaration)
        }
    }
}

internal sealed class PartialAnalysisResult {
    data class Success(val partial: Partial) : PartialAnalysisResult()
    data class Failure(val partial: Partial, val exit: Exit) : PartialAnalysisResult()
    data class Error(val exit: Exit) : PartialAnalysisResult()
}
