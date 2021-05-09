package org.komapper.gradle

import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class KomapperPluginTest {

    @JvmField
    @TempDir
    var testProjectDir: File? = null
    private lateinit var settingsFile: File
    private lateinit var buildFile: File

    @BeforeEach
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
            komapperCodeGen {
            }
            """.trimIndent()
        )
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("tasks", "--all")
            .withPluginClasspath()
            .build()
        println(result.output)
        assertTrue(result.output.contains("komapperCodeGen"))
        assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, result.task(":tasks")?.outcome)
    }

    @Test
    fun komapperGenerate() {
        buildFile.writeText(
            """
            import org.komapper.core.*
            import org.komapper.core.dsl.*
            plugins { id("org.komapper.gradle") }
            komapperCodeGen {
                val db = Database.create("jdbc:h2:mem:example;DB_CLOSE_DELAY=-1")
                db.runQuery {
                    ScriptDsl.execute(""${'"'}
                        drop all objects;
                        create table department(
                            department_id integer not null primary key, 
                            department_no integer not null,
                            department_name varchar(20),
                            location varchar(20) default 'tokyo', 
                            version integer
                         );
                        create table employee(
                            employee_id integer not null primary key, 
                            employee_no integer not null,
                            employee_name varchar(20)
                        );
                    ""${'"'}.trimIndent())
                }
                database.set(db)
            }
            """.trimIndent()
        )
        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withArguments("komapperCodeGen", "--stacktrace")
            .withPluginClasspath()
            .build()
        println(result.output)
        assertEquals(org.gradle.testkit.runner.TaskOutcome.SUCCESS, result.task(":komapperCodeGen")?.outcome)
        testProjectDir!!.toPath().resolve("src/main/kotlin/entities.kt").toFile().readText().let {
            println(it)
        }
        testProjectDir!!.toPath().resolve("src/main/kotlin/entityDefinitions.kt").toFile().readText().let {
            println(it)
        }
    }
}
