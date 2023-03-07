/*
 * Copyright 2017 the original author or authors.
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

package org.gradle.performance.regression.android

import org.gradle.integtests.fixtures.AvailableJavaHomes
import org.gradle.integtests.fixtures.versions.AndroidGradlePluginVersions
import org.gradle.performance.AbstractCrossVersionPerformanceTest
import org.gradle.performance.annotations.RunFor
import org.gradle.performance.annotations.Scenario
import org.gradle.performance.fixture.AndroidTestProject
import org.gradle.profiler.BuildMutator
import org.gradle.profiler.InvocationSettings
import org.gradle.profiler.ScenarioContext

import static org.gradle.performance.annotations.ScenarioType.PER_COMMIT
import static org.gradle.performance.results.OperatingSystem.LINUX

@RunFor(
    @Scenario(type = PER_COMMIT, operatingSystems = [LINUX], testProjects = ["largeAndroidBuild", "santaTrackerAndroidBuild"])
)
class RealLifeAndroidStudioPerformanceTest extends AbstractCrossVersionPerformanceTest implements AndroidPerformanceTestFixture {

    /**
     * To run this test locally you should have Android Studio installed in /Applications/Android Studio.*.app folder,
     * or you should set "studioHome" system property with the Android Studio installation path,
     * or you should to enable automatic download of Android Studio run test with -PautoDownloadAndroidStudio=true.
     *
     * Additionaly test needs also to have ANDROID_SDK_ROOT set with Android SDK (normally on MacOS it's installed in "$HOME/Library/Android/sdk")
     *
     * To enable headless mode run with -PrunAndroidStudioInHeadlessMode=true.
     */
    def "run Android Studio sync"() {
        given:
        runner.args = [AndroidGradlePluginVersions.OVERRIDE_VERSION_CHECK]
        def testProject = AndroidTestProject.projectFor(runner.testProject)
        testProject.configure(runner)
        AndroidTestProject.useStableAgpVersion(runner)
        AndroidTestProject.useStableKotlinVersion(runner)
        runner.warmUpRuns = 20
        runner.runs = 20
        // AGP 7.3 requires Gradle 7.4
        runner.minimumBaseVersion = "7.4"
        runner.setupAndroidStudioSync()
        configureProjectJavaHomeToJdk11()
        configureLocalProperties()

        when:
        def result = runner.run()

        then:
        result.assertCurrentVersionHasNotRegressed()
    }

    void configureLocalProperties() {
        if (System.getenv("ANDROID_SDK_ROOT") == null) {
            throw new RuntimeException("ANDROID_SDK_ROOT must be set for this test")
        }
        String androidSdkRootPath = System.getenv("ANDROID_SDK_ROOT")
        runner.addBuildMutator { invocation -> new LocalPropertiesMutator(invocation, androidSdkRootPath) }
    }

    static class LocalPropertiesMutator implements BuildMutator {
        private final String androidSdkRootPath
        private final InvocationSettings invocation

        LocalPropertiesMutator(InvocationSettings invocation, String androidSdkRootPath) {
            this.invocation = invocation
            this.androidSdkRootPath = androidSdkRootPath
        }

        @Override
        void beforeScenario(ScenarioContext context) {
            def localProperties = new File(invocation.projectDir, "local.properties")
            localProperties << "\nsdk.dir=${androidSdkRootPath.replace("\\", "/")}\n"
        }
    }
}
