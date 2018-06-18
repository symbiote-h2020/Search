package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Szymon Mueller on 07/11/2017.
 */
@Component
public class SecurityHandlerComponent {

    private IComponentSecurityHandler handler;

    private Log log = LogFactory.getLog(SecurityHandlerComponent.class);

    private boolean securityEnabled;

    @Autowired
    public SecurityHandlerComponent(@Value("${aam.deployment.owner.username}") String componentOwnerName,
                                    @Value("${aam.deployment.owner.password}") String componentOwnerPassword,
                                    @Value("${aam.environment.aamAddress}") String aamAddress,
                                    @Value("${aam.environment.clientId}") String clientId,
                                    @Value("${aam.environment.keystoreName}") String keystoreName,
                                    @Value("${aam.environment.keystorePass}") String keystorePass,
                                    @Value("${symbIoTe.validation.localaam}") Boolean alwaysUseLocalAAMForValidation,
                                    @Value("${search.security.enabled}") Boolean securityEnabled)
            throws SecurityHandlerException {
        this.securityEnabled = securityEnabled;
        if (securityEnabled) {
            log.info("Creating security handler for component");
            handler = ComponentSecurityHandlerFactory.getComponentSecurityHandler(aamAddress,
                    keystoreName,
                    keystorePass,
                    clientId,
                    aamAddress,
                    alwaysUseLocalAAMForValidation,
                    componentOwnerName,
                    componentOwnerPassword);
        } else {
            log.info("Security disabled - skipping creating security handler");
            handler = null;
        }
    }

    public IComponentSecurityHandler getHandler() {
        return handler;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }
}
