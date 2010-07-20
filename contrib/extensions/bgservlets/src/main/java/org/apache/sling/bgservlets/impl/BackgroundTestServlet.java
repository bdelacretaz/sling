/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.bgservlets.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet used for interactive testing of the background servlets engine. TODO
 * remove once we have better tests.
 */
@Component
@Service
@SuppressWarnings("serial")
@Property(name = "sling.servlet.paths", value = "/system/bgservlets/test")
public class BackgroundTestServlet extends SlingSafeMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void doGet(SlingHttpServletRequest request,
            SlingHttpServletResponse response) throws ServletException,
            IOException {
        response.setContentType("text/plain");
        final PrintWriter w = response.getWriter();

        final int cycles = getIntParam(request, "cycles", 10);
        final int interval = getIntParam(request, "interval", 1);
        final int flushEvery = getIntParam(request, "flushEvery", 2);

        w.println("Start at " + new Date());
        try {
            for (int i = 1; i <= cycles; i++) {
                if (i % flushEvery == 0) {
                    w.println("Flushing output");
                    w.flush();
                }
                w.printf("Cycle %d of %d\n", i, cycles);
                try {
                    Thread.sleep(interval * 1000);
                } catch (InterruptedException iex) {
                    throw new ServletException("InterruptedException", iex);
                }
            }
            w.println("All done.");
            w.flush();
        } catch (Throwable t) {
            log.info("Exception in doGet", t);
        }
    }

    private int getIntParam(SlingHttpServletRequest request, String name,
            int defaultValue) {
        int result = defaultValue;
        final String str = request.getParameter(name);
        if (str != null) {
            result = Integer.parseInt(str);
        }
        return result;
    }
}
