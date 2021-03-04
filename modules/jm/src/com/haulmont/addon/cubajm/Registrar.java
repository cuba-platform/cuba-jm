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
import com.haulmont.cuba.core.sys.events.AppContextStartedEvent;
import com.haulmont.cuba.core.sys.events.AppContextStoppedEvent;
import net.bull.javamelody.MonitoringFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
// implements AppContext.Listener
public class Registrar {

    private final Logger log = LoggerFactory.getLogger(Registrar.class);

//    public Registrar() {
//        AppContext.addListener(this);
//    }

//    @Override
    @EventListener
    public void applicationStarted(AppContextStartedEvent event) {
        ExecutorService service;
        try {
            if (AppContext.getAppComponents().getBlock().equals("core")) {
                System.out.println("\n\n\n");
                System.out.println("IN CORE MODULE");
                System.out.println("\n\n\n");
                service = Executors.newFixedThreadPool(1);
                service.submit(new JavaMelodyRegistrarCore()).get();
                service.shutdown();
            }
            if (AppContext.getAppComponents().getBlock().equals("web")) {
                System.out.println("\n\n\n");
                System.out.println("IN WEB MODULE");
                System.out.println("\n\n\n");
                service = Executors.newFixedThreadPool(1);
                service.submit(new JavaMelodyRegistrar()).get();
                service.shutdown();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

    }

//    @Override
    @EventListener
    public void applicationStopped(AppContextStoppedEvent event) {
        try {
            MonitoringFilter.unregisterApplicationNodeInCollectServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @EventListener
//    public void applicationStarted(AppContextStartedEvent event) throws ExecutionException, InterruptedException {
////        ((ServletContext) this.source).getServletContextName();
////        event.getSource()
//
//
////        if (BooleanUtils.toBoolean(AppContext.getProperty("cuba.useLocalServiceInvocation")))
//
////        try {
////            File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toString());
////            JarFile jarFile = new JarFile(file);
////
////        } catch (IOException e) {
////            log.error("file not found1!");
////        }
//
//
//
//        ExecutorService service;
//        if (isSingleWar) {
//            service = Executors.newFixedThreadPool(1);
//            service.submit(new JavaMelodyRegistrar()).get();
//            service.shutdown();
//        } else {
//            service = Executors.newFixedThreadPool(2);
//            service.submit(new JavaMelodyRegistrarCore()).get();
//            service.submit(new JavaMelodyRegistrar()).get();
//            service.shutdown();
//        }
//
//    }

//    @EventListener
//    public void applicationStopped(AppContextStartedEvent event) {
//        try {
//            MonitoringFilter.unregisterApplicationNodeInCollectServer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


//    private boolean singleWarDeployment(ServletContext sc) {
//        List<? extends FilterRegistration> monitoringFilters = sc.getFilterRegistrations().values()
//                .stream()
//                .filter(fr -> MonitoringFilter.class.getName().equals(fr.getClassName()))
//                .collect(Collectors.toList());
//
//        return monitoringFilters.size() > 1;
//    }
}
