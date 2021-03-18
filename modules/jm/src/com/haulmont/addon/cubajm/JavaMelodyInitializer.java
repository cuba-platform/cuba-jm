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

import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextDestroyedEvent;
import com.haulmont.cuba.core.sys.servlet.events.ServletContextInitializedEvent;
import net.bull.javamelody.MonitoringFilter;
import net.bull.javamelody.SessionListener;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.servlet.*;
import java.io.IOException;
import java.net.*;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

@Component("cubajm_JavaMelodyInitializer")
public class JavaMelodyInitializer {
    private static final String JAVAMELODY_FILTER_URL_PROP = "cubajm.monitoringUrl";
    private static final String JAVAMELODY_COLLECTOR_SERVER_URL_PROP = "cubajm.monitoringServerUrl";
    private static final String MONITORING_PATH_PARAM = "monitoring-path";
    private static final String DEFAULT_MONITORING_URL = "/monitoring";
    private final Logger log = LoggerFactory.getLogger(JavaMelodyInitializer.class);
    @Inject
    private ServletRegistrationManager servletRegistrationManager;
    @Inject
    private JavaMelodyConfig javaMelodyConfig;
    private boolean skipRegistrationCustomFilter;
    private String jmFilterUrl;

    @EventListener
    public void initialize(ServletContextInitializedEvent e) {

        jmFilterUrl = javaMelodyConfig.getMonitoringUrl();
        if (jmFilterUrl == null || jmFilterUrl.isEmpty()) {
            log.info("Value of application property '{}' is not defined." +
                            "JavaMelody monitoring will be enabled for this application block by URL '{}'",
                    JAVAMELODY_FILTER_URL_PROP, DEFAULT_MONITORING_URL);
            jmFilterUrl = DEFAULT_MONITORING_URL;
            skipRegistrationCustomFilter = true;
        } else if (jmFilterUrl.equals(DEFAULT_MONITORING_URL)) {
            skipRegistrationCustomFilter = true;
        }

        //It will be false, if used single war deployment.
        if (e.getSource().getFilterRegistration("custom_javamelody_filter") == null) {
            initJavaMelody(e);
        } else {
            String msg = String.format("SingleWAR deployment detected. JavaMelody monitoring will be available " +
                    "by the URL defined in application property %s for the \"core\" module (default \"%s\") " +
                    "or on collector-server, if property \"%s\" is specified.", JAVAMELODY_FILTER_URL_PROP, DEFAULT_MONITORING_URL, JAVAMELODY_COLLECTOR_SERVER_URL_PROP);
            log.info(msg);
        }
    }


    private void initJavaMelody(ServletContextInitializedEvent e) {
        initializeSecurityFilter(e);
        initializeJavaMelodyFilter(e);
        initializeJavamelodyListener(e);
        registrOnCollectorServer(e.getSource());
    }


    private void initializeSecurityFilter(ServletContextInitializedEvent e) {
        log.info("Registering JavaMelody security filter");
        String filtersNameBase;
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
        if (skipRegistrationCustomFilter && !singleWarDeployment(e.getSource())) {
            return;
        }
        log.info("Registering custom JavaMelody monitoring filter");
        MonitoringFilter javamelodyFilter = new MonitoringFilter();
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
            log.info("Servlet exception occurred while registering JavaMelody listener: {}", e);
        }
        log.info("JavaMelody listener registered");
    }


    /**
     *
     * @param context - current servlet context
     * @return true, if servletContext not contains MonitoringFilter
     * (it possible, when the file web-fragment.xml defined in javamelody-core is NOT taken into account)
     */
    private boolean singleWarDeployment(ServletContext context) {
        List<? extends FilterRegistration> monitoringFilters = context.getFilterRegistrations().values()
                .stream()
                .filter(fr -> MonitoringFilter.class.getName().equals(fr.getClassName()))
                .collect(Collectors.toList());
        return monitoringFilters.size() == 0;
    }

    private void registrOnCollectorServer(ServletContext context) {
        if (AppContext.getProperty(JAVAMELODY_COLLECTOR_SERVER_URL_PROP) != null) {
            if (skipRegistrationCustomFilter) {
                URL url = registrNodeOnCollectorServer(context);
                if (url != null) {
                    log.info("Monitoring your application available by next URL: {}", url);
                } else {
                    log.error("Error about registration application for monitoring.");
                }
            } else {
                log.warn("You have installed a unique URL monitoring. " +
                        "Now, you will not be able to register your application with the collector server. " +
                        "If you want to change this, please remove the \"{}}\" property " +
                        "or set it to \"{}\".",JAVAMELODY_FILTER_URL_PROP, DEFAULT_MONITORING_URL);
            }
        }
    }

    private URL registrNodeOnCollectorServer(ServletContext context) {
        String address;
        URL collectServerUrl;
        URL applicationNodeUrl;
        URL defaultApplicationMonitoringUrl;
        String webPort = AppContext.getProperty("cuba.webPort");
        String webContextName = context.getContextPath();
        String authorizedUserLogin = javaMelodyConfig.getAuthorizedUserLogin();
        String authorizedUserPassword = javaMelodyConfig.getAuthorizedUserPassword();

        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.error("IP address of a host could not be determined!", e);
            return null;
        }

        try {
            if (!StringUtils.isEmpty(authorizedUserLogin) && !StringUtils.isEmpty(authorizedUserPassword)) {
                applicationNodeUrl = new URL("http://" + authorizedUserLogin + ":" + authorizedUserPassword + "@"
                        + address + ":" + webPort + webContextName);
            } else {
                applicationNodeUrl = new URL("http://" + address + ":" + webPort + webContextName);
            }
        } catch (MalformedURLException e) {
            log.error("Error creating application node URL!", e);
            return null;
        }

        try {
            defaultApplicationMonitoringUrl = new URL(applicationNodeUrl.toString() + jmFilterUrl);
        } catch (MalformedURLException e) {
            log.error("Default application monitoring URL cannot be created. Check property {}", JAVAMELODY_FILTER_URL_PROP, e);
            return null;
        }

        try {
            collectServerUrl = new URL(javaMelodyConfig.getJavaMelodyServerAddress());
        } catch (MalformedURLException e) {
            log.error("Collector-server URL specified incorrectly!", e);
            return defaultApplicationMonitoringUrl;
        }

        MonitoringFilter.registerApplicationNodeInCollectServer(AppContext.getProperty("cuba.webHostName") + webContextName,
                collectServerUrl, applicationNodeUrl);

        return collectServerUrl;
    }


    @EventListener
    public void destroy(ServletContextDestroyedEvent event) {
        try {
            MonitoringFilter.unregisterApplicationNodeInCollectServer();
        } catch (IOException e) {
            log.error("Error unregistering application node from collector-server", e);
        }
    }

}
