/*******************************************************************************
 * (c) Copyright IBM Corporation 2021.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package net.wasdev.wlp.test.dev.it;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.maven.shared.utils.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

public class MultiModuleRecompileDepsTest extends BaseMultiModuleTest {

      @BeforeClass
      public static void setUpBeforeClass() throws Exception {
            setUpMultiModule("typeA", "ear", null);
            run("-DrecompileDependencies=false");
      }

      @Test
      public void runTest() throws Exception {
            // test setting recompileDependencies to false for a multi module project
            assertTrue(verifyLogMessageExists(
                        "The recompileDependencies parameter is set to \"false\". On a file change only the affected classes will be recompiled.",
                        20000));

            super.manualTestsInvocationTest("guide-maven-multimodules-jar", "guide-maven-multimodules-war",
                        "guide-maven-multimodules-ear");

            // verify that when modifying a jar class, classes in dependent modules are
            // not recompiled as well
            File targetWebClass = getTargetFileForModule(
                        "war/src/main/java/io/openliberty/guides/multimodules/web/HeightsBean.java",
                        "war/target/classes/io/openliberty/guides/multimodules/web/HeightsBean.class");
            long webLastModified = targetWebClass.lastModified();

            File targetEarClass = getTargetFileForModule(
                        "ear/src/test/java/it/io/openliberty/guides/multimodules/ConverterAppIT.java",
                        "ear/target/test-classes/it/io/openliberty/guides/multimodules/ConverterAppIT.class");
            long targetLastModified = targetEarClass.lastModified();

            testEndpointsAndUpstreamRecompile();

            // verify a source class in the war module was not compiled
            assertTrue(targetWebClass.lastModified() == webLastModified);

            // verify a test class in the ear module was not compiled
            assertTrue(targetEarClass.lastModified() == targetLastModified);
      }

}
