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
import net.bull.javamelody.MonitoringFilter;

import javax.servlet.ServletContext;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class RegistrarOfNodesOnCollectorServer implements Callable<Boolean> {

//                ExecutorService executor;
//                try {
//                    executor = Executors.newFixedThreadPool(1);
//                    executor.submit(new RegistrarOfNodesOnCollectorServer()).get();
//                    executor.shutdown();
//                } catch (ExecutionException | InterruptedException ex) {
//                    log.warn(ex.getLocalizedMessage());
//                }

    @Override
    public Boolean call() throws Exception {

        String address = null;
        URL collectServerUrl = null;
        URL applicationNodeUrl = null;

        String webPort = AppContext.getProperty("cuba.webPort");
        String webContextName = AppContext.getProperty("cuba.webContextName");


        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            collectServerUrl = new URL(AppContext.getProperty("cubajm.monitoringServerUrl"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            applicationNodeUrl = new URL("http://" + address + ":" + webPort + "/" + webContextName);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        MonitoringFilter.registerApplicationNodeInCollectServer(AppContext.getProperty("cuba.webHostName") + "/" + webContextName,
                collectServerUrl, applicationNodeUrl);
        return true;

    }
}
