package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.communication.payloads.SecurityCredentials;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;

import java.util.Map;

/**
 * Created by Szymon Mueller on 16/10/2017.
 */
public interface IFilteringManager {

    /**
     * Checks filtering policy for specified resource id.
     *
     * @param resourceId Id of the resource
     * @param request Security request
     * @param validatedCredentials map of validated credentials (for caching)
     * @return {@code true} if policy is fulfilled for this resource, {@code false} otherwise.
     * @throws Exception in case of error during validation
     */
    boolean checkPolicyByResourceId(String resourceId, SecurityRequest request, Map<SecurityCredentials, ValidationStatus> validatedCredentials) throws Exception;

    /**
     * Checks filtering policy for resource with specified iri.
     *
     * @param resourceIri Iri of the resource
     * @param request Security request
     * @param validatedCredentials map of validated credentials (for caching)
     * @return {@code true} if policy is fulfilled for this resource, {@code false} otherwise.
     * @throws Exception in case of error during validation
     */
    boolean checkPolicyByResourceIri(String resourceIri, SecurityRequest request, Map<SecurityCredentials, ValidationStatus> validatedCredentials) throws Exception;
}
