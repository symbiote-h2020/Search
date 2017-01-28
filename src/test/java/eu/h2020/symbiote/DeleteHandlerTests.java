package eu.h2020.symbiote;

import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.SearchRequest;
import eu.h2020.symbiote.query.SearchResponse;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;

import java.io.IOException;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Mael on 26/01/2017.
 */
public class DeleteHandlerTests {

    private SearchStorage storage;
    private Registry registry;
    private TripleStore triplestore;


    public void setUp() {

        storage = SearchStorage.getInstance(SearchStorage.TESTCASE_STORAGE_NAME);
        triplestore = storage.getTripleStore();
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);

            Model res101Model = loadFileAsModel( RESOURCE_101_FILENAME );
            Model res102Model = loadFileAsModel( RESOURCE_102_FILENAME);
            Model res103Model = loadFileAsModel( RESOURCE_103_FILENAME);

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res101Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res102Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res103Model);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deleteResourceTest() {
        setUp();
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
        assertEquals("Before delete should be 1 resource",1,searchResponse.getResourceList().size());

        assertNotNull(search101Response);
        assertNotNull(search101Response.getResourceList());
        assertEquals("Before delete not mentioned resource should be 1",1,search101Response.getResourceList().size());

        ResourceHandler delHandler = new ResourceHandler(storage);
        delHandler.deleteResource("102");

        searchResponse = searchHandler.search(searchReq);

        search101Response = searchHandler.search(searchReq101);

        assertNotNull(searchResponse);
        assertNotNull(searchResponse.getResourceList());
        assertEquals("After delete should be 0 resource",0,searchResponse.getResourceList().size());

        assertNotNull(search101Response);
        assertNotNull(search101Response.getResourceList());
        assertEquals("After delete not mentioned resource should still be 1",1,search101Response.getResourceList().size());
//        try {
//            executeUpdate(triplestore,"/qDelete.sparql");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        printDataset();
    }


    private void printDataset() {
        triplestore.printDataset();
    }

    private void executeUpdate( TripleStore store, String filename ) throws IOException {
        String query = IOUtils.toString(this.getClass()
                .getResource(filename));
        UpdateRequest request = UpdateFactory.create(query);
        store.executeUpdate(request);
    }

}
