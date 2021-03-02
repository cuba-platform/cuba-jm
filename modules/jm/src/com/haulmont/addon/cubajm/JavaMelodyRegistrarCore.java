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

import net.bull.javamelody.MonitoringFilter;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;

public class JavaMelodyRegistrarCore implements Callable<Boolean> {

//    @Inject
//    private Logger log;
//    @Inject
//    GlobalConfig globalConfig;

    @Override
    public Boolean call() throws Exception {

        String applicationName = null;
        String address = null;
        URL collectServerUrl = null;

//        String address = globalConfig.getWebHostName();
//        String applicationName = globalConfig.getWebContextName();

        try {
            address = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
//
        try {
            applicationName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            collectServerUrl = new URL("http://javamelody:1337/");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }


        URL applicationCoreNodeUrl = null;
        try {
            applicationCoreNodeUrl = new URL("http://" + address + ":" + 8079 + "/app-core");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        MonitoringFilter.registerApplicationNodeInCollectServer(applicationName, collectServerUrl, applicationCoreNodeUrl);
        return true;
    }
}
