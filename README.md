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
| 0.3.0         | 6.9.0         |
| 0.2.0         | 6.8.1         |
| 0.1.0         | 6.8.0         |

## Installation and configuration

1. Add custom application component to your project (change the version part if needed):

    `com.haulmont.addon.cubajm:cuba-jm-global:0.2.0`
  
2. Configure monitoring URLs with the `cubajm.monitoringUrl` application
property. Default values are `/core-jm/` and `/web-jm/` for middleware
and web tiers respectively;
3. Configure monitoring dashboard authorization credentials with
`cubajm.authorizedUserLogin` & `cubajm.authorizedUserPassword`
application properties. The default values are `admin`, `admin`;

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
