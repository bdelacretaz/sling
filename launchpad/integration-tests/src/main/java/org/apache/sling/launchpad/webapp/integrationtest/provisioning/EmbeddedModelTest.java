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
package org.apache.sling.launchpad.webapp.integrationtest.provisioning;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.sling.junit.rules.TeleporterRule;
import org.apache.sling.provisioning.model.Feature;
import org.apache.sling.provisioning.model.Model;
import org.apache.sling.provisioning.model.runtime.provider.ProvisioningModelProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/** Verify that the provisioning model used to build this instance is available
 *  via the ProvisioningModelProvider service.
 */
public class EmbeddedModelTest {
    @Rule
    public final TeleporterRule teleporter = TeleporterRule.forClass(getClass(), "Launchpad");
    
    private Model model;
    
    private void assertFeature(String name) {
        final Feature f = model.getFeature(name);
        assertNotNull("Expecting feature [" + name + "]", f);
    }
    
    @Before
    public void setup() throws IOException {
        model = teleporter.getService(ProvisioningModelProvider.class).getProvisioningModel(null);
        assertNotNull("Expecting non-null model", model);
    }
    
    @Test
    public void testLaunchpadFeature() {
        assertFeature(":launchpad");
    }
    
    @Test
    public void testBootFeature() {
        assertFeature(":boot");
    }
}
