/*
 * Copyright 2013 the original author or authors.
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

package org.gradle.buildinit.plugins.internal;

import org.gradle.api.internal.DocumentationRegistry;
import org.gradle.api.internal.file.FileResolver;

public class GroovyLibraryProjectInitDescriptor extends GroovyProjectInitDescriptor {

    public GroovyLibraryProjectInitDescriptor(BuildScriptBuilderFactory scriptBuilderFactory, TemplateOperationFactory templateOperationFactory, FileResolver fileResolver, TemplateLibraryVersionProvider libraryVersionProvider, DocumentationRegistry documentationRegistry) {
        super(scriptBuilderFactory, templateOperationFactory, fileResolver, libraryVersionProvider, documentationRegistry);
    }

    @Override
    public String getId() {
        return "groovy-library";
    }

    @Override
    protected TemplateOperation sourceTemplateOperation(InitSettings settings) {
        return fromClazzTemplate("groovylibrary/Library.groovy.template", settings, "main");
    }

    @Override
    protected TemplateOperation testTemplateOperation(InitSettings settings) {
        return fromClazzTemplate("groovylibrary/LibraryTest.groovy.template", settings, "test");
    }
}
