package com.haulmont.addon.cubajm;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.DefaultString;

/**
 * JavaMelody configuration parameters interface.
 */
@Source(type = SourceType.APP)
public interface JavaMelodyConfig extends Config {

    /**
     * @return login of a user that is authorized to open monitoring dashboard
     */
    @Property("cubajm.authorizedUserLogin")
    @DefaultString("admin")
    String getAuthorizedUserLogin();

    /**
     * @return password of a user that is authorized to open monitoring dashboard
     */
    @Property("cubajm.authorizedUserPassword")
    @DefaultString("admin")
    String getAuthorizedUserPassword();

    /**
     * @return the URL postfix to access to the monitoring dashboard. For example, for the middleware block it equals
     * to "/core-jm/
     */
    @Property("cubajm.monitoringUrl")
    String getMonitoringUrl();
}
