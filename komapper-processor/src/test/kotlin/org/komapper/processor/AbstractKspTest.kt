package org.komapper.processor

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.JvmCompilationResult
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspIncremental
import com.tschuchort.compiletesting.symbolProcessorProviders
import com.tschuchort.compiletesting.useKsp2
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

abstract class AbstractKspTest(private vararg val providers: SymbolProcessorProvider) {

    @TempDir
    @JvmField
    protected var tempDir: Path? = null

    protected fun compile(@Language("kotlin") contents: String): JvmCompilationResult {
        val sourceFile = SourceFile.kotlin("source.kt", contents)
        val compilation = KotlinCompilation()
            .apply {
                useKsp2()
                languageVersion = "2.0"
                apiVersion = "1.7"
                workingDir = tempDir!!.toFile()
                inheritClassPath = true
                symbolProcessorProviders = providers.toMutableList()
                sources = listOf(sourceFile)
                verbose = false
                kspIncremental = false
            }
        return compilation.compile()
    }
}
