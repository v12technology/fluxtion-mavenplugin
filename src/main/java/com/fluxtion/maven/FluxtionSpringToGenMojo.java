package com.fluxtion.maven;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URLClassLoader;

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
    @Override
    public void execute() throws MojoExecutionException {
        if (System.getProperty("skipFluxtion") != null) {
            getLog().info("Fluxtion generation skipped.");
        } else {
            try {
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

