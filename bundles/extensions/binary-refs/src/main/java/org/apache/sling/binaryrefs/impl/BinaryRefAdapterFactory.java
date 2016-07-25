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

import static org.apache.sling.binaryrefs.BinaryRefPropertyNames.REF_TYPE_PROP;
import static org.apache.sling.binaryrefs.BinaryRefPropertyNames.REF_URI_PROP;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.binaryrefs.BinaryRefResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Adapts a JCR node to an InputStream if the node
 *  contains a valid Sling Binary Reference.
 */
@Component(metatype=false)
@Service(value=AdapterFactory.class)
@Properties({
    @Property(name="service.vendor", value="The Apache Software Foundation"),
    @Property(name="service.description", value="Default SlingScriptResolver"),
    @Property(name="adaptables", value="org.apache.sling.api.resource.Resource"),
    @Property(name="adapters", value={"java.io.InputStream",}),
    @Property(name="adapter.condition", value="If the resource contains a valid SlingBinaryRef")
})
@Reference(
    name="binaryRefResolver", 
    referenceInterface=BinaryRefResolver.class, 
    cardinality=ReferenceCardinality.OPTIONAL_MULTIPLE, 
    policy=ReferencePolicy.DYNAMIC)
public class BinaryRefAdapterFactory implements AdapterFactory{

    private final ConcurrentHashMap<String, BinaryRefResolver> resolvers = new ConcurrentHashMap<String, BinaryRefResolver>();
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public <AdapterType> AdapterType getAdapter(Object toAdapt, Class<AdapterType> targetClass) {
        final ValueMap m = ((Resource)toAdapt).adaptTo(ValueMap.class);
        final String refType = m.get(REF_TYPE_PROP, "");
        if(!refType.isEmpty()) {
            final String refURI = m.get(REF_URI_PROP, "");
            if(!refURI.isEmpty()) {
                try {
                    return (AdapterType)resolve(refType, refURI);
                } catch (IOException e) {
                    log.warn("binaryRefResolver fails for " + refType + "/" + refURI, e);
                }
            }
        }
        return null;
    }
    
    private InputStream resolve(String refType, String refURI) throws IOException {
        InputStream result = null;
        final BinaryRefResolver resolver = resolvers.get(refType);
        if(resolver == null) {
            log.warn("No BinaryRefResolver found for type [{}]", refType);
        } else {
            result = resolver.resolve(refURI);
            log.debug("Resolving {}/{} using {} returns {}", 
                    new Object[] { refType, refURI, resolver, result });
        }
        return result;
    }
    
    protected void bindBinaryRefResolver(BinaryRefResolver r, Map<String, Object> properties) {
        final String resolverType = getResolverType(r, properties);
        if(resolverType != null) {
            log.info("Registering {} for type {}", r, resolverType);
            resolvers.put(resolverType, r);
        }
    }
    
    protected void unbindBinaryRefResolver(final BinaryRefResolver r, Map<String, Object> properties) {
        final String resolverType = getResolverType(r, properties);
        if(resolverType != null) {
            log.info("Unregistering {} for type {}", r, resolverType);
            resolvers.remove(resolverType);
        }
    }
    
    private String getResolverType(BinaryRefResolver r, Map<String, Object> properties) {
        final String result = PropertiesUtil.toString(properties.get(BinaryRefResolver.BINARY_REF_RESOLVER_TYPE), null);
        if(result == null) {
            log.warn("Ignoring BinaryRefResolver due to null {}: {}", BinaryRefResolver.BINARY_REF_RESOLVER_TYPE, r);
        }
        return result;
    }
    
}