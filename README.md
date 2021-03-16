# CUBA JavaMelody Monitoring Integration Add-On

[![license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Build Status](https://travis-ci.org/cuba-platform/cuba-jm.svg?branch=master)](https://travis-ci.org/cuba-platform/cuba-jm)

## Overview

The add-on adds an ability to use JavaMelody monitoring in CUBA-based
applications.

JavaMelody: monitoring of JavaEE applications
[[GitHub](https://github.com/javamelody/javamelody/wiki)].

![JavaMelody Monitoring dashboard](https://github.com/javamelody/javamelody/wiki/resources/screenshots/graphs.png)

## Installation

The add-on can be added to your project in one of the ways described below. Installation from the Marketplace is the simplest way. The last version of the add-on compatible with the used version of the platform will be installed.
Also, you can install the add-on by coordinates choosing the required version of the add-on from the table.

In case you want to install the add-on by manual editing or by building from sources see the complete add-ons installation guide in [CUBA Platform documentation](https://doc.cuba-platform.com/manual-latest/manual.html#app_components_usage).

### From the Marketplace

1. Open your application in CUBA Studio. Check the latest version of CUBA Studio on the [CUBA Platform site](https://www.cuba-platform.com/download/previous-studio/).
2. Go to *CUBA -> Marketplace* in the main menu.

 ![marketplace](img/marketplace.png)

3. Find the *JavaMelody* add-on there.

 ![addons](img/addons.png)

4. Click *Install* and apply the changes.
The add-on corresponding to the used platform version will be installed.

### By Coordinates

1. Open your application in CUBA Studio. Check the latest version of CUBA Studio on the [CUBA Platform site](https://www.cuba-platform.com/download/previous-studio/).
2. Go to *CUBA -> Marketplace* in the main menu.
3. Click the icon in the upper-right corner.

 ![by-coordinates](img/by-coordinates.png)

4. Paste the add-on coordinates in the corresponding field as follows:

 `com.haulmont.addon.cubajm:cuba-jm-global:<add-on version>`

where `<add-on version>` is compatible with the used version of the CUBA platform.

 | Platform Version| Add-on Version|
|:------------- |:------------- |
| 7.2.x         | 0.7.0         |
| 7.1.0         | 0.6.0         |
| 7.0.0         | 0.5.1         |
| 6.10.0        | 0.4.0         |
| 6.9.0         | 0.3.1         |
| 6.8.1         | 0.2.0         |
| 6.8.0         | 0.1.0         |

5. Click *Install* and apply the changes. The add-on will be installed to your project.

## Configuration

1. Configure monitoring URLs with the `cubajm.monitoringUrl` application property. Default value is `/monitoring`;
2. Configure monitoring dashboard authorization credentials with `cubajm.authorizedUserLogin` & `cubajm.authorizedUserPassword` application properties. The default values are `admin`, `admin`;

All described here application properties can also be found in the `JavaMelodyConfig` configuration interface.

## Usage

When the add-on is installed and configured you can access to the monitoring dashboard by the URL: `http://<host>:<tier_port>/<context>/<monitoring_url>/`.
Where:
1. `<host>` and `<tier_port>` are the location of a tier that interests us;
2. `<context>` is the name of the context of the tier;
3. `<monitoring_url>` equals to a value that is used for the
`cubajm.monitoringUrl` application property.
   
##Using collector server

If you want use centralized monitoring, you must:
1. Setup centralization collector-server (see https://github.com/javamelody/javamelody/wiki/UserGuideAdvanced#optional-centralization-server-setup)
2. In the `cubajm.monitoringServerUrl` property, specify the URL your collector-server.   

After it, all monitoring data will be available by specified URL.

**Pay attention**: to use this option, the `cubajm.monitoringUrl` property must have a default value `/monitoring`!
Also, I draw your attention to the fact, that after setting the collector-server - viewing the collected statistics on `cubajm.monitoringUrl` will not be available!

### Example URLs for default settings:
1. [http://localhost:8080/app/monitoring](http://localhost:8080/app/monitoring) - web client monitoring
2. [http://localhost:8080/app-core/monitoring](http://localhost:8080/app-core/monitoring) - middleware monitoring

**Pay attention**: in case of SingleWAR deployment the single monitoring dashboard is used for both middleware and web tiers. It's available by the monitoring URL configured for the middleware block.
