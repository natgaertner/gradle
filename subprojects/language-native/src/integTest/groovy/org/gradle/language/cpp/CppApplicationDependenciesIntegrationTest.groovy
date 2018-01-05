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

package org.gradle.language.cpp

import org.gradle.language.AbstractNativeProductionComponentDependenciesIntegrationTest

class CppApplicationDependenciesIntegrationTest extends AbstractNativeProductionComponentDependenciesIntegrationTest implements CppTaskNames {
    @Override
    protected void makeComponentWithLibrary() {
        buildFile << """
            apply plugin: 'cpp-application'
            project(':lib') {
                apply plugin: 'cpp-library'
            }
"""
        file("lib/src/main/cpp/lib.cpp") << """
            void lib_func() { }
"""
        file("src/main/cpp/app.cpp") << """
            int main() {
                return 0;
            }
"""
    }

    @Override
    protected String getComponentUnderTestDsl() {
        return "application"
    }

    @Override
    protected List<String> getAssembleDebugTasks() {
        return [':compileDebugCpp', ':linkDebug', ':installDebug']
    }

    @Override
    protected List<String> getAssembleReleaseTasks() {
        return [':compileReleaseCpp', ':linkRelease', ':installRelease'] + extractAndStripSymbolsTasksRelease(toolChain)
    }

    @Override
    protected List<String> getLibDebugTasks() {
        return [':lib:compileDebugCpp', ':lib:linkDebug']
    }
}