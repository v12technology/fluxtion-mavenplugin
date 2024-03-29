package com.fluxtion.maven;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractFluxtionMojo extends AbstractMojo {

    /**
     * The output directory for build artifacts generated by fluxtion. Absolute paths are preceded with "/" otherwise
     * the path relative to the project root directory
     */
    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    protected MavenProject project;

    protected URLClassLoader buildFluxtionClassLoader() throws MalformedURLException, DependencyResolutionRequiredException {
        List<String> elements = project.getCompileClasspathElements();
        URL[] urls = new URL[elements.size()];
        String cp = "";
        for (int i = 0; i < elements.size(); i++) {
            cp += File.pathSeparator + elements.get(i);
            urls[i] = new File(elements.get(i)).toURI().toURL();
        }
        getLog().debug("user classpath URL list:" + Arrays.toString(urls));
        URLClassLoader classLoader = URLClassLoader.newInstance(urls);
        System.setProperty("FLUXTION.GENERATION.CLASSPATH", cp);
        return classLoader;
    }
}
