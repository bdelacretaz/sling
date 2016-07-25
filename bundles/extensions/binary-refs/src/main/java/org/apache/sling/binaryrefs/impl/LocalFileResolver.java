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
package org.apache.sling.binaryrefs.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.binaryrefs.BinaryRefResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** BinaryRefResolver that resolves local files under a configurable root.
 *  Multiple resolvers can be configured if desired, each with its
 *  own root path. */
@Component(metatype=true, policy=ConfigurationPolicy.REQUIRE)
@Service(value=BinaryRefResolver.class)
public class LocalFileResolver implements  BinaryRefResolver {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Property
    public static final String PROP_RESOLVER_TYPE = BinaryRefResolver.BINARY_REF_RESOLVER_TYPE;
    private String resolverType;
    
    @Property
    public static final String PROP_ROOT_PATH = "root.path";
    private File root;
    
    @Activate
    public void activate(Map<String, Object> config) {
        root = new File(PropertiesUtil.toString(config.get(PROP_ROOT_PATH), null));
        resolverType = PropertiesUtil.toString(config.get(PROP_RESOLVER_TYPE), "");
    }
    
    public String toString() {
        return getClass().getSimpleName() + "/" + resolverType;
    }
    
    public String sanitize(String uri) {
        // TODO make sure the uri cannot allow for
        // jumping out of the root: must be a relative
        // path, no double dot etc.
        return uri;
    }
    
    @Override
    public InputStream resolve(String uri) throws IOException {
        uri = sanitize(uri);
        final File f = new File(root, uri);
        if(!f.canRead()) {
            throw new IOException("Cannot read " + uri);
        }
        return new BufferedInputStream(new FileInputStream(f));
    }
}