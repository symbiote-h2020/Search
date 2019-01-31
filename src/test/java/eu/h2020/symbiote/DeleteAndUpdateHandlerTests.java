package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.*;
import eu.h2020.symbiote.mappings.MappingManager;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.ranking.RankingHandler;
import eu.h2020.symbiote.search.SearchStorage;
import eu.h2020.symbiote.semantics.ontology.CIM;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Mael on 26/01/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteAndUpdateHandlerTests {

    private SearchStorage storage;
    private Registry registry;
    private TripleStore triplestore;
    @Mock
    private AccessPolicyRepo accessPolicyRepo;
    @Mock
    private SecurityManager securityManager;
    @Mock
    private RankingHandler rankingHandler;
    @Mock
    private MappingManager mappingManager;
    @Mock
    private InterworkingServiceInfoRepo interworkingServiceInfoRepo;

    @Before
    public void setUp() throws Exception {

        when(securityManager.checkPolicyByResourceId(anyString(),any(),any())).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(anyString(),any(),any())).thenReturn(Boolean.TRUE);
        SearchStorage.clearStorage();
        storage = SearchStorage.getInstance(SearchStorage.TESTCASE_STORAGE_NAME, securityManager, false);
        triplestore = storage.getTripleStore();
//        triplestore.getDefaultGraph().removeAll();
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD" );
            Model mobileModel = loadFileAsModel(RESOURCE_MOBILE_FILENAME, "JSONLD" );
//            Model serviceModel = loadFileAsModel(RESOURCE_SERVICE_FILENAME, "JSONLD" );
//            Model actuatingServiceModel = loadFileAsModel(RESOURCE_ACTUATING_SERVICE_FILENAME, "JSONLD" );
//            Model actuatorModel = loadFileAsModel(RESOURCE_ACTUATOR_FILENAME, "JSONLD" );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_STATIONARY_URI,stationaryModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_MOBILE_URI,mobileModel);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_SERVICE_URI,serviceModel);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATOR_URI,actuatorModel);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATING_SERVICE_URI,actuatingServiceModel);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteResourceTest() throws InterruptedException {
        when(securityManager.checkGroupPolicies(anyList(),any())).thenAnswer(i -> i.getArguments()[0]);
//        SearchCommunicationHandler comm1 = new SearchCommunicationHandler("1",null,null,null,null);
        SearchCommunicationHandler comm1 = Mockito.mock(SearchCommunicationHandler.class);
        SearchCommunicationHandler comm2 = Mockito.mock(SearchCommunicationHandler.class);
        SearchCommunicationHandler comm3 = Mockito.mock(SearchCommunicationHandler.class);
        SearchCommunicationHandler comm4 = Mockito.mock(SearchCommunicationHandler.class);

        MultiSearchHandler searchHandler = new MultiSearchHandler(triplestore,true,securityManager,rankingHandler, mappingManager,false, 5, 10, 5);

        CoreQueryRequest searchReq = new CoreQueryRequest();
        searchReq.setId("stationary1");
        QueryResponse searchResponse = sendSearchAndGetResponse(searchHandler,comm1,searchReq);

        CoreQueryRequest otherResReq = new CoreQueryRequest();
        otherResReq.setId("mobile1");
//        SearchCommunicationHandler comm2 = new SearchCommunicationHandler("2",null,null,null,null);
        QueryResponse otherSearchResponse = sendSearchAndGetResponse(searchHandler,comm2,otherResReq);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResources());
        assertEquals("Before delete should be 1 resource", 1, searchResponse.getResources().size());

        assertNotNull(otherSearchResponse);
        assertNotNull(otherSearchResponse.getResources());
        assertEquals("Before delete should be 1 resource", 1, otherSearchResponse.getResources().size());

        ResourceHandler delHandler = new ResourceHandler(storage,accessPolicyRepo, interworkingServiceInfoRepo);
        delHandler.deleteResource("stationary1");

        searchResponse = sendSearchAndGetResponse(searchHandler,comm3,searchReq);

        otherSearchResponse = sendSearchAndGetResponse(searchHandler,comm4,otherResReq);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResources());
        assertEquals("After delete should be 0 resource", 0, searchResponse.getResources().size());

        assertNotNull(otherSearchResponse);
        assertNotNull(otherSearchResponse.getResources());
        assertEquals("After delete not mentioned resource should still be 1", 1, otherSearchResponse.getResources().size());
    }

    @Test
    public void deletePlatformTest() {
        printDataset();

        Model graph = triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH,false,false);
        org.apache.jena.rdf.model.Resource resource = graph.createResource(PLATFORM_A_URI);
        boolean contains = graph.contains(resource, CIM.id, PLATFORM_A_ID);
        assertTrue("Before platform A should exist", contains);

        PlatformHandler delHandler = new PlatformHandler(storage,interworkingServiceInfoRepo);
        delHandler.deletePlatform(PLATFORM_A_ID);

        graph = triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH,false,false);
        contains = graph.contains(resource, CIM.id, PLATFORM_A_ID);
        assertFalse("After delete platform A should not exist", contains);
    }

    @Test
    public void modifyResourceTest() throws InterruptedException {
        when(securityManager.checkGroupPolicies(anyList(),any())).thenAnswer(i -> i.getArguments()[0]);
        InterworkingServiceInfo iiService = new InterworkingServiceInfo(PLATFORM_A_SERVICE_URI,PLATFORM_A_SERVICE_URI,PLATFORM_A_ID,"BIM");
        List<InterworkingServiceInfo> list = Arrays.asList(iiService);
        when(interworkingServiceInfoRepo.findByInterworkingServiceURL(any())).thenReturn(list);
//        SearchCommunicationHandler comm1 = new SearchCommunicationHandler("1",null,null,null,null);
        SearchCommunicationHandler comm1 = Mockito.mock(SearchCommunicationHandler.class);
        when(comm1.getReqId()).thenReturn("1");
        SearchCommunicationHandler comm2 = Mockito.mock(SearchCommunicationHandler.class);
        when(comm2.getReqId()).thenReturn("2");
        SearchCommunicationHandler comm3 = Mockito.mock(SearchCommunicationHandler.class);
        when(comm3.getReqId()).thenReturn("3");

        Model graph = triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH,false,false);
        boolean hasAtLeastOne = graph.listStatements().hasNext();
        assertTrue("Initial graph should have at least some statements", hasAtLeastOne);

        MultiSearchHandler searchHandler = new MultiSearchHandler(triplestore,true, securityManager, rankingHandler,mappingManager,false,3,5,5);
        CoreQueryRequest searchReq = new CoreQueryRequest();
        searchReq.setName("*stationary*");
        System.out.println("1");
        QueryResponse searchResponse = sendSearchAndGetResponse(searchHandler,comm1,searchReq);
        System.out.println("2");

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResources());
        assertEquals("Before modify resource should be 1", 1, searchResponse.getResources().size());

        CoreResource resource = generateModifiedStationarySensor();
        resource.setName(RESOURCE_STATIONARY_LABEL_MODIFIED);
        ResourceHandler modifyHandler = new ResourceHandler(storage,accessPolicyRepo,interworkingServiceInfoRepo);
        CoreResourceRegisteredOrModifiedEventPayload updateReq = new CoreResourceRegisteredOrModifiedEventPayload();
        updateReq.setPlatformId(PLATFORM_A_ID);

        System.out.println("3");
        updateReq.setResources(Arrays.asList(resource));
        modifyHandler.updateResource(updateReq);
        System.out.println("4");

        searchResponse = sendSearchAndGetResponse(searchHandler,comm2,searchReq);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResources());
        assertEquals("After modify should not find resource for old name", 0, searchResponse.getResources().size());


        searchReq.setName(RESOURCE_STATIONARY_LABEL_MODIFIED);

        searchResponse = sendSearchAndGetResponse(searchHandler,comm3,searchReq);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResources());
        assertEquals("After modify should find 1 resource with new name", 1, searchResponse.getResources().size());
    }
//
//    @Test
//    public void modifyPlatformTest() {
//        printDataset();
//
//        Model graph = triplestore.getDefaultGraph();
//        boolean hasAtLeastOne = graph.listStatements().hasNext();
//        assertTrue("Initial graph should have at least some statements", hasAtLeastOne);
//
//        PlatformHandler modifyHandler = new PlatformHandler(storage);
//        modifyHandler.updatePlatform(generatePlatformAUpdate());
//
//        printDataset();
//
//        graph = triplestore.getDefaultGraph();
//        hasAtLeastOne = graph.listStatements().hasNext();
//        assertTrue("After platform delete graph should be empty", hasAtLeastOne);
//    }


    private void printDataset() {
        triplestore.printDataset();
    }

    private void executeUpdate(TripleStore store, String filename) throws IOException {
        String query = IOUtils.toString(this.getClass()
                .getResource(filename));
        UpdateRequest request = UpdateFactory.create(query);
        store.executeUpdate(request);
    }

}
