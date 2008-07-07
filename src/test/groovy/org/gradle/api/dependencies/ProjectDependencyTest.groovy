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

package org.gradle.api.dependencies

import java.awt.Point
import org.apache.ivy.core.module.descriptor.DependencyDescriptor
import org.apache.ivy.core.module.id.ModuleId
import org.apache.ivy.core.module.id.ModuleRevisionId
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Project
import org.gradle.api.internal.dependencies.DefaultDependencyManager
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.UnknownDependencyNotation
import org.gradle.util.HelperUtil
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*
import org.gradle.util.JUnit4GroovyMockery
import org.gradle.api.DependencyManager
import org.gradle.api.Task

/**
 * @author Hans Dockter
 */
class ProjectDependencyTest {
    static final String TEST_CONF = "conf"
    static final Set TEST_CONF_SET = [TEST_CONF]
    static final String TEST_DEPENDENCY_CONF = "depconf"

    ProjectDependency projectDependency
    DefaultProject project
    DefaultProject dependencyProject
    ModuleRevisionId dependencyProjectModuleRevisionId
    String dependencyProjectArtifactProductionTaskName

    @Before public void setUp() {
        project = HelperUtil.createRootProject(new File('root'))

        dependencyProjectModuleRevisionId = new ModuleRevisionId(new ModuleId('org', 'otherproject'), '1.0')
        dependencyProjectArtifactProductionTaskName = 'somename'
        DefaultDependencyManager mockDependencyManager = [createModuleRevisionId: {dependencyProjectModuleRevisionId},
                getArtifactProductionTaskName: {dependencyProjectArtifactProductionTaskName}] as DefaultDependencyManager
        dependencyProject = HelperUtil.createRootProject(new File('dependency'))
        dependencyProject.dependencies = mockDependencyManager
        dependencyProject.createTask(dependencyProjectArtifactProductionTaskName)

        projectDependency = new ProjectDependency(TEST_CONF_SET, dependencyProject, project)
    }

    @Test public void testProjectDependencyStringObjectProject() {
        assertEquals(Dependency.DEFAULT_CONFIGURATION, projectDependency.dependencyConfiguration)
        assertEquals(TEST_CONF_SET, projectDependency.confs)
        assert dependencyProject.is(projectDependency.userDependencyDescription)
        assertEquals(project, projectDependency.project)
    }

    @Test public void testProjectDependencyProjectString() {
        projectDependency = new ProjectDependency(dependencyProject, TEST_DEPENDENCY_CONF)
        assertEquals(TEST_DEPENDENCY_CONF, projectDependency.dependencyConfiguration)
        assert projectDependency.userDependencyDescription.is(dependencyProject)
        assert !projectDependency.confs
        assertNull projectDependency.project
    }

    @Test (expected = UnknownDependencyNotation) public void testWithSingleString() {
        new ProjectDependency(TEST_CONF_SET, "string", project)
    }

    @Test (expected = UnknownDependencyNotation) public void testWithUnknownType() {
        new ProjectDependency(TEST_CONF_SET, new Point(3, 4), project)
    }

    @Test public void testCreateDependencyDescriptor() {
        projectDependency.dependencyConfiguration = TEST_DEPENDENCY_CONF
        DependencyDescriptor dependencyDescriptor = projectDependency.createDepencencyDescriptor()
        assertEquals(dependencyProjectModuleRevisionId, dependencyDescriptor.dependencyRevisionId)
        assertEquals(1, dependencyDescriptor.getDependencyConfigurations(TEST_CONF).size())
        assertEquals(TEST_DEPENDENCY_CONF, dependencyDescriptor.getDependencyConfigurations(TEST_CONF)[0])
        assertTrue(dependencyDescriptor.isChanging())
    }

    @Test public void testInitialize() {
        JUnit4GroovyMockery context = new JUnit4GroovyMockery()
        JUnit4GroovyMockery context2 = new JUnit4GroovyMockery()
        Project project = context.mock(Project)
        DependencyManager dependencyManager = context.mock(DependencyManager)
        Task task = context.mock(Task)
        Map conf2Tasks = [:]
        Project dependencyProject = context2.mock(Project)
        DependencyManager dependencyProjectDependencyManager = context2.mock(DependencyManager)
        Task expectedArtifactProductionTask = context2.mock(Task)
        String expectedArtifactProductionTaskName = "artifactTask"
        String expectedArtifactProductionTaskPath = "artifactTaskPath"

        projectDependency.project = project
        projectDependency.userDependencyDescription = dependencyProject
        conf2Tasks[TEST_CONF] = [TEST_CONF]

        context.checking {
            allowing(project).getDependencies(); will(returnValue(dependencyManager))
            allowing(dependencyManager).getConf2Tasks(); will(returnValue(conf2Tasks))
            allowing(project).task(TEST_CONF); will(returnValue(task))
            one(task).dependsOn(expectedArtifactProductionTaskPath)
        }

        context2.checking {
            one(dependencyProject).evaluate()
            allowing(dependencyProject).getDependencies(); will(returnValue(dependencyProjectDependencyManager))
            allowing(dependencyProjectDependencyManager).getArtifactProductionTaskName(); will(
                returnValue(expectedArtifactProductionTaskName))
            allowing(dependencyProject).task(expectedArtifactProductionTaskName); will(returnValue(expectedArtifactProductionTask))
            allowing(expectedArtifactProductionTask).getPath(); will(returnValue(expectedArtifactProductionTaskPath))

        }

        projectDependency.initialize()
    }
}
