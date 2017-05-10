package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.ontology.model.MetaInformationModel;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.SearchResponse;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;

/**
 * Created by Mael on 26/01/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeleteAndUpdateHandlerTests {

    private SearchStorage storage;
    private Registry registry;
    private TripleStore triplestore;

    @Before
    public void setUp() {

        storage = SearchStorage.getInstance(SearchStorage.TESTCASE_STORAGE_NAME);
        triplestore = storage.getTripleStore();
//        triplestore.getDefaultGraph().removeAll();
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);

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
    public void deleteResourceTest() {
        System.out.println(" ===== PRE ==== ");
        printDataset();
        System.out.println(" ===== PRE ==== ");
        SearchHandler searchHandler = new SearchHandler(triplestore);

        CoreQueryRequest searchReq = new CoreQueryRequest();
        searchReq.setId("stationary1");
        SearchResponse searchResponse = searchHandler.search(searchReq);

        CoreQueryRequest otherResReq = new CoreQueryRequest();
        otherResReq.setId("mobile1");
        SearchResponse otherSearchResponse = searchHandler.search(otherResReq);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("Before delete should be 1 resource", 1, searchResponse.getResourceList().size());

        assertNotNull(otherSearchResponse);
        assertNotNull(otherSearchResponse.getResourceList());
        assertEquals("Before delete should be 1 resource", 1, otherSearchResponse.getResourceList().size());

        ResourceHandler delHandler = new ResourceHandler(storage);
        delHandler.deleteResource("stationary1");

        searchResponse = searchHandler.search(searchReq);

        otherSearchResponse = searchHandler.search(otherResReq);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After delete should be 0 resource", 0, searchResponse.getResourceList().size());

        assertNotNull(otherSearchResponse);
        assertNotNull(otherSearchResponse.getResourceList());
        assertEquals("After delete not mentioned resource should still be 1", 1, otherSearchResponse.getResourceList().size());

        System.out.println(" ===== AFTER ==== ");
        printDataset();
        System.out.println(" ===== AFTER ==== ");
    }

    @Test
    public void deletePlatformTest() {
        System.out.println(" ====== BEFORE ===== ");
        printDataset();
        System.out.println(" ====== BEFORE ===== ");

        Model graph = triplestore.getDefaultGraph();
        org.apache.jena.rdf.model.Resource resource = graph.createResource(PLATFORM_A_URI);
        boolean contains = graph.contains(resource, MetaInformationModel.CIM_HASID, PLATFORM_A_ID);
        assertTrue("Before platform A should exist", contains);

        PlatformHandler delHandler = new PlatformHandler(storage);
        delHandler.deletePlatform(PLATFORM_A_ID);

        System.out.println(" ====== AFTER ===== ");
        printDataset();
        System.out.println(" ====== AFTER ===== ");

        contains = graph.contains(resource, MetaInformationModel.CIM_HASID, PLATFORM_A_ID);
        assertFalse("After delete platform A should not exist", contains);
    }

    @Test
    public void modifyResourceTest() {
        System.out.println(">>>>>>>>>>>>>>>>>>>BEFORE<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        printDataset();
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>");

        Model graph = triplestore.getDefaultGraph();
        boolean hasAtLeastOne = graph.listStatements().hasNext();
        assertTrue("Initial graph should have at least some statements", hasAtLeastOne);

        SearchHandler searchHandler = new SearchHandler(triplestore);
        CoreQueryRequest searchReq = new CoreQueryRequest();
        searchReq.setName("*stationary*");
        SearchResponse searchResponse = searchHandler.search(searchReq);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("Before modify resource should be 1", 1, searchResponse.getResourceList().size());

        CoreResource resource = generateModifiedStationarySensor();
//        resource.setLabels(Arrays.asList(RESOURCE_101_LABEL_UPDATE));
        ResourceHandler modifyHandler = new ResourceHandler(storage);
        CoreResourceRegisteredOrModifiedEventPayload updateReq = new CoreResourceRegisteredOrModifiedEventPayload();
        updateReq.setPlatformId(PLATFORM_A_ID);

        updateReq.setResources(Arrays.asList(resource));
        modifyHandler.updateResource(updateReq);

        System.out.println(">>>>>>>>>>>>>>>>>>>After modify<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        printDataset();
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>");

        searchResponse = searchHandler.search(searchReq);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After modify should not find resource for old name", 0, searchResponse.getResourceList().size());


        searchReq.setName("New sensor 1");
        searchResponse = searchHandler.search(searchReq);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After modify should find 1 resource with new name", 1, searchResponse.getResourceList().size());


        graph = triplestore.getDefaultGraph();
        hasAtLeastOne = graph.listStatements().hasNext();
        assertTrue("After platform delete graph should be empty", hasAtLeastOne);
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
