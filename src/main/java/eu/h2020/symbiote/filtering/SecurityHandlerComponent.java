package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Created by Szymon Mueller on 07/11/2017.
 */
@Component
public class SecurityHandlerComponent {

    private IComponentSecurityHandler handler;

    @Autowired
    public SecurityHandlerComponent(@Value("${aam.deployment.owner.username}") String componentOwnerName,
                           @Value("${aam.deployment.owner.password}") String componentOwnerPassword,
                           @Value("${aam.environment.aamAddress}") String aamAddress,
                           @Value("${aam.environment.clientId}") String clientId,
                           @Value("${aam.environment.keystoreName}") String keystoreName,
                           @Value("${aam.environment.keystorePass}") String keystorePass,
                           @Value("${symbIoTe.validation.localaam}") Boolean alwaysUseLocalAAMForValidation)
            throws SecurityHandlerException {
            handler = ComponentSecurityHandlerFactory.getComponentSecurityHandler(aamAddress,
                    keystoreName,
                    keystorePass,
                    clientId,
                    aamAddress,
                    alwaysUseLocalAAMForValidation,
                    componentOwnerName,
                    componentOwnerPassword);
        }

    public IComponentSecurityHandler getHandler() {
        return handler;
    }
}
