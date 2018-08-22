package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.SecurityCredentials;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for resolving filtering policies of the resources that are being queried for.
 * <p>
 * Created by Szymon Mueller on 10/10/2017.
 */
@Component
public class SecurityManager implements IFilteringManager {

    private final Log log = LogFactory.getLog(SecurityManager.class);

    private final AccessPolicyRepo accessPolicyRepo;
//    IComponentSecurityHandler componentSecurityHandler;
    SecurityHandlerComponent securityHandlerComponent;
    private CachedMap<SecurityCacheKey, Boolean> cache = new CachedMap<>(10 * 1000,120*1000,150);

    @Autowired
    public SecurityManager(AccessPolicyRepo accessPolicyRepo,
                           SecurityHandlerComponent securityHandlerComponent) throws SecurityHandlerException {
        this.accessPolicyRepo = accessPolicyRepo;
//        this.componentSecurityHandler = securityHandlerComponent.getHandler();
        this.securityHandlerComponent = securityHandlerComponent;

    }

    public String generateSecurityResponse() throws SecurityHandlerException {
//        return "";
        //TODO deployment true sec resoonse
            return this.securityHandlerComponent.getHandler().generateServiceResponse();
    }

    @Override
    public boolean checkPolicyByResourceId(String resourceId, SecurityRequest request, Map<SecurityCredentials, ValidationStatus> validatedCredentials) {
        return checkPolicy(accessPolicyRepo.findById(resourceId), request, validatedCredentials);
    }

    @Override
    public boolean checkPolicyByResourceIri(String resourceIri, SecurityRequest request, Map<SecurityCredentials, ValidationStatus> validatedCredentials) {
        return checkPolicy(accessPolicyRepo.findByIri(resourceIri), request, validatedCredentials);
    }

    public List<String> checkGroupPolicies( List<String> resourceIds, SecurityRequest securityRequest ) {
        if( !this.securityHandlerComponent.isSecurityEnabled() ) {
            //If no security -> return all resources
            return resourceIds;
        }

        List<String> result = new ArrayList<>();

        Map<String, IAccessPolicy> accessPolicies = new HashMap<>();

        result.addAll(resourceIds.stream().filter( id ->  {
            Optional<AccessPolicy> accessPolicy = accessPolicyRepo.findById(id);
            if( accessPolicy.isPresent() ) {
                //Add to list of policies to check
                accessPolicies.put(id,accessPolicy.get().getPolicy());
                return false;
            } else {
                return true;
            }
        }).collect(Collectors.toList()));

//        log.debug("Got list of ids with size: " + resourceIds.size() + " and found " + result.size() + " resources without policies which has been added to return list");
//        log.debug("Resources without policies: " + String.join(",",result));


        Set<String> idsFulfillingPolicies = this.securityHandlerComponent.getHandler().getSatisfiedPoliciesIdentifiers(accessPolicies, securityRequest);

        result.addAll(idsFulfillingPolicies);
//        log.debug("Checking access policies for map with size: " + accessPolicies.size() + " found additional " + idsFulfillingPolicies.size() + " resources fulfilling criteria. Returning total of " + result.size());

        return result;
    }

    private boolean checkPolicy(Optional<AccessPolicy> policy, SecurityRequest request, Map<SecurityCredentials, ValidationStatus> validatedCredentials) {
        boolean result = true;
        if( !this.securityHandlerComponent.isSecurityEnabled() ) {
            return true;
        }

        if (request == null) {
            log.info("Security Request is null");
            result = false;
        } else {
            if (policy.isPresent()) {
                //Check in cache
                String resourceId = policy.get().getResourceId();
                SecurityCacheKey securityCacheKey = new SecurityCacheKey(request, resourceId);
                if (cache.get(securityCacheKey) == null) {
//                    log.debug("No cache available for res " + resourceId + " and request timestamp + "
//                            + request.getTimestamp() + ". Checking policy");
                    Map<String, IAccessPolicy> accessPolicyMap = new HashMap<>();
                    if (resourceId != null) {
                        if (policy.get().getPolicy() != null) {
                            accessPolicyMap.put(resourceId, policy.get().getPolicy());
                            Set<String> ids = this.securityHandlerComponent.getHandler().getSatisfiedPoliciesIdentifiers(accessPolicyMap, request, validatedCredentials);
                            if( ids != null ) {
                                if (!ids.contains(resourceId)) {
//                                    log.debug("Security Policy is not valid for res: " + resourceId);
//                                    System.out.println("Security Policy is not valid for res: " + resourceId);
                                    result = false;
                                } else {
//                                    log.debug("Security Policy is valid " + resourceId);
//                                    System.out.println("Security Policy is valid " + resourceId);
                                    result = true;
                                }
                                cache.put(securityCacheKey, Boolean.valueOf(result));
                            } else {
//                                System.out.println("satisfied ids are null");
                                result = false;
                            }
                        } else {
//                            log.debug("Policy is null");
                        }
                    } else {
//                        log.debug("Resource id of policy is null");
                    }
                } else {
                    result = cache.get(securityCacheKey);
//                    log.debug("Cache available for res " + resourceId + " and request timestamp + "
//                            + request.getTimestamp() + ". Result: " + result);
                }
            } else {
//                log.debug("No policy to check is present");
            }
        }

        return result;
    }

}
