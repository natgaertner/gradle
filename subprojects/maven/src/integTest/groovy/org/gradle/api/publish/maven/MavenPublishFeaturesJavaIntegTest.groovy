/*
 * Copyright 2019 the original author or authors.
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

package org.gradle.api.publish.maven


import spock.lang.Unroll

class MavenPublishFeaturesJavaIntegTest extends AbstractMavenPublishFeaturesJavaIntegTest {

    def "can publish java-library with a feature"() {
        mavenRepo.module('org', 'optionaldep', '1.0').withModuleMetadata().publish()

        given:
        buildFile << """
            configurations {
                optionalFeatureImplementation
                optionalFeatureRuntimeElements {
                    extendsFrom optionalFeatureImplementation
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME_JARS))
                    }
                    outgoing.capability("org:optional-feature:\${version}")
                }
                compileClasspath.extendsFrom(optionalFeatureImplementation)
            }
            
            dependencies {
                optionalFeatureImplementation 'org:optionaldep:1.0'
            }
            
            components.java.addVariantsFromConfiguration(configurations.optionalFeatureRuntimeElements, { it.mapToMavenScope('compile', true) })
        """

        when:
        run "publish"

        then:
        javaLibrary.parsedModuleMetadata.variant("apiElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("runtimeElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("optionalFeatureRuntimeElements") {
            dependency('org', 'optionaldep', '1.0')
            noMoreDependencies()
        }
        javaLibrary.parsedPom.scope('compile') {
            assertOptionalDependencies('org:optionaldep:1.0')
        }
        javaLibrary.parsedPom.hasNoScope('runtime')

        and:
        resolveArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveApiArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveRuntimeArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }

        resolveRuntimeArtifacts(javaLibrary) {
            optionalFeatureCapabilities << "org:optional-feature:1.0"
            withModuleMetadata {
                expectFiles "publishTest-1.9.jar", "optionaldep-1.0.jar"
            }
            withoutModuleMetadata {
                shouldFail {
                    // documents the current behavior
                    assertHasCause("Unable to find a variant of org.gradle.test:publishTest:1.9 providing the requested capability org:optional-feature:1.0")
                }
            }
        }
    }

    def "can group dependencies by feature"() {
        mavenRepo.module('org', 'optionaldep-g1', '1.0').publish()
        mavenRepo.module('org', 'optionaldep1-g2', '1.0').publish()
        mavenRepo.module('org', 'optionaldep2-g2', '1.0').publish()

        given:
        buildFile << """
            configurations {
                optionalFeature1Implementation
                optionalFeature1RuntimeElements {
                    extendsFrom optionalFeature1Implementation
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME_JARS))
                    }
                    outgoing.capability("org:optional-feature1:\${version}")
                }
                compileClasspath.extendsFrom(optionalFeature1Implementation)
                
                optionalFeature2Implementation
                optionalFeature2RuntimeElements {
                    extendsFrom optionalFeature2Implementation
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME_JARS))
                    }
                    outgoing.capability("org:optional-feature2:\${version}")
                }
                compileClasspath.extendsFrom(optionalFeature2Implementation)
            }
            
            dependencies {
                optionalFeature1Implementation 'org:optionaldep-g1:1.0'
                optionalFeature2Implementation 'org:optionaldep1-g2:1.0'
                optionalFeature2Implementation 'org:optionaldep2-g2:1.0'
            }
            
            components.java.addVariantsFromConfiguration(configurations.optionalFeature1RuntimeElements, { it.mapToMavenScope('compile', true) })
            components.java.addVariantsFromConfiguration(configurations.optionalFeature2RuntimeElements, { it.mapToMavenScope('compile', true) })
        """

        when:
        run "publish"

        then:
        javaLibrary.parsedModuleMetadata.variant("apiElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("runtimeElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("optionalFeature1RuntimeElements") {
            dependency('org', 'optionaldep-g1', '1.0')
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("optionalFeature2RuntimeElements") {
            dependency('org', 'optionaldep1-g2', '1.0')
            dependency('org', 'optionaldep2-g2', '1.0')
            noMoreDependencies()
        }
        javaLibrary.parsedPom.scope('compile') {
            assertOptionalDependencies(
                    'org:optionaldep-g1:1.0',
                    'org:optionaldep1-g2:1.0',
                    'org:optionaldep2-g2:1.0')
        }
        javaLibrary.parsedPom.hasNoScope('runtime')

        and:
        resolveArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveApiArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveRuntimeArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
    }

    @Unroll("publish java-library with feature with additional artifact #id (#optionalFeatureFileName)")
    def "publish java-library with feature with additional artifact"() {
        mavenRepo.module('org', 'optionaldep', '1.0').withModuleMetadata().publish()

        given:
        buildFile << """
            configurations {
                optionalFeatureImplementation
                optionalFeatureRuntimeElements {
                    extendsFrom optionalFeatureImplementation
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME_JARS))
                    }
                    outgoing.capability("org:optional-feature:\${version}")
                }
                compileClasspath.extendsFrom(optionalFeatureImplementation)
            }
            
            dependencies {
                optionalFeatureImplementation 'org:optionaldep:1.0'
            }
            
            components.java.addVariantsFromConfiguration(configurations.optionalFeatureRuntimeElements, { it.mapToMavenScope('compile', true) })
            
            artifacts {
                optionalFeatureRuntimeElements file:file("\$buildDir/$optionalFeatureFileName"), builtBy:'touchFile'
            }
            
            task touchFile {
                doLast {
                    file("\$buildDir/$optionalFeatureFileName") << "test"
                }
            }
        """

        when:
        if (failureText) {
            fails "publish"
        } else {
            run "publish"
        }

        then:
        if (failureText) {
            failure.assertHasCause(failureText)
        } else {
            javaLibrary.withClassifiedArtifact("optional-feature", "jar")
            javaLibrary.mavenModule.assertArtifactsPublished(
                    "publishTest-1.9.jar" ,
                    optionalFeatureFileName ,
                    "publishTest-1.9.pom",
                    "publishTest-1.9.module")
            javaLibrary.parsedModuleMetadata.variant("apiElements") {
                assert files*.name == ["publishTest-1.9.jar"]
                noMoreDependencies()
            }
            javaLibrary.parsedModuleMetadata.variant("runtimeElements") {
                assert files*.name == ["publishTest-1.9.jar"]
                noMoreDependencies()
            }
            javaLibrary.parsedModuleMetadata.variant("optionalFeatureRuntimeElements") {
                assert files*.name == [optionalFeatureFileName]
                dependency('org', 'optionaldep', '1.0')
                noMoreDependencies()
            }
            javaLibrary.parsedPom.scope('compile') {
                assertOptionalDependencies('org:optionaldep:1.0')
            }
            javaLibrary.parsedPom.hasNoScope('runtime')

            resolveRuntimeArtifacts(javaLibrary) {
                optionalFeatureCapabilities << "org:optional-feature:1.0"
                withModuleMetadata {
                    expectFiles "publishTest-1.9.jar", "optionaldep-1.0.jar", optionalFeatureFileName
                }
                withoutModuleMetadata {
                    shouldFail {
                        // documents the current behavior
                        assertHasCause("Unable to find a variant of org.gradle.test:publishTest:1.9 providing the requested capability org:optional-feature:1.0")
                    }
                }
            }
        }

        where:
        id                       | optionalFeatureFileName                | failureText
        "with a classifier"      | "publishTest-1.9-optional-feature.jar" | null
        "with an arbitrary name" | "optional-feature-1.9.jar"             | "Invalid publication 'maven': multiple artifacts with the identical extension and classifier ('jar', 'null')"
        "with the same name "    | "publishTest-1.9.jar"                  | "Invalid publication 'maven': multiple artifacts with the identical extension and classifier ('jar', 'null')"

    }

    def "can publish java-library with a feature from a configuration with more than one outgoing variant"() {
        mavenRepo.module('org', 'optionaldep', '1.0').withModuleMetadata().publish()

        given:
        buildFile << """
            configurations {
                optionalFeatureImplementation
                optionalFeatureRuntimeElements {
                    extendsFrom optionalFeatureImplementation
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME_JARS))
                    }
                    outgoing.capability("org:optional-feature:\${version}")
                }
                compileClasspath.extendsFrom(optionalFeatureImplementation)
            }
            
            dependencies {
                optionalFeatureImplementation 'org:optionaldep:1.0'
            }
            
            components.java.addVariantsFromConfiguration(configurations.optionalFeatureRuntimeElements, { it.mapToMavenScope('compile', true) })
            
            def alt = configurations.optionalFeatureRuntimeElements.outgoing.variants.create("alternate")
            alt.attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, 'java-runtime-alt'))
            }
            def altFile = file("\${buildDir}/\${name}-\${version}-alt.jar")
            task createFile { doFirst { altFile.parentFile.mkdirs(); altFile.text = "test file" } }
            alt.artifact(file:altFile, builtBy: 'createFile')
            
        """

        when:
        run "publish"

        then:
        javaLibrary.parsedModuleMetadata.variant("apiElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("runtimeElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("optionalFeatureRuntimeElements") {
            dependency('org', 'optionaldep', '1.0')
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("optionalFeatureRuntimeElementsAlternate") {
            dependency('org', 'optionaldep', '1.0')
            noMoreDependencies()
        }
        javaLibrary.parsedPom.scope('compile') {
            assertOptionalDependencies('org:optionaldep:1.0')
        }
        javaLibrary.parsedPom.hasNoScope('runtime')

        and:
        resolveArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveApiArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveRuntimeArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }

        resolveRuntimeArtifacts(javaLibrary) {
            optionalFeatureCapabilities << "org:optional-feature:1.0"
            withModuleMetadata {
                expectFiles "publishTest-1.9.jar", "optionaldep-1.0.jar"
            }
            withoutModuleMetadata {
                shouldFail {
                    // documents the current behavior
                    assertHasCause("Unable to find a variant of org.gradle.test:publishTest:1.9 providing the requested capability org:optional-feature:1.0")
                }
            }
        }
    }

    def "can publish java-library with a feature from a configuration with more than one outgoing variant and filter out variants"() {
        mavenRepo.module('org', 'optionaldep', '1.0').withModuleMetadata().publish()

        given:
        buildFile << """
            configurations {
                optionalFeatureImplementation
                optionalFeatureRuntimeElements {
                    extendsFrom optionalFeatureImplementation
                    canBeResolved = false
                    canBeConsumed = true
                    attributes {
                        attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, Usage.JAVA_RUNTIME_JARS))
                    }
                    outgoing.capability("org:optional-feature:\${version}")
                }
                compileClasspath.extendsFrom(optionalFeatureImplementation)
            }
            
            dependencies {
                optionalFeatureImplementation 'org:optionaldep:1.0'
            }
            
            components.java.addVariantsFromConfiguration(configurations.optionalFeatureRuntimeElements) {
                if (it.configurationVariant.name != 'alternate') {
                    it.skip()
                } else {
                    it.mapToMavenScope('compile', true)
                } 
            }
            
            def alt = configurations.optionalFeatureRuntimeElements.outgoing.variants.create("alternate")
            alt.attributes {
                attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, 'java-runtime-jars'))
            }
            def altFile = file("\${buildDir}/\${name}-\${version}-alt.jar")
            task createFile { doFirst { altFile.parentFile.mkdirs(); altFile.text = "test file" } }
            alt.artifact(file:altFile, builtBy: 'createFile')
            
        """

        when:
        run "publish"

        then:
        javaLibrary.parsedModuleMetadata.variant("apiElements") {
            noMoreDependencies()
        }
        javaLibrary.parsedModuleMetadata.variant("runtimeElements") {
            noMoreDependencies()
        }
        !javaLibrary.parsedModuleMetadata.variants.name.contains('optionalFeatureRuntimeElements')
        javaLibrary.parsedModuleMetadata.variant("optionalFeatureRuntimeElementsAlternate") {
            dependency('org', 'optionaldep', '1.0')
            files.name == ['publishTest-1.9-alt.jar']
            noMoreDependencies()
        }
        javaLibrary.parsedPom.scope('compile') {
            assertOptionalDependencies('org:optionaldep:1.0')
        }
        javaLibrary.parsedPom.hasNoScope('runtime')

        and:
        resolveArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveApiArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }
        resolveRuntimeArtifacts(javaLibrary) { expectFiles "publishTest-1.9.jar" }

        resolveRuntimeArtifacts(javaLibrary) {
            optionalFeatureCapabilities << "org:optional-feature:1.0"
            withModuleMetadata {
                // the first file comes from the fact the test fixture adds 2 dependencies (main component, optional feature)
                expectFiles "publishTest-1.9.jar", "publishTest-1.9-alt.jar", "optionaldep-1.0.jar"
            }
            withoutModuleMetadata {
                shouldFail {
                    // documents the current behavior
                    assertHasCause("Unable to find a variant of org.gradle.test:publishTest:1.9 providing the requested capability org:optional-feature:1.0")
                }
            }
        }
    }
}
