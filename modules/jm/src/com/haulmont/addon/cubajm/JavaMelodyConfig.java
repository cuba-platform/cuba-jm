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
     * @return credentials of user that is authorized to open monitoring dashboard
     */
    @Property("cubajm.authorizedUserCredentials")
    @DefaultString("admin:admin")
    String getAuthorizedUserCredentials();
}
