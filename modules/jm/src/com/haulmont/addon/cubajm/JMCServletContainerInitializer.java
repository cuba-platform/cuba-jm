package com.haulmont.addon.cubajm;

import com.haulmont.bali.util.ReflectionHelper;
import net.bull.javamelody.MonitoringFilter;
import net.bull.javamelody.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import java.util.EnumSet;
import java.util.Set;

@SuppressWarnings("unchecked")
public class JMCServletContainerInitializer implements ServletContainerInitializer {
    private static final String JM_SEC_FILTER_FQN = "com.haulmont.addon.cubajm.JavaMelodySecurityFilter";
    private static final String JM_FILTER_FQN = "net.bull.javamelody.MonitoringFilter";

    private final Logger log = LoggerFactory.getLogger(JMCServletContainerInitializer.class);

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) {
        log.info("JMCServletContainerInitializer started");

        registerJMSecurityFilter(ctx);

        registerJavaMelodyFilter(ctx);

        registerJavaMelodyListener(ctx);

        log.info("JMCServletContainerInitializer finished");
    }

    private void registerJMSecurityFilter(ServletContext ctx) {
        log.info("Registering JavaMelody security filter");

        try {
            Class<JavaMelodySecurityFilter> filterClass = (Class<JavaMelodySecurityFilter>) ReflectionHelper
                    .loadClass(JM_SEC_FILTER_FQN);

            FilterRegistration.Dynamic filterReg = ctx.addFilter("jmc_filter", filterClass);

            filterReg.addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "/monitoring/*"
            );
        } catch (ClassNotFoundException e) {
            log.warn("Cannot load Filter class: {}", JM_SEC_FILTER_FQN);
        }

        log.info("JavaMelody security filter registered");
    }

    private void registerJavaMelodyFilter(ServletContext ctx) {
        log.info("Registering JavaMelody filter");

        try {
            Class<MonitoringFilter> filterClass = (Class<MonitoringFilter>) ReflectionHelper
                    .loadClass(JM_FILTER_FQN);

            FilterRegistration.Dynamic filterReg = ctx.addFilter("javamelody", filterClass);

            filterReg.addMappingForUrlPatterns(
                    EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC), true, "/*"
            );
        } catch (ClassNotFoundException e) {
            log.warn("Cannot load Filter class: {}", JM_FILTER_FQN);
        }

        log.info("JavaMelody filter registered");
    }

    private void registerJavaMelodyListener(ServletContext ctx) {
        log.info("Registering JavaMelody listener");

        try {
            Class<SessionListener> sessionListenerClass = (Class<SessionListener>) ReflectionHelper
                    .loadClass("net.bull.javamelody.SessionListener");

            ctx.addListener(ctx.createListener(sessionListenerClass));
        } catch (ClassNotFoundException e) {
            log.warn("Cannot load Listener class: {}", "net.bull.javamelody.SessionListener");
        } catch (ServletException e) {
            log.warn("Servlet exception while registering JM Security filter: {}", e);
        }

        log.info("JavaMelody listener registered");
    }
}
