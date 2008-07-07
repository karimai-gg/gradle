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
 
package org.gradle.api.tasks.util

import org.junit.Before
import org.junit.Test
import static org.junit.Assert.*;

/**
 * @author Hans Dockter
 */
class FileCollectionTest {
    FileCollection fileCollection
    Set testFiles

    @Before public void setUp()  {
        testFiles = [new File('/file1'), new File('/file2')]
        fileCollection = new FileCollection(testFiles)
    }

    @Test public void testInit() {
        assertEquals(testFiles, fileCollection.files)
    }
    
    @Test public void testAddToAntBuilder() {
        fileCollection.addToAntBuilder(new AntBuilder())
    }
}
