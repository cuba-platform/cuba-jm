# CUBA JavaMelody monitoring integration Add-on
## Overview

The add-on adds an ability to use JavaMelody monitoring in CUBA-based
applications.

JavaMelody: monitoring of JavaEE applications
[[GitHub](https://github.com/javamelody/javamelody/wiki)].

![JavaMelody Monitoring dashboard](https://github.com/javamelody/javamelody/wiki/resources/screenshots/graphs.png)

## Compatibility with platform versions

| Add-on        | Platform      |
|:------------- |:------------- |
| 0.1.0         | 6.8.0         |

## Installation and configuration

1. add custom application component to your project via CUBA Studio
(Project properties > Edit):
* Artifact group: `com.haulmont.addon.cubajm`
* Artifact name: `cuba-jm-global`
* Version: 0.1.0
2. configure monitoring URLs with the `cubajm.monitoringUrl` application
property. Default values are `/core-jm/` and `/web-jm/` for middleware
and web tiers respectively;
3. configure monitoring dashboard authorization credentials with
`cubajm.authorizedUserLogin` & `cubajm.authorizedUserPassword`
application properties. The default values are `admin`, `admin`;
4. declare "javamelody-core" dependency as appJar in the parent project
for the `core` and `web` modules:

        // example for the 'web' module
        configure(webModule) {
            ...
            task deploy(dependsOn: [assemble, cleanConf], type: CubaDeployment) {
                appName = "${modulePrefix}"
                appJars("${modulePrefix}-global", "${modulePrefix}-gui", "${modulePrefix}-web", "javamelody-core")
            }
            ...
        }

All described here application properties can also be found in the
`JavaMelodyConfig` configuration interface.

## Usage

When the add-on is installed and configured you can access to the
monitoring dashboard by the URL:
`http://<host>:<tier_port>/<context>/<monitoring_url>/`.
Where:
1. `<host>` and `<tier_port>` are the location of a tier that interests us;
2. `<context>` is the name of the context of the tier;
3. `<monitoring_url>` equals to a value that is used for the
`cubajm.monitoringUrl` application property.

**Pay attention**: trailing slash should be present in the URL.

### Example URLs for default settings:
1. [http://localhost:8080/app/web-jm/](http://localhost:8080/app/web-jm/) - web client monitoring
2. [http://localhost:8080/app-core/core-jm/](http://localhost:8080/app-core/core-jm/) - middleware monitoring

**Pay attention**: in case of SingleWAR deployment the single monitoring
dashboard is used for both middleware and web tiers. It's available
by the monitoring URL configured for the middleware block.