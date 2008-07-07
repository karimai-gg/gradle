/*
 * Copyright 2007-2008 the original author or authors.
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
 
package org.gradle.api.plugins

import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.internal.project.PluginRegistry
import org.gradle.util.HelperUtil
import org.junit.Test

/**
 * @author Hans Dockter
 */
class WarPluginTest {
    @Test public void testApply() {
        // todo Make test stronger
        // This is a very weak test. But due to the dynamic nature of Groovy, it does help to find bugs.
        DefaultProject project = HelperUtil.createRootProject(new File('path', 'root'))
        WarPlugin warPlugin = new WarPlugin()
        warPlugin.apply(project, new PluginRegistry(), [:])
    }
}
