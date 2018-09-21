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
package org.gradle.integtests.samples.dependencymanagement

import org.gradle.integtests.fixtures.AbstractSampleIntegrationTest
import org.gradle.integtests.fixtures.Sample
import org.gradle.integtests.fixtures.UsesSample
import org.gradle.util.Requires
import org.junit.Rule
import spock.lang.Unroll

import static org.gradle.util.TestPrecondition.KOTLIN_SCRIPT

@Requires(KOTLIN_SCRIPT)
class SamplesComponentSelectionRulesIntegrationTest extends AbstractSampleIntegrationTest {

    @Rule
    Sample sample = new Sample(testDirectoryProvider)

    @Unroll
    @UsesSample("userguide/dependencyManagement/customizingResolution/selectionRule")
    def "can run resolveConfiguration sample with #dsl dsl"() {
        executer.inDirectory(sample.dir.file(dsl))
        if(expectedDeprecationWarnings) executer.expectDeprecationWarnings(expectedDeprecationWarnings)

        when:
        run "resolveConfiguration"

        then:
        output.contains "Rejected version: 1.5"
        output.contains "Rejected version: 1.4"
        output.contains "** Accepted version: 1.3.0"

        where:
        dsl      | expectedDeprecationWarnings
        'groovy' | 0
        'kotlin' | 1
    }

    @Unroll
    @UsesSample("userguide/dependencyManagement/customizingResolution/selectionRule")
    def "can run reject sample with #dsl dsl"() {
        executer.inDirectory(sample.dir.file(dsl))
        if(expectedDeprecationWarnings) executer.expectDeprecationWarnings(expectedDeprecationWarnings)

        when:
        run "printRejectConfig"

        then:
        output.contains "Resolved: api-1.4.jar"

        where:
        dsl      | expectedDeprecationWarnings
        'groovy' | 0
        'kotlin' | 1
    }

    @Unroll
    @UsesSample("userguide/dependencyManagement/customizingResolution/selectionRule")
    def "can run metadata rules sample with #dsl dsl"() {
        executer.inDirectory(sample.dir)
        if(expectedDeprecationWarnings) executer.expectDeprecationWarnings(expectedDeprecationWarnings)

        when:
        run "printMetadataRulesConfig"

        then:
        output.contains "Resolved: api-1.3.0.jar"
        output.contains "Resolved: lib-1.9.jar"

        where:
        dsl      | expectedDeprecationWarnings
        'groovy' | 0
        'kotlin' | 0
    }

    @Unroll
    @UsesSample("userguide/dependencyManagement/customizingResolution/selectionRule")
    def "can run targeted rule sample with #dsl dsl"() {
        executer.inDirectory(sample.dir.file(dsl))
        if(expectedDeprecationWarnings) executer.expectDeprecationWarnings(expectedDeprecationWarnings)

        when:
        run "printTargetConfig"

        then:
        output.contains "Resolved: api-1.4.jar"

        where:
        dsl      | expectedDeprecationWarnings
        'groovy' | 0
        'kotlin' | 1
    }

    @Unroll
    @UsesSample("userguide/dependencyManagement/customizingResolution/selectionRule")
    def "can run rules source sample with #dsl dsl"() {
        executer.inDirectory(sample.dir.file(dsl))
        if(expectedDeprecationWarnings) executer.expectDeprecationWarnings(expectedDeprecationWarnings)

        when:
        run "printRuleSourceConfig"

        then:
        output.contains "Resolved: api-1.4.jar"

        where:
        dsl      | expectedDeprecationWarnings
        'groovy' | 0
        'kotlin' | 1
    }
}
