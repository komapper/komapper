package org.komapper.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KomapperPluginTest {
    @JvmField
    @TempDir
    var testProjectDir: File? = null
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeTest
    fun setup() {
        settingsFile = File(testProjectDir, "settings.gradle.kts").apply {
            writeText(
                """
                rootProject.name = "app"
                """.trimIndent()
            )
        }
        buildFile = File(testProjectDir, "build.gradle.kts")
    }

    @Test
    fun tasks() {
        buildFile.writeText(
            """
            plugins { id("org.komapper.gradle") }
            komapper {
                generators {
                    register("h2") {
                    }
                    register("mysql") {
                    }
                }
            }
            """.trimIndent()
        )
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all", "--stacktrace")
            .withPluginClasspath()
            .build()
        println(result.output)
        assertTrue(result.output.contains("komapperGenerator"))
        assertTrue(result.output.contains("komapperH2Generator"))
        assertTrue(result.output.contains("komapperMysqlGenerator"))
        assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
    }

    @Test
    fun dryRun() {
        buildFile.writeText(
            """
            plugins { id("org.komapper.gradle") }
            komapper {
                generators {
                    register("h2") {
                    }
                    register("mysql") {
                    }
                }
            }
            """.trimIndent()
        )
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("komapperGenerator", "--dry-run", "--stacktrace")
            .withPluginClasspath()
            .build()
        println(result.output)
        assertTrue(result.output.contains("komapperGenerator"))
        assertTrue(result.output.contains("komapperH2Generator"))
        assertTrue(result.output.contains("komapperMysqlGenerator"))
    }
}
