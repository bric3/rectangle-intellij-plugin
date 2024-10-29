/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

plugins {
  id("dev.yumi.gradle.licenser")
}

license {
  rule(rootProject.file("HEADER"))

  include(
    "**/*.java",
    "**/*.kt",
    "**/*.kts",
    "**/*.properties",
    "**/*.xml",
  )
}

tasks {
  named("classes") {
    finalizedBy(applyLicenses)
  }
}