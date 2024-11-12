/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

import org.intellij.lang.annotations.Language
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.date
import org.jetbrains.changelog.markdownToHTML
import org.jetbrains.gradle.ext.settings
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("java")
  alias(libs.plugins.kotlin)
  alias(libs.plugins.intelliJPlatform)
  alias(libs.plugins.changelog)
  alias(libs.plugins.idea.ext)
  id("license-management")
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

// Set the JVM language level used to build the project.
kotlin {
  jvmToolchain(17)

  compilerOptions {
    jvmTarget = JvmTarget.fromTarget("17")
    // Supported version from https://plugins.jetbrains.com/docs/intellij/kotlin.html#kotlin-standard-library
    apiVersion = KotlinVersion.KOTLIN_1_9
    languageVersion = KotlinVersion.KOTLIN_1_9

    optIn.add("kotlinx.coroutines.ExperimentalCoroutinesApi")
  }
}

// Configure project's dependencies
repositories {
  mavenCentral()

  // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
  intellijPlatform {
    defaultRepositories()
  }
}

// Dependencies are managed with Gradle version catalog - read more: https://docs.gradle.org/current/userguide/platforms.html#sub:version-catalog
dependencies {
  testImplementation(libs.junit)

  // IntelliJ Platform Gradle Plugin Dependencies Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html
  intellijPlatform {
    create(providers.gradleProperty("platformType"), providers.gradleProperty("platformVersion"))

    instrumentationTools()
    pluginVerifier()
    zipSigner()
    testFramework(TestFrameworkType.Platform)
  }
}

// Configure IntelliJ Platform Gradle Plugin - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-extension.html
intellijPlatform {
  pluginConfiguration {
    id = providers.gradleProperty("pluginGroup")
    name = providers.gradleProperty("pluginName")
    version = providers.gradleProperty("pluginVersion")

    // Extract the <!-- Plugin description --> section from README.md and provide for the plugin's manifest
    description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
      val start = "<!-- Plugin description -->"
      val end = "<!-- Plugin description end -->"

      with(it.lines()) {
        if (!containsAll(listOf(start, end))) {
          throw GradleException("Plugin description section not found in README.md:\n$start ... $end")
        }
        subList(indexOf(start) + 1, indexOf(end)).joinToString("\n").let(::markdownToHTML)
      }
    }

    val changelog = project.changelog // local variable for configuration cache compatibility
    // Get the latest available change notes from the changelog file
    changeNotes = providers.gradleProperty("pluginVersion").map { pluginVersion ->
      with(changelog) {
        renderItem(
          (getOrNull(pluginVersion) ?: getUnreleased())
            .withHeader(false)
            .withEmptySections(false),
          Changelog.OutputType.HTML,
        )
      }
    }

    ideaVersion {
      sinceBuild = providers.gradleProperty("pluginSinceBuild")
      untilBuild = provider { null } // Disable upper bound
    }

    vendor {
      name = providers.gradleProperty("pluginVendor")
      url = providers.gradleProperty("pluginRepositoryUrl")
    }
  }

  signing {
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
    privateKey = providers.environmentVariable("PRIVATE_KEY")
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
  }

  publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN")
    // The pluginVersion is based on the SemVer (https://semver.org) and supports pre-release labels, like 2.1.7-alpha.3
    // Specify pre-release label to publish the plugin in a custom Release Channel automatically. Read more:
    // https://plugins.jetbrains.com/docs/intellij/deployment.html#specifying-a-release-channel
    channels = providers.gradleProperty("pluginVersion")
      .map { listOf(it.substringAfter('-', "").substringBefore('.').ifEmpty { "default" }) }
  }

  pluginVerification {
    ides {
      recommended()
    }
  }

  buildSearchableOptions = false
}

// Configure Gradle Changelog Plugin - read more: https://github.com/JetBrains/gradle-changelog-plugin
changelog {
  path = "${rootProject.projectDir}/CHANGELOG.md"
  repositoryUrl = providers.gradleProperty("pluginRepositoryUrl")
  itemPrefix = "-"
  version = providers.gradleProperty("pluginVersion")
  keepUnreleasedSection = true
  unreleasedTerm = "[Next]"
  header = provider { "[${version.get()}] - ${date()}" }
  groups = emptyList()
  // groups.set(listOf("Added", "Changed", "Deprecated", "Removed", "Fixed", "Security"))
}

tasks {
  jar {
    from("LICENSE")
  }
  buildPlugin {
    from("LICENSE")
  }

  val listProductsReleases by registering() {
    dependsOn(printProductsReleases)
    val outputF = layout.buildDirectory.file("listProductsReleases.txt").also {
      outputs.file(it)
    }
    val content = printProductsReleases.flatMap { it.productsReleases }.map { it.joinToString("\n") }

    doLast {
      outputF.orNull?.asFile?.writeText(content.get())
    }
  }

  // Latest available EAP release
  // https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-faq.html#how-to-check-the-latest-available-eap-release
  printProductsReleases {
    channels = listOf(ProductRelease.Channel.EAP)
    types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
    untilBuild = provider { null }

    doLast {
      productsReleases.get().max()
    }
  }

  publishPlugin {
    dependsOn(patchChangelog)
  }

  // About Daemon JVM Criteria: https://docs.gradle.org/current/userguide/gradle_daemon.html#sec:daemon_jvm_criteria
  updateDaemonJvm {
    jvmVersion = JavaLanguageVersion.of(21)
  }
}

