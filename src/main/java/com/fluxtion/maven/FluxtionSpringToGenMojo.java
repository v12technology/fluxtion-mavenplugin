package com.fluxtion.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import static com.fluxtion.maven.FluxtionScanToGenMojo.OUTPUT_DIRECTORY;
import static com.fluxtion.maven.FluxtionScanToGenMojo.RESOURCES_DIRECTORY;

/**
 * @author greg higgins
 */
@Mojo(name = "springToFluxtion",
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.COMPILE
)
public class FluxtionSpringToGenMojo extends AbstractFluxtionMojo {

    public static final String GENERATOR_METHOD = "compileAot";
    public static final String FLUXTION_GENERATOR_CLASS = "com.fluxtion.compiler.extern.spring.FluxtionSpring";
    @Parameter(required = true)
    protected String className;
    @Parameter(required = true)
    protected String packageName;
    @Parameter(required = true)
    protected File springFile;

    @Parameter(property = "outputDirectory")
    protected String outputDirectory;

    @Parameter(property = "resourcesDirectory")
    protected String resourcesDirectory;
    @Override
    public void execute() throws MojoExecutionException {
        if (System.getProperty("skipFluxtion") != null) {
            getLog().info("Fluxtion generation skipped.");
        } else {
            try {
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
                Method generatorMethod = genClass.getMethod(GENERATOR_METHOD, ClassLoader.class, File.class, String.class, String.class);
                generatorMethod.invoke(null, classLoader, springFile, className, packageName);
            } catch (Exception exception) {
                getLog().error(exception);
                throw new MojoExecutionException("problem setting building fluxtion class loader", exception);
            }
        }
    }
}

