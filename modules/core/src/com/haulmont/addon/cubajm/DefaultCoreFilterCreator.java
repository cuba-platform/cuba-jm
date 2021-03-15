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

import com.haulmont.cuba.core.sys.AppComponents;
import com.haulmont.cuba.core.sys.AppContext;
import net.bull.javamelody.MonitoringFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.*;
import javax.servlet.annotation.WebListener;
import java.util.EnumSet;
// implements ServletContextListener
//@Component
public class DefaultCoreFilterCreator implements ServletContextAware {

    @Override
    public void setServletContext(ServletContext servletContext) {

    }



//    @Override
//    public void contextInitialized(ServletContextEvent sce) {
//        ServletContext servletContext = sce.getServletContext();
//        FilterRegistration.Dynamic javamelody = servletContext.addFilter("javamelody-core", MonitoringFilter.class);
//        javamelody.setInitParameter("monitoring-path", "/monitoring");
//        javamelody.setAsyncSupported(true);
//        javamelody.addMappingForUrlPatterns(
//                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "/*");
//    }
//
//    @Override
//    public void contextDestroyed(ServletContextEvent sce) {}
}
