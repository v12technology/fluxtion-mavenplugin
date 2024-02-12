package com.fluxtion.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

import static com.fluxtion.maven.FluxtionScanToGenMojo.OUTPUT_DIRECTORY;
import static com.fluxtion.maven.FluxtionScanToGenMojo.RESOURCES_DIRECTORY;

/**
 * @author greg higgins support DataDrivenGenerationConfig yaml files to generate a Fluxtion event processor
 */
@Mojo(name = "yamlToFluxtion",
        requiresProject = true,
        requiresDependencyResolution = ResolutionScope.COMPILE,
        defaultPhase = LifecyclePhase.COMPILE
)
public class FluxtionYamlToGenMojo extends AbstractFluxtionMojo {

    public static final String GENERATOR_METHOD = "compileFromReader";
    public static final String FLUXTION_GENERATOR_CLASS = "com.fluxtion.compiler.Fluxtion";
    @Parameter(required = true)
    protected File[] fluxtionConfigFiles;
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
                for (File fluxtionConfigFile : fluxtionConfigFiles) {
                    FileReader fileReader = new FileReader(fluxtionConfigFile);
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
                    Method generatorMethod = genClass.getMethod(GENERATOR_METHOD, Reader.class);
                    generatorMethod.invoke(null, fileReader);
                }
            } catch (Exception exception) {
                getLog().error(exception);
                throw new MojoExecutionException("problem setting building fluxtion class loader", exception);
            }
        }
    }
}

