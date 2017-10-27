package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.ComponentSecurityHandlerFactory;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Utility class for resolving filtering policies of the resources that are being queried for.
 *
 * Created by Szymon Mueller on 10/10/2017.
 */
@Component
public class SecurityManager implements IFilteringManager {

    private final Log log = LogFactory.getLog(SecurityManager.class);

    private final Boolean securityEnabled;
    private final AccessPolicyRepo accessPolicyRepo;
    IComponentSecurityHandler componentSecurityHandler;

    @Autowired
    public SecurityManager( AccessPolicyRepo accessPolicyRepo,
                            @Value("${aam.deployment.owner.username}") String componentOwnerName,
                            @Value("${aam.deployment.owner.password}") String componentOwnerPassword,
                            @Value("${aam.environment.aamAddress}") String aamAddress,
                            @Value("${aam.environment.clientId}") String clientId,
                            @Value("${aam.environment.keystoreName}") String keystoreName,
                            @Value("${aam.environment.keystorePass}") String keystorePass,
                            @Value("${search.security.enabled}") Boolean securityEnabled) throws SecurityHandlerException {
        this.securityEnabled = securityEnabled;
        this.accessPolicyRepo = accessPolicyRepo;
        if (securityEnabled) {
            componentSecurityHandler = ComponentSecurityHandlerFactory.getComponentSecurityHandler(aamAddress,
                    keystoreName,
                    keystorePass,
                    clientId,
                    aamAddress,
                    false,
                    componentOwnerName,
                    componentOwnerPassword);
        }
    }

    @Override
    public boolean checkPolicyByResourceId(String resourceId, SecurityRequest request ) throws Exception {
        return checkPolicy(accessPolicyRepo.findById(resourceId), request);
    }

    @Override
    public boolean checkPolicyByResourceIri(String resourceIri, SecurityRequest request) throws Exception {
        return checkPolicy(accessPolicyRepo.findByIri(resourceIri),request);
    }

    private boolean checkPolicy( Optional<AccessPolicy> policy, SecurityRequest request ) throws Exception {
        boolean result = true;

        //TODO uncomment for proper validation
        if( policy.isPresent() ) {
            Map<String, IAccessPolicy> accessPolicyMap = new HashMap<>();
            accessPolicyMap.put(policy.get().getResourceId(), policy.get().getPolicy());
            Set<String> ids = componentSecurityHandler.getSatisfiedPoliciesIdentifiers(accessPolicyMap, request);
            if(!ids.contains(policy.get().getResourceId())) {
                throw new Exception("Security Policy is not valid");
            }
        }

        Random rand = new Random();
        result = rand.nextBoolean();

        log.debug("Checking if policy is right for resource " + policy.get().getResourceId() + " result: " + result );

        return result;
    }

}
