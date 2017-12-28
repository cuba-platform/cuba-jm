# jm-component
CUBA application component that adds an ability to use JavaMelody monitoring in CUBA-based applications.

JavaMelody: monitoring of JavaEE applications [[GitHub](https://github.com/javamelody/javamelody/wiki)].

![JavaMelody Monitoring dashboard](https://github.com/javamelody/javamelody/wiki/resources/screenshots/graphs.png)

## Details
The app component uses Servlet 3.0+ interface: `ServletContainerInitializer` to register JavaMelody and security filters on context startup: [[Oracle Docs](https://docs.oracle.com/javaee/6/api/javax/servlet/ServletContainerInitializer.html)].

Security filter turns on basic auth to access to the JavaMelody monitoring dashboard. Authorized user credentials
can be found in the `JavaMelodyConfig` configuration interface.

To open monitoring dashboard add `/monitoring` after application context. Example: `http://localhost:8080/app/monitoring`  