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
import net.bull.javamelody.MonitoringFilter;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Registrar {

    /*

    cuba.useLocalServiceInvocation
    При установке данного свойства в true блоки Web Client и Web Portal вызывают сервисы Middleware в обход сетевого стека,
    что положительно сказывается на производительности системы. Это возможно в случае быстрого развертывания, единого WAR и единого Uber JAR.
    В других вариантах развертывания данное свойство необходимо установить в false.

    Значение по умолчанию: true

    Используется в блоках Web Client и Web Portal.

     */

    private static boolean isSingleWar = false;

//    @EventListener
//    public void isSingleWarDeployment(ServletContextInitializedEvent e) {
//        if (singleWarDeployment(e.getSource())) {
//            isSingleWar = true;
//        }
//    }

    @EventListener
    public void applicationStarted(AppContextStartedEvent event) throws ExecutionException, InterruptedException {

//        if (BooleanUtils.toBoolean(AppContext.getProperty("cuba.useLocalServiceInvocation")))


        ExecutorService service;
        if (isSingleWar) {
            service = Executors.newFixedThreadPool(1);
            service.submit(new JavaMelodyRegistrar()).get();
            service.shutdown();
        } else {
            service = Executors.newFixedThreadPool(2);
            service.submit(new JavaMelodyRegistrarCore()).get();
            service.submit(new JavaMelodyRegistrar()).get();
            service.shutdown();
        }

    }

    @EventListener
    public void applicationStopped(AppContextStartedEvent event) {
        try {
            MonitoringFilter.unregisterApplicationNodeInCollectServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private boolean singleWarDeployment(ServletContext sc) {
//        List<? extends FilterRegistration> monitoringFilters = sc.getFilterRegistrations().values()
//                .stream()
//                .filter(fr -> MonitoringFilter.class.getName().equals(fr.getClassName()))
//                .collect(Collectors.toList());
//
//        return monitoringFilters.size() > 1;
//    }
}
