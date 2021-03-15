/*
 * Copyright (c) 2008-2021 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.addon.cubajm;

import com.haulmont.cuba.web.sys.singleapp.SingleAppWebContextLoader;
import net.bull.javamelody.MonitoringFilter;

import javax.servlet.*;
import java.util.EnumSet;

public class MySingleAppWebServletLoader extends SingleAppWebContextLoader {

    public MySingleAppWebServletLoader() {
        super();
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.contextInitialized(servletContextEvent);
        Filter javamelodyFilter = null;
        try {
            javamelodyFilter = servletContextEvent.getServletContext().createFilter(MonitoringFilter.class);
        } catch (ServletException e) {
            e.printStackTrace();
        }
        String filterName = "web_javamelody_filter";
        ServletContext servletContext = servletContextEvent.getServletContext();
//        try {
//            // initialize custom filter manually to avoid auto registration caused by web fragment in javamelody-core
//            javamelodyFilter.init(new FilterConfig() {
//                @Override
//                public String getFilterName() {
//                    return filterName;
//                }
//                @Override
//                public ServletContext getServletContext() {
//                    return servletContext;
//                }
//                @Override
//                public String getInitParameter(String name) {
//                    if (MONITORING_PATH_PARAM.equals(name)) {
//                        return "/monitoring";
//                    }
//                    return null;
//                }
//                @Override
//                public Enumeration<String> getInitParameterNames() {
//                    Vector<String> params = new Vector<>();
//                    params.add(MONITORING_PATH_PARAM);
//                    return params.elements();
//                }
//            });
//        } catch (ServletException ex) {
//            throw new RuntimeException("ServletException occurred while initializing JavaMelody filter", ex);
//        }
        FilterRegistration.Dynamic javamelody = servletContext.addFilter(filterName, javamelodyFilter);
        javamelody.setInitParameter("monitoring-path", "/monitoring");
        javamelody.setAsyncSupported(true);
        javamelody.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "/*");
    }
}