idea {
  module {
    isDownloadSources = true
    isDownloadJavadoc = true
  }

  project?.settings {
//    taskTriggers {
//      // Tell IDE to execute task
//      afterSync(":task")
//    }
  }
}

tasks.register<GenerateDarkIconVariant>("patchSVG") {
  forceDarkSync = true
}
abstract class GenerateDarkIconVariant @Inject constructor(project: Project) : DefaultTask() {

  @InputFiles
  val svgFiles = project.fileTree(project.file("src/main/resources/icons")) {
    exclude("**/rectangle.svg", "**/base.svg")
    include("**/*.svg")
  }

  @OutputFiles
  val patchedFiles = svgFiles + svgFiles.files.map {
    it.parentFile.resolve(it.name.replace(".svg", "_dark.svg"))
  }.filter {
    it.exists()
  }

  @Input
  val forceDarkSync = project.objects.property<Boolean>().convention(false)

  // TODO ensure width rules per folder / file
  //  - actions: 16
  //  - filetypes: 16
  //  - toolwindows: 13, newUI 20
  //  - editorgutter: 12
  //  - bookmarks: 12

  @TaskAction
  fun run() {
    svgFiles.filter {
      val darkVariant = it.parentFile.resolve(it.name.replace(".svg", "_dark.svg"))
      !it.name.endsWith("_dark.svg") && (!svgFiles.contains(darkVariant) || forceDarkSync.get())
    }.forEach { svgFile ->
      logger.lifecycle(svgFile.path)
      val svgContent = svgFile.bufferedReader().readText()

      // Light SVG
      patchContent(svgContent, "#6E6E6E", "black").also {
        svgFile.parentFile.resolve("${svgFile.nameWithoutExtension}.svg").writeText(it)
      }
      // Dark SVG
      patchContent(svgContent, "#AFB1B3", "white").also {
        svgFile.parentFile.resolve("${svgFile.nameWithoutExtension}_dark.svg").writeText(it)
      }
    }
  }

  private fun patchContent(svgContent: String, screenColor: String, windowColor: String): String {
    val screenShapeFillRegex = "(?s)<rect.+?id=\"(?<id>[^\"]+?)\".*?fill=\"(?<fill>[^\"]+?)\""
    val screenShapeStrokeRegex = "(?s)<rect.+?id=\"(?<id>[^\"]+?)\".*?stroke=\"(?<stroke>[^\"]+?)\""
    val otherShapesFillRegex =
      "(?s)<(?<shape>rect|circle|ellipse|path|line|polyline|polygon).+?id=\"(?<id>[^\"]+?)\".*?fill=\"(?<fill>[^\"]+?)\""
    val otherShapesStrokeRegex =
      "(?s)<(?<shape>rect|circle|ellipse|path|line|polyline|polygon).+?id=\"(?<id>[^\"]+?)\".*?stroke=\"(?<stroke>[^\"]+?)\""
    val buildString = buildString {
      append(svgContent)
      patchAllFillAttributeWith(
        regex = screenShapeFillRegex,
        replacement = "fill" to screenColor,
        include = mapOf("id" to setOf("screen"))
      )
      patchAllFillAttributeWith(
        regex = screenShapeStrokeRegex,
        replacement = "stroke" to screenColor,
        include = mapOf("id" to setOf("screen"))
      )
      patchAllFillAttributeWith(
        regex = otherShapesFillRegex,
        replacement = "fill" to windowColor,
        include = mapOf("shape" to setOf("rect", "circle", "ellipse", "path", "line", "polyline", "polygon")),
        exclude = mapOf(
          "id" to setOf("screen"),
          "fill" to setOf("none")
        )
      )
      patchAllFillAttributeWith(
        regex = otherShapesStrokeRegex,
        replacement = "stroke" to windowColor,
        include = mapOf("shape" to setOf("rect", "circle", "ellipse", "path", "line", "polyline", "polygon")),
        exclude = mapOf(
          "id" to setOf("screen"),
          "stroke" to setOf("none")
        )
      )
    }
    return buildString
  }

  /**
   * Stick man's SVG patcher as regular XML API (DOM, StAX) re-order attributes.
   *
   * XML standard allows this because attribute order is irrelevant, however,
   * when patching a document it does because it may have been manually ordered by a human.
   */
  private fun StringBuilder.patchAllFillAttributeWith(
    @Language("RegExp")
    regex: String,
    replacement: Pair<String, String>,
    include: Map<String, Set<String>> = emptyMap(),
    exclude: Map<String, Set<String>> = emptyMap(),
  ) {

    Regex(regex)
      .findAll(this)
      .mapNotNull {
        val containsGroupValue: (Map.Entry<String, Set<String>>) -> Boolean = containsPredicate@{ (k, valueSet) ->
          val group = it.groups[k] ?: return@containsPredicate false
          valueSet.contains(group.value)
        }
        if (exclude.any(containsGroupValue)) {
          return@mapNotNull null
        }
        if (include.none(containsGroupValue)) {
          return@mapNotNull null
        }

        // if (exclude != null && it.groups[exclude.first]?.value == exclude.second) {
        //   return@mapNotNull null
        // }
        // if (include != null && it.groups[include.first]?.value != include.second) {
        //   return@mapNotNull null
        // }
        it.groups[replacement.first]
      }
      .toList()
      .reversed() // THis will make replacements from last to first to avoid skewing int ranges.
      .forEach {
        logger.debug("match to be changed : $it")
        replace(it.range.first, it.range.last + 1, replacement.second)
      }
  }
}