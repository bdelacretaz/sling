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
package org.apache.sling.binaryrefs;

import java.io.IOException;
import java.io.InputStream;

/** Resolves binary references of a specific type. Services implementing
 *  this interface must be registered with a BINARY_REF_RESOLVER_TYPE
 *  service property that indicates which type they handle.
 */
public interface BinaryRefResolver {
    public static final String BINARY_REF_RESOLVER_TYPE = "sling.binaryref.resolver.type";

    /** Resolve a binary reference of this type to an InputStream.
     * @param uri The resolver-specific URI to resolve.
     * @return An InputStream or null if not found.
     * @throws IOException
     */
    InputStream resolve(String uri) throws IOException;
}
