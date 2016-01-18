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
package org.apache.sling.provisioning.model.runtime.provider.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.provisioning.model.Model;
import org.apache.sling.provisioning.model.io.ModelReader;
import org.apache.sling.provisioning.model.runtime.provider.ProvisioningModelProvider;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** The default {@link ProvisioningModelProvider} */
@Component
@Service(value=ProvisioningModelProvider.class)
public class DefaultProvisioningModelProvider implements ProvisioningModelProvider {
    private BundleContext bundleContext;
    private final Logger log = LoggerFactory.getLogger(getClass());
    public static final String MODEL_RESOURCE_ROOT = "/MODELS";
    
    @Activate
    protected void activate(ComponentContext ctx) {
        bundleContext = ctx.getBundleContext();
    }
    
    @Override
    public Model getProvisioningModel(String id) throws IOException {
        if(id != null && id.trim().length() == 0) {
            id = null;
        }
        if(id != null) {
            throw new UnsupportedOperationException("Retrieving specific models is not implemented yet");
        }
        return getMainModel();
    }
    
    private List<URL> getModelUrls() throws IOException {
        final List<URL> urls = new ArrayList<URL>();
        @SuppressWarnings("unchecked")
        final Enumeration<URL> entries = bundleContext.getBundle().findEntries("MODELS", "*.txt", true);
        while(entries.hasMoreElements()) {
            urls.add(entries.nextElement());
        }
        return urls;
    }
    
    private Model getMainModel() throws IOException {
        final List<URL> urls = getModelUrls();
        if(urls.isEmpty()) {
            return null;
        } else if(urls.size() > 1) {
            throw new IOException("Multiple models found under " + MODEL_RESOURCE_ROOT + ":" + urls);
        }

        final URL url = urls.get(0);
        log.debug("Main provisioning model found at {}", url);
        final InputStream is = url.openConnection().getInputStream();
        try {
            return ModelReader.read(new InputStreamReader(is), url.toString());
        } finally {
            is.close();
        }
    }
}
