package com.haulmont.addon.cubajm;

import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.ConditionalOnAppProperty;
import net.bull.javamelody.MonitoringFilter;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;

@Component
@ConditionalOnAppProperty(property = "javamelody.centralized.monitoring", value = "true")
public class JavaMelodyRegistrar implements AppContext.Listener {

    @Inject
    private JavaMelodyConfig javaMelodyConfig;

    @Inject
    private GlobalConfig globalConfig;

    @Inject
    private Logger log;

    public JavaMelodyRegistrar() {
        AppContext.addListener(this);
    }

    @Override
    public void applicationStarted() {
        String javaMelodyServerAddress = javaMelodyConfig.getJavaMelodyServerAddress();
        String webAppUrl = globalConfig.getWebAppUrl();
        String appName = globalConfig.getWebContextName();
        log.info("Registering application {} with URL {} in javamelody server: {}", appName, webAppUrl, javaMelodyServerAddress);
        // url of the collect server
        try {
            URL collectServerUrl = new URL(javaMelodyServerAddress);
            // url of the application node to be called by the collect server to collect data
            URL applicationNodeUrl = new URL(webAppUrl);
            MonitoringFilter.registerApplicationNodeInCollectServer(
                    appName, collectServerUrl, applicationNodeUrl);
        } catch (MalformedURLException e) {
            log.error("Cannot register application in the monitoring server: "+e.getMessage(), e);
        }
    }

    @Override
    public void applicationStopped() {

    }
}
