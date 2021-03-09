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

import com.haulmont.cuba.core.config.AppPropertiesLocator;
import com.haulmont.cuba.core.sys.AppContext;
import com.haulmont.cuba.core.sys.servlet.ServletRegistrationManager;
import net.bull.javamelody.MonitoringFilter;
import org.springframework.context.ApplicationContext;

import javax.inject.Inject;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.Callable;

public class JavaMelodyRegistrarCore implements Callable<Boolean> {
    @Override
    public Boolean call() throws UnknownHostException {

        String applicationName = null;
        String address = null;
        URL collectServerUrl = null;

        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            applicationName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            collectServerUrl = new URL("http://javamelody:1337/");
//            collectServerUrl = new URL("http://localhost:1337/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        URL applicationCoreNodeUrl = null;
        try {
            applicationCoreNodeUrl = new URL("http://" + applicationName + "@" + address + ":"
                    + AppContext.getProperty("cuba.webPort") + "/" + AppContext.getProperty("cuba.webContextName"));
//            applicationCoreNodeUrl = new URL("http://" + address + ":" + 7777 + "/" + AppContext.getProperty("cuba.webContextName"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        System.out.println("\n\n\n");
        System.out.println("CORE URL = " + applicationCoreNodeUrl);
        System.out.println("HOST NAME + " + AppContext.getProperty("cuba.webHostName"));
        System.out.println("HOST NAME INET ADDRESS" + InetAddress.getLocalHost().getHostName());
        System.out.println("\n\n\n");



        MonitoringFilter.registerApplicationNodeInCollectServer("CORE" +
                String.valueOf((new Random()).nextInt(100)), collectServerUrl, applicationCoreNodeUrl);
        return true;
    }
}
