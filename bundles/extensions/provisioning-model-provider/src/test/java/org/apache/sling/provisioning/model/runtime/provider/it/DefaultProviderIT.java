/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.provisioning.model.runtime.provider.it;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.ops4j.pax.exam.CoreOptions.bundle;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.sling.provisioning.model.Feature;
import org.apache.sling.provisioning.model.Model;
import org.apache.sling.provisioning.model.runtime.provider.ProvisioningModelProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
@Ignore("TODO - activate with a different test bundle, not launchpad.test-bundles")
public class DefaultProviderIT {
    private static final String FELIX_GID = "org.apache.felix";
    private static final String SLING_GID = "org.apache.sling";
    
    @Inject
    private ProvisioningModelProvider provider;
    
    private String requireSystemProperty(String key) {
        final String value = System.getProperty(key);
        if(value == null || value.trim().length() == 0) {
            fail("Missing system property " + key);
        }
        return value;
    }
    
    @org.ops4j.pax.exam.Configuration
    public Option[] config() {
        final String bundleFileName = requireSystemProperty("bundle.file.name");
        final File bundleFile = new File(bundleFileName);
        if (!bundleFile.canRead()) {
            throw new IllegalArgumentException("Cannot read from bundle file " + bundleFile.getAbsolutePath());
        }
        
        final String testBundlesVersion = requireSystemProperty("test.bundles.version");

        return options(
                // Provision this bundle along with a fragment that
                // provides a provisioning model under /MODELS
                provision(
                        bundle(bundleFile.toURI().toString()),
                        mavenBundle(FELIX_GID, "org.apache.felix.scr", "1.6.2"),
                        mavenBundle(
                                maven().groupId(SLING_GID)
                                .artifactId("org.apache.sling.provisioning.model")
                                .versionAsInProject()),
                         bundle(
                                 "mvn:org.apache.sling/org.apache.sling.launchpad.test-bundles/" 
                                  + testBundlesVersion 
                                  + "/jar/modelfragment").noStart()
                ),
                junitBundles()
        );
    }
    
    @Test
    public void mainModelIsPresent() throws IOException {
        final Model model = provider.getProvisioningModel(null);
        assertNotNull("Expecting non-null main model", model);
    }
    
    @Test
    public void mainModelFeatures() throws IOException {
        final Model model = provider.getProvisioningModel(null);
        final List<String> featureNames= new ArrayList<String>();
        for(Feature f : model.getFeatures()) {
            featureNames.add(f.getName());
        }
        final String expected = "launchpad-test-bundles";
        assertTrue(
                "Expecting feature names to include " + expected + " (" + featureNames + ")", 
                featureNames.contains(expected));
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void specificModel() throws IOException {
        provider.getProvisioningModel("foo");
    }
}