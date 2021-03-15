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

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.ServletContextHolder;



import com.haulmont.cuba.core.sys.SingleAppResourcePatternResolver;
import com.haulmont.cuba.core.sys.events.AppContextInitializedEvent;
import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextDestroyedEvent;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import net.bull.javamelody.MonitoringFilter;
import net.bull.javamelody.SessionListener;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.ServletContextAwareProcessor;
import org.springframework.web.context.support.ServletContextScope;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class JMInitializer1 {
    private static final String JAVAMELODY_FILTER_URL_PROP = "cubajm.monitoringUrl";
    private static final String MONITORING_PATH_PARAM = "monitoring-path";

    private final Logger log = LoggerFactory.getLogger(JMInitializer1.class);

    @Inject
    private ServletRegistrationManager servletRegistrationManager;
    @Inject
    private JavaMelodyConfig javaMelodyConfig;

    private boolean initialized;
    private boolean skipRegistrationCustomFilter;
    private String jmFilterUrl;

//    private boolean getClusterStatus() {
//        return BooleanUtils.toBoolean(AppContext.getProperty("cuba.cluster.enabled"));
//    }

    @EventListener
    public void initialize(ServletContextInitializedEvent e) {
        ClassLoader classLoader = e.getSource().getClassLoader();
        String parent = classLoader.getName();
//        ServletContextAwareProcessor processor = new ServletContextAwareProcessor(e.getSource());
//        processor.

        System.out.println("\n\n\n");
        e.getSource().getFilterRegistrations().forEach((name, filter) -> {
            System.out.println("FILTER REG CLASS NAME = " + filter.getClassName());
            System.out.println("FILTER REG CANONICL CLASS NAME = " + filter.getClass().getCanonicalName());
            System.out.println();
        });
        System.out.println("\n\n\n");
        e.getSource().getServletRegistrations().forEach((name, servlet) -> {
            System.out.println("Servlet CLASS NAME = " + servlet.getClassName());
            System.out.println("Servlet NAME = " + servlet.getName());
            System.out.println();
        });

        String name = AppContext.getApplicationContext().getClassLoader().getName();

        System.out.println("\n\n\n");
        System.out.println("NAME1 = " + parent);
        System.out.println("NAME2 = " + name);
        System.out.println("\n\n\n");

        if (singleWarDeployment(e.getSource())) {
            String msg = String.format("SingleWAR deployment detected. JavaMelody monitoring will be available " +
                    "by the URL defined in application property %s for the \"core\" module", JAVAMELODY_FILTER_URL_PROP);
            log.info(msg);
            return;
        } else {
        }

        e.getSource().getFilterRegistrations().forEach( (s, fr) -> {
            System.out.println("\n\n\n");
            System.out.println("FR name");
            System.out.println(fr.getName());
            System.out.println("FR init params");
            fr.getInitParameters().forEach( (s1 ,s2) -> System.out.println(s1 + ": " + s2));
            System.out.println("FR servlet name mappings");
            fr.getServletNameMappings().forEach(System.out::println);
            System.out.println("FR url pattern mappings");
            fr.getUrlPatternMappings().forEach(System.out::println);
            System.out.println("\n\n\n");
        });





        jmFilterUrl = javaMelodyConfig.getMonitoringUrl();
        if (jmFilterUrl == null || jmFilterUrl.isEmpty()) {
            log.info("Value of application property '{}' is not defined." +
                            "JavaMelody monitoring will be enabled for this application block by URL '{}'",
                    JAVAMELODY_FILTER_URL_PROP, "/monitoring");
            jmFilterUrl = "/monitoring";
//            skipRegistrationCustomFilter = true;
        }
        if (jmFilterUrl.equals("/monitoring")) {
//            skipRegistrationCustomFilter = true;
        }




        if (!initialized) {

            if (AppContext.getProperty("cubajm.monitoringServerUrl") == null) {
                initializeSecurityFilter(e);
            }

            initializeJavaMelodyFilter(e);
            initializeJavamelodyListener(e);
            initialized = true;
        }

        //If needed register node on collector server
        if (AppContext.getProperty("cubajm.monitoringServerUrl") != null) {
            if (!skipRegistrationCustomFilter) {
                ExecutorService executor;
                try {
                    executor = Executors.newFixedThreadPool(1);
                    executor.submit(new RegistrarOfNodesOnCollectorServer()).get();
                    executor.shutdown();
                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                }
            } else {
                log.warn("You have installed a unique URL monitoring. " +
                        "Now, you will not be able to register your application with the collector server. " +
                        "If you want to change this, please remove the \"cubajm.monitoringUrl\" property " +
                        "or set it to \"/monitoring\".");
            }
        }
    }


    @EventListener
    public void destroy(ServletContextDestroyedEvent event) {
        try {
            MonitoringFilter.unregisterApplicationNodeInCollectServer();
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    private void initializeSecurityFilter(ServletContextInitializedEvent e) {
        log.info("Registering JavaMelody security filter");
        jmFilterUrl = javaMelodyConfig.getMonitoringUrl();
        String filtersNameBase;
        if (jmFilterUrl == null || jmFilterUrl.isEmpty()) {
            log.info("Value of application property '{}' is not defined." +
                            "JavaMelody monitoring will be enabled for this application block by URL '{}'",
                    JAVAMELODY_FILTER_URL_PROP, "/monitoring");
            jmFilterUrl = "/monitoring";
//            skipRegistrationCustomFilter = true;
        }
        if (jmFilterUrl.equals("/monitoring")) {
//            skipRegistrationCustomFilter = true;
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
        if (skipRegistrationCustomFilter) {
            return;
        }
        log.info("Registering custom JavaMelody monitoring filter");
        Filter javamelodyFilter = servletRegistrationManager.createFilter(e.getApplicationContext(),
                MonitoringFilter.class.getName());
        String filterName = "custom_javamelody_filter";
        ServletContext servletContext = e.getSource();
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
        FilterRegistration.Dynamic javamelody = servletContext.addFilter(filterName, javamelodyFilter);
        javamelody.setInitParameter(MONITORING_PATH_PARAM, jmFilterUrl);
        javamelody.setAsyncSupported(true);
        javamelody.addMappingForUrlPatterns(
                EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "/*");
        log.info("Custom JavaMelody monitoring filter registered");
    }

    private void initializeJavamelodyListener(ServletContextInitializedEvent e) {
        log.info("Registering JavaMelody listener");
        ServletContext servletContext = e.getSource();
        try {
            servletContext.addListener(servletContext.createListener(SessionListener.class));
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
        System.out.println("SIZE = " + monitoringFilters.size());
        return monitoringFilters.size() == 1;
    }
}
