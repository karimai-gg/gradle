/*
 * Copyright 2007 the original author or authors.
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

package org.gradle.api.internal.project

import org.gradle.api.DependencyManager
import org.gradle.api.DependencyManagerFactory
import org.gradle.api.Project
import org.gradle.api.internal.dependencies.DefaultDependencyManager
import org.gradle.util.HelperUtil
import static org.junit.Assert.*
import org.junit.Test;

/**
 * @author Hans Dockter
 */
class ProjectFactoryTest {
    @Test public void testProjectFactory() {
        DependencyManager dependencyManager = new DefaultDependencyManager()
        DependencyManagerFactory dependencyManagerFactory = [createDependencyManager: {dependencyManager}] as DependencyManagerFactory

        String expectedName = 'somename'
        String expectedBuildFileName = 'build.gradle'
        File rootDir = '/root' as File
        Project rootProject = HelperUtil.createRootProject(rootDir)
        Project parentProject = rootProject.addChildProject('parent')
        BuildScriptProcessor buildScriptProcessor = new BuildScriptProcessor()
        PluginRegistry expectedPluginRegistry = new PluginRegistry()
        ProjectRegistry expectedProjectRegistry = rootProject.getProjectRegistry()
        ClassLoader expectedBuildScriptClassLoader = new URLClassLoader([] as URL[])

        ProjectFactory projectFactory = new ProjectFactory(dependencyManagerFactory, buildScriptProcessor, expectedPluginRegistry,
                expectedBuildFileName, expectedProjectRegistry)
        Project project = projectFactory.createProject(expectedName, parentProject, rootDir, rootProject, expectedBuildScriptClassLoader)

        assert project.name.is(expectedName)
        assertEquals(expectedBuildFileName, project.buildFileName)
        assert project.parent.is(parentProject)
        assert project.rootDir.is(rootDir)
        assert project.rootProject.is(rootProject)
        assert project.buildScriptClassLoader.is(expectedBuildScriptClassLoader)
        assert project.projectFactory.is(projectFactory)
        assert project.dependencies.is(dependencyManager)
        assert project.buildScriptProcessor.is(buildScriptProcessor)
        assert project.pluginRegistry.is(expectedPluginRegistry)
        assert project.projectRegistry.is(expectedProjectRegistry)
    }

}
