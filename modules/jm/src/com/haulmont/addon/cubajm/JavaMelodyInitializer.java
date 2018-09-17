/*
 * Copyright (c) 2008-2018 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.haulmont.addon.cubajm;

import com.haulmont.bali.util.ReflectionHelper;
import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import net.bull.javamelody.MonitoringFilter;
import net.bull.javamelody.SessionListener;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.*;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Component("cubajm_JavaMelodyInitializer")
public class JavaMelodyInitializer {

    private static final String JAVAMELODY_FILTER_URL_PROP = "cubajm.monitoringUrl";
    private static final String MONITORING_PATH_PARAM = "monitoring-path";

    private final Logger log = LoggerFactory.getLogger(JavaMelodyInitializer.class);

    @Inject
    private ServletRegistrationManager servletRegistrationManager;

    @Inject
    private JavaMelodyConfig javaMelodyConfig;

    private boolean initialized;
    private boolean skipRegistration;

    private String jmFilterUrl;
    private String filtersNameBase;

    @EventListener
    public void initialize(ServletContextInitializedEvent e) {
        if (singleWarDeployment(e.getSource())) {
            String msg = String.format("SingleWAR deployment detected. JavaMelody monitoring will be available " +
                    "by the URL defined in application property %s for the \"core\" module", JAVAMELODY_FILTER_URL_PROP);
            log.info(msg);

            return;
        }

        if (!initialized) {
            initializeSecurityFilter(e);

            initializeJavaMelodyFilter(e);

            initializeJavamelodyListener(e);

            initialized = true;
        }
    }

    private void initializeSecurityFilter(ServletContextInitializedEvent e) {
        log.info("Registering JavaMelody security filter");

        jmFilterUrl = javaMelodyConfig.getMonitoringUrl();
        if (jmFilterUrl == null || jmFilterUrl.isEmpty()) {
            skipRegistration = true;

            log.info("Value of application property '{}' is not defined." +
                    "JavaMelody monitoring will not be enabled for this application block",
                    JAVAMELODY_FILTER_URL_PROP);

            return;
        }

        filtersNameBase = StringUtils.replaceEach(jmFilterUrl,
                new String[]{"/", "*"},
                new String[]{"_", ""})
                .substring(1);
        String secFilterName = filtersNameBase + "javamelody_security_filter";

        Filter securityFilter = servletRegistrationManager.createFilter(e.getApplicationContext(),
                JavaMelodySecurityFilter.class.getName());

        e.getSource().addFilter(secFilterName, securityFilter)
                .addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, jmFilterUrl);

        log.info("JavaMelody security filter registered");
    }

    private void initializeJavaMelodyFilter(ServletContextInitializedEvent e) {
        if (skipRegistration) {
            return;
        }

        log.info("Registering JavaMelody monitoring filter");

        Filter javamelodyFilter = servletRegistrationManager.createFilter(e.getApplicationContext(),
                MonitoringFilter.class.getName());

        String filterName = filtersNameBase + "javamelody_filter";

        final ServletContext servletContext = e.getSource();
        try {
            // initialize custom filter manually to avoid auto registration caused by web fragment in javamelody-core
            javamelodyFilter.init(new FilterConfig() {
                @Override
                public String getFilterName() {
                    return filterName;
                }

                @Override
                public ServletContext getServletContext() {
                    return servletContext;
                }

                @Override
                public String getInitParameter(String name) {
                    if (MONITORING_PATH_PARAM.equals(name)) {
                        return jmFilterUrl;
                    }

                    return null;
                }

                @Override
                public Enumeration<String> getInitParameterNames() {
                    Vector<String> params = new Vector<>();
                    params.add(MONITORING_PATH_PARAM);

                    return params.elements();
                }
            });
        } catch (ServletException ex) {
            throw new RuntimeException("ServletException occurred while initializing JavaMelody filter", ex);
        }

        FilterRegistration.Dynamic javamelody = servletContext
                .addFilter(filterName, javamelodyFilter);

        javamelody.setInitParameter(MONITORING_PATH_PARAM, jmFilterUrl);
        javamelody.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "/*");

        log.info("JavaMelody monitoring filter registered");
    }

    private void initializeJavamelodyListener(ServletContextInitializedEvent e) {
        if (skipRegistration) {
            return;
        }

        log.info("Registering JavaMelody listener");

        Class<SessionListener> sessionListenerClass = ReflectionHelper.getClass(SessionListener.class.getName());

        ServletContext servletContext = e.getSource();
        try {
            servletContext.addListener(servletContext.createListener(sessionListenerClass));
        } catch (ServletException ex) {
            log.info("Servlet exception occurred while registering JavaMelody Security filter: {}", e);
        }

        log.info("JavaMelody listener registered");
    }

    private boolean singleWarDeployment(ServletContext sc) {
        List<? extends FilterRegistration> monitoringFilters = sc.getFilterRegistrations().values()
                .stream()
                .filter(fr -> MonitoringFilter.class.getName().equals(fr.getClassName()))
                .collect(Collectors.toList());

        return monitoringFilters.size() > 1;
    }
}
