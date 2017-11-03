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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for resolving filtering policies of the resources that are being queried for.
 * <p>
 * Created by Szymon Mueller on 10/10/2017.
 */
@Component
public class SecurityManager implements IFilteringManager {

    private final Log log = LogFactory.getLog(SecurityManager.class);

    private final Boolean securityEnabled;
    private final AccessPolicyRepo accessPolicyRepo;
    IComponentSecurityHandler componentSecurityHandler;
    private SecurityCache<SecurityCacheKey, Boolean> cache = new SecurityCache<>(150);

    @Autowired
    public SecurityManager(AccessPolicyRepo accessPolicyRepo,
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

    public String generateSecurityResponse() throws SecurityHandlerException {
        if (securityEnabled) {
            return componentSecurityHandler.generateServiceResponse();
        }
        return "";
    }

    @Override
    public boolean checkPolicyByResourceId(String resourceId, SecurityRequest request) {
        return checkPolicy(accessPolicyRepo.findById(resourceId), request);
    }

    @Override
    public boolean checkPolicyByResourceIri(String resourceIri, SecurityRequest request) {
        return checkPolicy(accessPolicyRepo.findByIri(resourceIri), request);
    }

    private boolean checkPolicy(Optional<AccessPolicy> policy, SecurityRequest request) {
        boolean result = true;
        if (request == null) {
            log.info("Security Request is null");
            result = false;
        } else {
            if (policy.isPresent()) {
                //Check in cache
                String resourceId = policy.get().getResourceId();
                SecurityCacheKey securityCacheKey = new SecurityCacheKey(request, resourceId);
                if (!cache.get(securityCacheKey)) {
                    log.debug("No cache available for res " + resourceId + " and request timestamp + "
                            + request.getTimestamp() + ". Checking policy");
                    Map<String, IAccessPolicy> accessPolicyMap = new HashMap<>();
                    if (resourceId != null) {
                        if (policy.get().getPolicy() != null) {
                            accessPolicyMap.put(resourceId, policy.get().getPolicy());
                            Set<String> ids = componentSecurityHandler.getSatisfiedPoliciesIdentifiers(accessPolicyMap, request);
                            if (!ids.contains(resourceId)) {
                                log.debug("Security Policy is not valid for res: " + resourceId);
                                result = false;
                            } else {
                                log.debug("Security Policy is valid " + resourceId);
                                result = true;
                            }
                            cache.put(securityCacheKey, Boolean.valueOf(result));
                        } else {
                            log.debug("Policy is null");
                        }
                    } else {
                        log.debug("Resource id of policy is null");
                    }
                } else {
                    result = cache.get(securityCacheKey);
                    log.debug("Cache available for res " + resourceId + " and request timestamp + "
                            + request.getTimestamp() + ". Result: " + result);
                }
            } else {
                log.debug("No policy to check is present");
            }
        }
//        Random rand = new Random();
//        result = rand.nextBoolean();
//
//        log.debug("Checking if policy is right for resource " + policy.get().getResourceId() + " result: " + result );

        return result;
    }

}
