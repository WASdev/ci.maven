/**
 * (C) Copyright IBM Corporation 2014, 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.wasdev.wlp.maven.plugins.applications;

import java.io.File;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.plugin.MojoExecutionException;
import org.codehaus.mojo.pluginsupport.util.ArtifactItem;

/**
 * Copy applications to the specified directory of the Liberty server.
 */
@Mojo(name = "install-apps", requiresDependencyResolution=ResolutionScope.COMPILE)
public class InstallAppsMojo extends InstallAppMojoSupport {

    /**
     * Packages to install. One of "all", "dependencies" or "project".
     */
    @Parameter(property = "installAppPackages", defaultValue = "dependencies")
    protected String installAppPackages = null;
    
    protected void doExecute() throws Exception {
        if (skip) {
            return;
        }
        checkServerHomeExists();
        checkServerDirectoryExists();
        
        boolean installDependencies = false;
        boolean installProject = false;
        
        switch (installAppPackages) {
            case "all":
                installDependencies = true;
                installProject = true;
                break;
            case "dependencies":
                installDependencies = true;
                break;
            case "project":
                installProject = true;
                break;
            default:
                return;
        }
        if (installDependencies) {
            installDependencies();
        }
        if (installProject) {
            installProject();
        }
    }
    
    private void installDependencies() throws Exception {
        @SuppressWarnings("unchecked")
        Set<Artifact> artifacts = (Set<Artifact>) project.getDependencyArtifacts();
        for (Artifact dep : artifacts) {
            // TODO: skip if not an application type supported by Liberty
            // skip assemblyArtifact if specified as a dependency
            if (assemblyArtifact != null && matches(dep, assemblyArtifact)) {
                continue;
            }
            if (dep.getScope().equals("compile")) {
                installApp(dep);
            }
        }
    }
    
    private void installProject() throws Exception {
        if (isSupportedType(project.getPackaging())) {
            if (looseConfig) {
                switch(project.getPackaging()) {
                    case "war":
                        installLooseConfigApp();
                        break;
                    case "liberty-assembly":
                        File dir = new File(project.getBasedir() + "/src/main/webapp");
                        if (dir.exists()) {
                            installLooseConfigApp();
                        } else {
                            log.debug("liberty-assembly project does not have soruce code for web project.");
                        }
                        break;
                    default:
                        //TODO: revise and move message to MvnMessages.properties
                        log.info("Can not generate loose configuration for Project artifact type. Project aritifact will be installed as is.");
                        installApp(project.getArtifact());
                        break;
                }
            } else {
                installApp(project.getArtifact());
            }
        } else {
            // TODO: revise and put the message in MvnMessgaes.properties.
            throw new MojoExecutionException("Application type is not supported," + project.getPackaging());
        }
    }

    private boolean matches(Artifact dep, ArtifactItem assemblyArtifact) {
        return dep.getGroupId().equals(assemblyArtifact.getGroupId())
                && dep.getArtifactId().equals(assemblyArtifact.getArtifactId())
                && dep.getType().equals(assemblyArtifact.getType());
    }
    
    private boolean isSupportedType(String type) {
        switch (type) {
        case "ear":
        case "war":
        case "eba":
        case "esa":
        case "liberty-assembly":
            return true;
        default:
            return false;
        }
    }
   
}
