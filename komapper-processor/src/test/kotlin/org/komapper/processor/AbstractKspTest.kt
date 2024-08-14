package org.komapper.processor

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
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

    protected fun compile(@Language("kotlin") contents: String, vararg additionalProviders: SymbolProcessorProvider): JvmCompilationResult {
        val sourceFile = SourceFile.kotlin("source.kt", contents)
        val providers = providers.toList() + additionalProviders
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

    protected fun compile(@Language("kotlin") contents: String, block: (SymbolProcessorEnvironment, Resolver) -> List<KSAnnotated>): JvmCompilationResult {
        val provider = SymbolProcessorProvider { env ->
            object : SymbolProcessor {
                override fun process(resolver: Resolver): List<KSAnnotated> {
                    return block(env, resolver)
                }
            }
        }
        return compile(contents, provider)
    }
}
