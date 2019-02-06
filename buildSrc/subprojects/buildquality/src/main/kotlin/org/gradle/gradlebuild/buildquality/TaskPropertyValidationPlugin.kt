/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.gradlebuild.buildquality

import accessors.java
import accessors.reporting
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.gradle.plugin.devel.tasks.ValidateTaskProperties


private
const val validateTaskName = "validateTaskProperties"


private
const val reportFileName = "task-properties/report.txt"


open class TaskPropertyValidationPlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = project.run {
        plugins.withType<JavaBasePlugin> {
            configurations.create("minimalRuntime") {
                isCanBeConsumed = false
                attributes.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
            }
            // TODO - Move this to a distribution project
            dependencies.add("minimalRuntime", dependencies.project(":core"))
            dependencies.add("minimalRuntime", dependencies.project(":dependencyManagement"))
            dependencies.add("minimalRuntime", dependencies.project(":platformJvm"))
            addValidateTask()
        }
    }
}


private
fun Project.addValidateTask() {
    val validateTask = tasks.register(validateTaskName, ValidateTaskProperties::class) {
        val main by java.sourceSets
        dependsOn(main.output)
        classes.setFrom(main.output.classesDirs)
        classpath.from(main.output) // to pick up resources too
        classpath.from(main.runtimeClasspath)
        classpath.from(configurations["minimalRuntime"])
        // TODO Should we provide a more intuitive way in the task definition to configure this property from Kotlin?
        outputFile.set(reporting.baseDirectory.file(reportFileName))
        failOnWarning = true
        enableStricterValidation = true
    }
    tasks.named("codeQuality").configure {
        dependsOn(validateTask)
    }
    tasks.withType(Test::class).configureEach {
        shouldRunAfter(validateTask)
    }
}
