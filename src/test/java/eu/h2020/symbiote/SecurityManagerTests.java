package eu.h2020.symbiote;

import eu.h2020.symbiote.filtering.*;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.singletoken.SingleLocalHomeTokenAccessPolicy;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.security.handler.IComponentSecurityHandler;
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

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 07/11/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class SecurityManagerTests {

    public static final String RES_ID = "res1";
    public static final String RES_IRI = "http://res1";

    public static final String RES2_ID = "res2";
    public static final String RES2_IRI = "http://res2";

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
        ReflectionTestUtils.setField(securityManager, "securityHandlerComponent", securityHandlerComponent);
    }

    @Test
    public void getPolicyForId() throws InvalidArgumentsException {


        AccessPolicy accessPolicy = generatePolicy(RES_ID,RES_IRI);
        when(accessPolicyRepo.findByIri(RES_IRI)).thenReturn(Optional.of(accessPolicy));

        SecurityRequest request = new SecurityRequest("test");

        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class),anyMap())).thenAnswer( new SecurityAnswer(accessPolicy,request));
        boolean b = securityManager.checkPolicyByResourceIri(RES_IRI, request,new HashMap<>());
        assertTrue(b);
    }

    @Test
    public void getPolicyForIri() throws InvalidArgumentsException {

        AccessPolicy accessPolicy = generatePolicy(RES_ID,RES_IRI);
        when(accessPolicyRepo.findById(RES_ID)).thenReturn(Optional.of(accessPolicy));

        SecurityRequest request = new SecurityRequest("test");

        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class),anyMap())).thenAnswer( new SecurityAnswer(accessPolicy,request));
//        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class),anyMap())).thenAnswer( Arrays.asList(RES_ID));
        boolean b = securityManager.checkPolicyByResourceId(RES_ID, request,new HashMap<>());
        assertTrue(b);
    }

    @Test
    public void getPolicyForNullSecurityRequest() throws InvalidArgumentsException {
        AccessPolicy accessPolicy = generatePolicy(RES_ID,RES_IRI);
        when(accessPolicyRepo.findById(RES_ID)).thenReturn(Optional.of(accessPolicy));
        when(securityHandlerComponent.isSecurityEnabled()).thenReturn(true);

        SecurityRequest request = null;

        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class),anyMap())).thenAnswer( new SecurityAnswer(accessPolicy,request));
        boolean b = securityManager.checkPolicyByResourceId(RES_ID, request,new HashMap<>());
        assertFalse(b);
    }

    @Test
    public void getPolicyResultForSensorNotFulfillingPolicy() throws InvalidArgumentsException {
        AccessPolicy accessPolicy = generatePolicy(RES2_ID,RES2_IRI);
        when(accessPolicyRepo.findById(RES2_ID)).thenReturn(Optional.of(accessPolicy));
        when(securityHandlerComponent.isSecurityEnabled()).thenReturn(true);


        SecurityRequest request = new SecurityRequest("test");

        when(componentSecurityHandler.getSatisfiedPoliciesIdentifiers(anyMap(), any(SecurityRequest.class),anyMap())).thenAnswer( new SecurityAnswer(accessPolicy,request));
        boolean b = securityManager.checkPolicyByResourceId(RES2_ID, request,new HashMap<>());
        assertFalse(b);
    }

    @Test
    public void testSecurityCacheKeyEquals() {
        SecurityRequest request = new SecurityRequest("test1");
        String resourceUri1 = "resourceUri";
        String resourceUri2 = "resourceUri";
        SecurityCacheKey key1 = new SecurityCacheKey(request, resourceUri1 );
        SecurityCacheKey key2 = new SecurityCacheKey(request, resourceUri2 );
        assertTrue("Equal method must return true", key1.equals(key2));
    }

    @Test
    public void testSecurityCache() {
        //Size test
        CachedMap<SecurityCacheKey, Boolean> cache = new CachedMap(10 * 1000, 120*1000, 1);

        SecurityRequest request = new SecurityRequest("test1");
        SecurityCacheKey key1 = new SecurityCacheKey(request,RES_IRI);
        cache.put(key1,Boolean.TRUE);

        assertNotNull( "Cache should return cached object", cache.get(key1) );
        assertTrue( "Cached object should return true", cache.get(key1) );

        SecurityCacheKey key2 = new SecurityCacheKey(request,RES2_IRI);
        cache.put(key2,Boolean.FALSE);
        assertNull( "Previous cached object should be removed", cache.get(key1));
        assertNotNull( "New cache should return cached object",cache.get(key2) );
        assertFalse( "New cache should return false",cache.get(key2) );
    }

    @Test
    public void testSecurityTTL() {
        //Size test
        CachedMap<SecurityCacheKey, Boolean> cache = new CachedMap(1 * 1000, 3*1000, 10);

        SecurityRequest request = new SecurityRequest("test1");
        SecurityCacheKey key1 = new SecurityCacheKey(request,RES_IRI);
        cache.put(key1,Boolean.TRUE);


        Boolean cachedObject = cache.get(key1);
        assertNotNull("Cache should return cached object", cachedObject);
        assertTrue("Returned cache should be true", cachedObject);

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        cachedObject = cache.get(key1);
        assertNull("Object should not outlive its ttl", cachedObject);

    }


    private AccessPolicy generatePolicy(String resourceId,String resourceIri) throws InvalidArgumentsException {
        SingleLocalHomeTokenAccessPolicy pol = new SingleLocalHomeTokenAccessPolicy("public", new HashMap<String, String>());
        AccessPolicy policy = new AccessPolicy(resourceId, resourceIri, pol);
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
            if( arguments !=null && arguments.length == 3 && arguments[0] != null && arguments[1] != null ){
                Map<String,IAccessPolicy> argMap = (Map<String, IAccessPolicy>) arguments[0];
                SecurityRequest argRequest = (SecurityRequest) arguments[1];

                if( request.equals(argRequest) ){
//                    if( argMap != null && argMap.containsKey(RES_ID) ){
//                        IAccessPolicy policy = argMap.get(RES_ID);
//                        if( policy.equals(accessPolicy.getPolicy())) {
//                            return new HashSet<>(Arrays.asList(RES_ID));
//                        }
//                    }
                    return new HashSet<>(Arrays.asList(RES_ID));
                }

            }
//            if(arguments !=null && arguments.length == 3 && arguments[0] != null && arguments[1] != null && ar ) {
//                System.out.println("Mockito arguments 1");
//                if( arguments[0].equals(RES_ID )) {
//                    return new HashSet<>(Arrays.asList(RES_ID));
//                }
//            } else {
//                System.out.println("Mockito wrong arguments ");
//                System.out.println(arguments==null?"Mockito args null ": "length " + arguments.length);
//            }

            return null;
        }
    }

}
