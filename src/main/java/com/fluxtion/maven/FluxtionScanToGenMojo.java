package com.fluxtion.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author greg higgins
 */
@Mojo(name = "scan",
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.COMPILE
)
public class FluxtionScanToGenMojo extends AbstractFluxtionMojo {

    @Parameter(property = "buildDirectory", defaultValue = "target/classes")
    protected String buildDirectory;

    @Parameter(property = "outputDirectory")
    protected String outputDirectory;

    @Parameter(property = "resourcesDirectory")
    protected String resourcesDirectory;
    public static final String GENERATOR_METHOD = "scanAndGenerateFluxtionBuilders";
    public static final String FLUXTION_GENERATOR_CLASS = "com.fluxtion.compiler.Fluxtion";
    public static final String OUTPUT_DIRECTORY = "FLUXTION.OUTPUT.DIRECTORY";
    public static final String RESOURCES_DIRECTORY = "FLUXTION.RESOURCES.DIRECTORY";
    @Override
    public void execute() throws MojoExecutionException {
        if (System.getProperty("skipFluxtion") != null) {
            getLog().info("Fluxtion generation skipped.");
        } else {
            try {
                if (buildDirectory == null) {
                    buildDirectory = project.getBasedir().getCanonicalPath() + "/target/classes";
                } else if (!buildDirectory.startsWith("/")) {
                    buildDirectory = project.getBasedir().getCanonicalPath() + "/" + buildDirectory;
                }
                if(outputDirectory == null){
                    outputDirectory = project.getBuild().getSourceDirectory();
                }
                if(resourcesDirectory == null){
                    resourcesDirectory = project.getBasedir().getCanonicalPath() + "/src/main/resources";
                }
                System.setProperty(OUTPUT_DIRECTORY, outputDirectory);
                System.setProperty(RESOURCES_DIRECTORY, resourcesDirectory);
                URLClassLoader classLoader = buildFluxtionClassLoader();
                Class<?> genClass = classLoader.loadClass(FLUXTION_GENERATOR_CLASS);
                Method generatorMethod = genClass.getMethod(GENERATOR_METHOD, ClassLoader.class, File[].class);
                generatorMethod.invoke(null, classLoader, new File[]{new File(buildDirectory)});
            } catch (Exception exception) {
                getLog().error(exception);
                throw new MojoExecutionException("problem setting building fluxtion class loader", exception);
            }
        }
    }
}

