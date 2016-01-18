/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.sling.maven.slingstart;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.commons.io.IOUtils;
import org.apache.felix.utils.version.VersionCleaner;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.sling.provisioning.model.Model;
import org.apache.sling.provisioning.model.ModelUtility;
import org.apache.sling.provisioning.model.io.ModelWriter;

/**
 * Builds a fragment bundle containing our provisioning model,
 * meant for the the provisioning.model.provider bundle to provide 
 * the model at runtime.
 */
@Mojo(
        name = "attach-modelfragment",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true
    )
public class AttachModelFragmentBundle extends AbstractSlingStartMojo {

    public static final String MODELS_BASE_PATH = "MODELS/";
    public static final String MODEL_SUFFIX = "." + BuildConstants.CLASSIFIER_MODEL_FRAGMENT + ".txt";
    public static final String FRAGMENT_HOST = "org.apache.sling.provisioning.model.provider";
    public static final String BSN_SUFFIX ="-" + BuildConstants.CLASSIFIER_MODEL_FRAGMENT;
    
    private void addEntry(JarOutputStream jos, String fullPath, long timestamp) throws IOException {
        final JarEntry e = new JarEntry(fullPath);
        e.setTime(timestamp > 0 ? timestamp : System.currentTimeMillis());
        jos.putNextEntry(e);
    }
    
    private void addFile(JarOutputStream jos, File f, String fullPath) throws IOException {
        final JarEntry e = new JarEntry(fullPath);
        e.setTime(f.lastModified());
        jos.putNextEntry(e);
        final InputStream fis = new BufferedInputStream(new FileInputStream(f));
        try {
            IOUtils.copy(fis, jos);
        } finally {
            IOUtils.closeQuietly(fis);
        }
    }
    
    static String toOsgiVersion(String version) {
        return VersionCleaner.clean(version);
    }
    
    private void dumpModel(JarOutputStream jos, Model model, String path) throws IOException {
        getLog().debug("Adding model dump at " + path);
        addEntry(jos, path, -1);
        final StringWriter w = new StringWriter();
        ModelWriter.write(w, model);
        IOUtils.copy(new StringReader(w.toString()), jos);
    }
    
    private void copyLegalFiles(JarOutputStream jos) throws IOException {
        // Add license and notice if available
        final String [] toCopy = { "LICENSE", "NOTICE" }; 
        
        // If the remote resources plugin has been executed
        // the LICENSE and NOTICE files are found under this path
        final File sourceFolder = new File(new File(project.getBasedir(), "target"), "maven-shared-archive-resources");
        for(String filename : toCopy) {
            final String path = "META-INF" + File.separator + filename;
            final File f = new File(sourceFolder, path);
            if(f.canRead()) {
                getLog().debug("Adding " + filename);
                addFile(jos, f, path);
            }
        }
    }
    
    /** Build jar Manifest including bundle headers */
    private Manifest getManifest(String bundleSymbolicName, String version) {
        final String source = bundleSymbolicName + " " + version;
        final Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(new Attributes.Name("Bundle-SymbolicName"), bundleSymbolicName);
        manifest.getMainAttributes().put(new Attributes.Name("Bundle-Version"), version);
        manifest.getMainAttributes().put(new Attributes.Name("Bundle-ManifestVersion"), "2");
        manifest.getMainAttributes().put(new Attributes.Name("Fragment-Host"), FRAGMENT_HOST);
        manifest.getMainAttributes().put(new Attributes.Name("X-Sling-ModelFragmentSource"), source);
        return manifest;
    }
    
    private File buildModelFragmentBundle(Model model) throws MojoExecutionException {
        getLog().debug("Generating fragment bundle for host bundle " + FRAGMENT_HOST);
        
        final File bundle = new File(this.project.getBuild().getDirectory() + File.separatorChar + BuildConstants.MODEL_FRAGMENT_ARTIFACT_NAME);
        JarOutputStream jos = null;
        final String bundleSymbolicName = project.getArtifactId() + BSN_SUFFIX;
        final String version = toOsgiVersion(project.getVersion());
        
        try {
            jos = new JarOutputStream(new FileOutputStream(bundle), getManifest(bundleSymbolicName, version));
            dumpModel(jos, model, MODELS_BASE_PATH + project.getArtifactId() + MODEL_SUFFIX);
            copyLegalFiles(jos);
        } catch(IOException ioe) {
            throw new MojoExecutionException("Bundle creation failed", ioe);
        } finally {
            IOUtils.closeQuietly(jos);
        }
        return bundle;
    }
        
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Model model = ProjectHelper.getRawModel(this.project);
        if (usePomVariables) {
            model = ModelUtility.applyVariables(model, new PomVariableResolver(project));
        }
        if (usePomDependencies) {
            model = ModelUtility.applyArtifactVersions(model, new PomArtifactVersionResolver(project, allowUnresolvedPomDependencies));
        }
        
        final File fragmentFile = buildModelFragmentBundle(model);

        projectHelper.attachArtifact(project, BuildConstants.TYPE_JAR, 
                BuildConstants.CLASSIFIER_MODEL_FRAGMENT, fragmentFile);
    }
}
