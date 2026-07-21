import dev.detekt.gradle.Detekt
import org.gradle.kotlin.dsl.withType
import kotlin.jvm.java

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.androidx.room) apply false
    alias(libs.plugins.detekt) apply false
}

/*
    make detekt to only runs on staged files
    Sources: https://detekt.dev/docs/next/gettingstarted/git-pre-commit-hook#only-run-on-staged-files---gradle
 */

fun Project.getGitStagedFiles(rootDir: File): Provider<List<File>> {
    return providers.exec {
        commandLine("git", "--no-pager", "diff", "--name-only", "--cached")
    }.standardOutput.asText
        .map { outputText ->
            outputText.trim()
                .split("\n")
                .filter { it.isNotBlank() }
                .map { File(rootDir, it) }
        }
}

subprojects {
    pluginManager.withPlugin("dev.detekt") {
        tasks.withType<Detekt>().configureEach {

            exclude("**/build/generated/**")
            exclude("**/build/reports/**")

            if (project.hasProperty("precommit")) {
                val rootDir = project.rootDir
                val projectDir = projectDir

                val fileCollection = files()

                setSource(
                    getGitStagedFiles(rootDir)
                        .map { stagedFiles ->
                            val stagedFilesFromThisProject = stagedFiles
                                .filter { it.startsWith(projectDir) }

                            fileCollection.setFrom(*stagedFilesFromThisProject.toTypedArray())

                            fileCollection.asFileTree
                        }
                )

            }
        }
    }
}



afterEvaluate {
    tasks.withType(Detekt::class.java).configureEach {
        val typeResolutionEnabled = !classpath.isEmpty
        if (typeResolutionEnabled && project.hasProperty("precommit")) {
            // We must exclude kts files from pre-commit hook to prevent detekt from crashing
            // This is a workaround for the https://github.com/detekt/detekt/issues/5501
            exclude("*.gradle.kts")
        }
    }
}
