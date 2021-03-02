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

import com.haulmont.cuba.core.global.GlobalConfig;
import net.bull.javamelody.MonitoringFilter;
import org.slf4j.Logger;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class JavaMelodyRegistrar implements Callable<Boolean> {

    @Inject
    private JavaMelodyConfig javaMelodyConfig;

    @Inject
    private GlobalConfig globalConfig;

    @Inject
    private Logger log;

    @Override
    public Boolean call() throws Exception {
        String address = null;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        URL collectServerUrl = null;
        try {
            collectServerUrl = new URL("http://javamelody:1337/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        String applicationName = null;
        try {
            applicationName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        URL applicationWebNodeUrl = null;
        try {
            applicationWebNodeUrl = new URL("http://" + address + ":" + 8080 + "/app");

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        MonitoringFilter.registerApplicationNodeInCollectServer(applicationName, collectServerUrl, applicationWebNodeUrl);
        return true;
    }
}



//        String javaMelodyServerAddress = javaMelodyConfig.getJavaMelodyServerAddress();
//        String webAppUrl = globalConfig.getWebAppUrl();
//        String appName = globalConfig.getWebContextName();
//        log.info("Registering application {} with URL {} in javamelody server: {}", appName, webAppUrl, javaMelodyServerAddress);
//        // url of the collect server
//        try {
//            URL collectServerUrl = new URL(javaMelodyServerAddress);
//            // url of the application node to be called by the collect server to collect data
//            URL applicationNodeUrl = new URL(webAppUrl);
//            MonitoringFilter.registerApplicationNodeInCollectServer(
//                    appName, collectServerUrl, applicationNodeUrl);
//        } catch (MalformedURLException e) {
//            log.error("Cannot register application in the monitoring server: "+e.getMessage(), e);
//        }