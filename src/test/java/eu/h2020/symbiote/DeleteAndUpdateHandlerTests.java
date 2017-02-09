package eu.h2020.symbiote;

import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.ontology.model.MetaInformationModel;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.SearchRequest;
import eu.h2020.symbiote.query.SearchResponse;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;

/**
 * Created by Mael on 26/01/2017.
 */
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


                Model res101Model = loadFileAsModel(RESOURCE_101_FILENAME);
                Model res102Model = loadFileAsModel(RESOURCE_102_FILENAME);
                Model res103Model = loadFileAsModel(RESOURCE_103_FILENAME);

                registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res101Model);
                registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res102Model);
                registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res103Model);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteResourceTest() {
        printDataset();
        SearchHandler searchHandler = new SearchHandler(triplestore);
        SearchRequest searchReq = new SearchRequest();
        searchReq.setId("102");
        SearchResponse searchResponse = searchHandler.search(searchReq);

        SearchRequest searchReq101 = new SearchRequest();
        searchReq101.setId("101");
        SearchResponse search101Response = searchHandler.search(searchReq101);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("Before delete should be 1 resource", 1, searchResponse.getResourceList().size());

        assertNotNull(search101Response);
        assertNotNull(search101Response.getResourceList());
        assertEquals("Before delete not mentioned resource should be 1", 1, search101Response.getResourceList().size());

        ResourceHandler delHandler = new ResourceHandler(storage);
        delHandler.deleteResource("102");

        searchResponse = searchHandler.search(searchReq);

        search101Response = searchHandler.search(searchReq101);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After delete should be 0 resource", 0, searchResponse.getResourceList().size());

        assertNotNull(search101Response);
        assertNotNull(search101Response.getResourceList());
        assertEquals("After delete not mentioned resource should still be 1", 1, search101Response.getResourceList().size());
//        try {
//            executeUpdate(triplestore,"/qDelete.sparql");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        printDataset();
    }

    @Test
    public void deletePlatformTest() {
        printDataset();

        Model graph = triplestore.getDefaultGraph();
        org.apache.jena.rdf.model.Resource resource = graph.createResource(PLATFORM_A_URI);
        Property property = graph.createProperty(MetaInformationModel.CIM_PREFIX + "hasID");
        boolean contains = graph.contains(resource, property, PLATFORM_A_ID);
        assertTrue("Before platform A should exist", contains);

        PlatformHandler delHandler = new PlatformHandler(storage);
        delHandler.deletePlatform(PLATFORM_A_ID);
        printDataset();

        contains = graph.contains(resource, property, PLATFORM_A_ID);
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
        SearchRequest searchReq = new SearchRequest();
        searchReq.setName("*101*");
        SearchResponse searchResponse = searchHandler.search(searchReq);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("Before modify resource should be 1", 1, searchResponse.getResourceList().size());

        Resource resource = generateResource();
        resource.setName(RESOURCE_101_LABEL_UPDATE);
        ResourceHandler modifyHandler = new ResourceHandler(storage);
        modifyHandler.updateResource( resource );

        System.out.println(">>>>>>>>>>>>>>>>>>>After modify<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
        printDataset();
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<>>>>>>>>>>>>>>>>>>>");

        searchResponse = searchHandler.search(searchReq);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After modify should not find resource for old name", 0, searchResponse.getResourceList().size());


        searchReq.setName("*Hundred*");
        searchResponse = searchHandler.search(searchReq);
        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After modify should find 1 resource with new name", 1, searchResponse.getResourceList().size());


        graph = triplestore.getDefaultGraph();
        hasAtLeastOne = graph.listStatements().hasNext();
        assertTrue("After platform delete graph should be empty", hasAtLeastOne);
    }

    @Test
    public void modifyPlatformTest() {
        printDataset();

        Model graph = triplestore.getDefaultGraph();
        boolean hasAtLeastOne = graph.listStatements().hasNext();
        assertTrue("Initial graph should have at least some statements", hasAtLeastOne);

        PlatformHandler modifyHandler = new PlatformHandler(storage);
        modifyHandler.updatePlatform( generatePlatformAUpdate() );

        printDataset();

        graph = triplestore.getDefaultGraph();
        hasAtLeastOne = graph.listStatements().hasNext();
        assertTrue("After platform delete graph should be empty", hasAtLeastOne);
    }


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
