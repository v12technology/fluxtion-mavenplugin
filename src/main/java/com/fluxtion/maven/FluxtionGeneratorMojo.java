/* 
 * Copyright (C) 2017 V12 Technology Limited
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.fluxtion.maven;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * A mojo to wrap the invocation of the Fluxtion executable.
 *
 * @author Greg Higgins (greg.higgins@v12technology.com)
 */
@Mojo(name = "generate",
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.COMPILE
)
public class FluxtionGeneratorMojo extends AbstractMojo {

    private String classPath;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            updateClasspath();
            try {
                setDefaultProperties();
                getLog().info("=========== Fluxtion gen started  ==============");
                ProcessBuilder processBuilder = new ProcessBuilder();
                List<String> cmdList = new ArrayList<>();
                cmdList.add(fluxtionExePath.getCanonicalPath());
                if(logDebug){
                    cmdList.add("--debug");
                }
                cmdList.add("-outDirectory");
                cmdList.add(outputDirectory);
                cmdList.add("-buildDirectory");
                cmdList.add(buildDirectory);
                cmdList.add("-outResDirectory");
                cmdList.add(resourcesOutputDirectory);
                cmdList.add("-outPackage");
                cmdList.add(packageName);
                cmdList.add("-configClass");
                cmdList.add(configClass);
                cmdList.add("-outClass");
                cmdList.add(className);
                cmdList.add("-buildClasses");
                cmdList.add(Boolean.toString(compileGenerated));
                cmdList.add("-formatSource");
                cmdList.add(Boolean.toString(formatSource));
                cmdList.add("-supportDirtyFiltering");
                cmdList.add(Boolean.toString(supportDirtyFiltering));
                cmdList.add("-generateDebugPrep");
                cmdList.add(Boolean.toString(generateDebugPrep));
//                cmdList.add("-generateTestDecorator");
//                cmdList.add(Boolean.toString(generateTestDecorator));
                cmdList.add("-assignPrivate");
                cmdList.add(Boolean.toString(assignNonPublicMembers));
                //optionals
                if(nodeNamingClass!=null){
                    cmdList.add("-nodeNamingClass");
                    cmdList.add(nodeNamingClass);
                }
                if(filterNamingClass!=null){
                    cmdList.add("-filterNamingClass");
                    cmdList.add(filterNamingClass);
                }
                if(rootFactoryClass!=null){
                    cmdList.add("-rootFactoryClass");
                    cmdList.add(rootFactoryClass);
                }
                if(yamlFactoryConfig!=null){
                    cmdList.add("-yamlFactoryConfig");
                    cmdList.add(yamlFactoryConfig.getCanonicalPath());
                }
                if(templateSep!=null){
                    cmdList.add("-sepTemplate");
                    cmdList.add(templateSep);
                }
                if(templateDebugSep!=null){
                    cmdList.add("-sepDebugTemplate");
                    cmdList.add(templateDebugSep);
                }                
                //must be at end
                cmdList.add("-cp");
                cmdList.add(classPath);
                processBuilder.command(cmdList);
                processBuilder.redirectErrorStream(true);
                processBuilder.inheritIO();
                getLog().info(processBuilder.command().stream().collect(Collectors.joining(" ")));
                Process p = processBuilder.start();
                p.waitFor();
                getLog().info("=========== Fluxtion gen complete ==============");
            } catch (IOException | InterruptedException e) {
                getLog().error("error while invoking Fluxtion generator", e);
                getLog().info("=========== Fluxtion gen ERROR =================");
            }
        } catch (MalformedURLException | DependencyResolutionRequiredException ex) {
                getLog().error("error while building classpath", ex);
                getLog().info("=========== Fluxtion gen ERROR =================");
        }
    }
    
    private void setDefaultProperties() throws MojoExecutionException, IOException {
        try {
            if (outputDirectory == null || outputDirectory.length() < 1) {
                outputDirectory = project.getBasedir().getCanonicalPath() + "/target/generated-sources/java";
            }
            if (resourcesOutputDirectory == null || resourcesOutputDirectory.length() < 1) {
                resourcesOutputDirectory = project.getBasedir().getCanonicalPath() + "/target/generated-sources/sep";
            }
            if(buildDirectory == null){
                buildDirectory = project.getBasedir().getCanonicalPath() + "/target/classes";
            }
        } catch (IOException iOException) {
            getLog().error(iOException);
            throw new MojoExecutionException("problem setting default properties", iOException);
        }
    }

    @Parameter(property = "project", readonly = true, required = true)
    private MavenProject project;

    @Parameter(defaultValue = "${session}", readonly = true)
    private MavenSession mavenSession;

    @Component
    private BuildPluginManager pluginManager;

    
    @Parameter(property = "fluxtionExe", required = true)
    private File fluxtionExePath;
    
    @Parameter(property = "plugin")
    private PluginDescriptor descriptor;

    @Parameter(property = "configClass")
    private String configClass;

    @Parameter(property = "nodeNamingClass")
    private String nodeNamingClass;

    @Parameter(property = "filterNamingClass")
    private String filterNamingClass;

    @Parameter(property = "packageName", required = true)
    private String packageName;

    @Parameter(property = "className", required = true)
    private String className;

    @Parameter(property = "rootFactoryClass", required = false)
    private String rootFactoryClass;

    @Parameter(property = "yamlFactoryConfig", required = false)
    private File yamlFactoryConfig;

    @Parameter(property = "outputDirectory")
    private String outputDirectory;

    @Parameter(property = "buildDirectory")
    private String buildDirectory;

    @Parameter(property = "resourcesOutputDirectory")
    private String resourcesOutputDirectory;

    @Parameter(property = "templateSep", defaultValue = "javaTemplate.vsl")
    private String templateSep;

    @Parameter(property = "templateDebugSep", defaultValue = "javaTemplateDebug.vsl")
    private String templateDebugSep;

    @Parameter(property = "supportDirtyFiltering", defaultValue = "true")
    private boolean supportDirtyFiltering;

    @Parameter(property = "generateDebugPrep", defaultValue = "false")
    public boolean generateDebugPrep;

    @Parameter(property = "generateTestDecorator", defaultValue = "false")
    public boolean generateTestDecorator;

    @Parameter(property = "assignNonPublicMembers", defaultValue = "false")
    public boolean assignNonPublicMembers;

    @Parameter(property = "compileGenerated", defaultValue = "true")
    public boolean compileGenerated;

    @Parameter(property = "formatSource", defaultValue = "true")
    public boolean formatSource;

    @Parameter(property = "logDebug", defaultValue = "false")
    public boolean logDebug;

    private void updateClasspath() throws MojoExecutionException, MalformedURLException, DependencyResolutionRequiredException {
        StringBuilder sb = new StringBuilder();
        List<String> elements = project.getRuntimeClasspathElements();
        Set<File> fileSet = new HashSet<>();
        for (String element : elements) {
            File elementFile = new File(element);
            fileSet.add(elementFile);
            getLog().debug("Adding element from plugin classpath:" + elementFile.getPath());
            sb.append(elementFile.getPath()).append(";");
        }
        Set<Artifact> artifacts = project.getArtifacts();
        for (Artifact artifact : artifacts) {
            File file = artifact.getFile();
            if(!fileSet.contains(file)){
                getLog().debug("Adding element from plugin classpath:" + file.getAbsolutePath());
                sb.append(file.getAbsolutePath()).append(";");
            }
        }
        classPath = sb.substring(0, sb.length() - 1);
        getLog().debug("classpath:" + classPath);
    }
}
