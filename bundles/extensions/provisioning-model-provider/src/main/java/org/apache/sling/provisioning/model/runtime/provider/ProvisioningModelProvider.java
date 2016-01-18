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
package org.apache.sling.provisioning.model.runtime.provider;

import java.io.IOException;

import org.apache.sling.provisioning.model.Model;

/** Service that gives access to the instance's provisioning 
 *  model at runtime.
 */
public interface ProvisioningModelProvider {
    /** Retrieve a provisioning model.
     * @param id If null, the main provisioning model, used to
     *  start this instance, is returned.
     * @return The specified model, null if not found.
     */
    Model getProvisioningModel(String id) throws IOException;
}
