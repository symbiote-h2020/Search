package eu.h2020.symbiote;

import eu.h2020.symbiote.filtering.AccessPolicy;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.filtering.SecurityHandlerComponent;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleLocalHomeTokenAccessPolicy;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
import eu.h2020.symbiote.security.handler.SecurityHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.apache.jena.sparql.vocabulary.TestManifestUpdate_11.request;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 07/11/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityManagerTests {

    public static final String RES_ID = "res1";
    public static final String RES_IRI = "http://res1";

    @InjectMocks
    SecurityManager securityManager;

    @Mock
    IComponentSecurityHandler componentSecurityHandler;

    @Mock
    AccessPolicyRepo accessPolicyRepo;

    @Mock
    SecurityHandlerComponent securityHandlerComponent;


    @Before
    public void setup() {
        when(securityHandlerComponent.getHandler()).thenReturn(componentSecurityHandler);
        ReflectionTestUtils.setField(securityManager, "componentSecurityHandler", componentSecurityHandler);
    }

    @Test
    public void getPolicyForId() throws InvalidArgumentsException {

        AccessPolicy accessPolicy = generatePolicy();
        when(accessPolicyRepo.findByIri(RES_IRI)).thenReturn(Optional.of(accessPolicy));

        SecurityRequest request = new SecurityRequest("test");

        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class))).thenAnswer( new SecurityAnswer(accessPolicy,request));
        boolean b = securityManager.checkPolicyByResourceIri(RES_IRI, request);
        assertTrue(b);
    }

    @Test
    public void getPolicyForIri() throws InvalidArgumentsException {

        AccessPolicy accessPolicy = generatePolicy();
        when(accessPolicyRepo.findById(RES_ID)).thenReturn(Optional.of(accessPolicy));

        SecurityRequest request = new SecurityRequest("test");

        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class))).thenAnswer( new SecurityAnswer(accessPolicy,request));
        boolean b = securityManager.checkPolicyByResourceId(RES_ID, request);
        assertTrue(b);
    }

    private AccessPolicy generatePolicy() throws InvalidArgumentsException {
        SingleLocalHomeTokenAccessPolicy pol = new SingleLocalHomeTokenAccessPolicy("public", new HashMap<String, String>());
        AccessPolicy policy = new AccessPolicy(RES_ID, RES_IRI, pol);
        return policy;
    }


    class SecurityAnswer implements Answer<Set<String>> {


        private final AccessPolicy accessPolicy;
        private final SecurityRequest request;

        public SecurityAnswer(AccessPolicy accessPolicy,SecurityRequest request ) {
            this.accessPolicy = accessPolicy;
            this.request = request;
        }

        @Override
        public Set<String> answer(InvocationOnMock invocation) throws Throwable {
            Object[] arguments = invocation.getArguments();
            if( arguments !=null && arguments.length == 2 && arguments[0] != null && arguments[1] != null ){
                Map<String,IAccessPolicy> argMap = (Map<String, IAccessPolicy>) arguments[0];
                SecurityRequest argRequest = (SecurityRequest) arguments[1];

                if( request.equals(argRequest) ){
                    if( argMap != null && argMap.containsKey(RES_ID) ){
                        IAccessPolicy policy = argMap.get(RES_ID);
                        if( policy.equals(accessPolicy.getPolicy())) {
                            return new HashSet<String>(Arrays.asList(RES_ID));
                        }
                    }
                }

            }
            return null;
        }
    }

}
