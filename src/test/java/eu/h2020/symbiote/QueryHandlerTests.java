package eu.h2020.symbiote;

import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.query.SearchRequest;
import eu.h2020.symbiote.query.SearchResponse;
import eu.h2020.symbiote.query.SearchResponseResource;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;

/**
 * Created by Mael on 26/01/2017.
 */
public class QueryHandlerTests {

//    private SearchStorage storage;
    private Registry registry;
    private TripleStore triplestore;

    @Before
    public void setUp() {

//        storage = SearchStorage.getInstance(SearchStorage.TESTCASE_STORAGE_NAME);
        triplestore = new TripleStore();
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);

            Model res101Model = loadFileAsModel( RESOURCE_101_FILENAME );
            Model res102Model = loadFileAsModel( RESOURCE_102_FILENAME);
            Model res103Model = loadFileAsModel( RESOURCE_103_FILENAME);
            Model res501Model = loadFileAsModel( RESOURCE_501_FILENAME);

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res101Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res102Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res103Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_501_URI, res501Model);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHandlerGenerateSimpleResource() {
        String query = new QueryGenerator().addResourceId("102").toString();
        ResultSet resultSet = triplestore.executeQuery(query);
        SearchResponse response = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        assertNotNull(response);
        assertNotNull(response.getResourceList());
        assertEquals("Should return 1 response",1,response.getResourceList().size());
        SearchResponseResource res = response.getResourceList().get(0);
        ensureFieldsNotNull(res);
        printResource(res);
        assertEquals(1,res.getObservedProperties().size());
    }

    @Test
    public void testHandlerGenerateResourceWithTwoProperties() {
        String query = new QueryGenerator().addResourceId("101").toString();
        ResultSet resultSet = triplestore.executeQuery(query);
        SearchResponse response = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        assertNotNull(response);
        assertNotNull(response.getResourceList());
        assertEquals("Should return 1 response",1,response.getResourceList().size());
        SearchResponseResource res = response.getResourceList().get(0);
        ensureFieldsNotNull(res);
        printResource(res);
        assertEquals(2,res.getObservedProperties().size());
    }

    @Test
    public void testHandlerGenerateTwoResources() {
        SearchHandler handler = new SearchHandler(triplestore);

        SearchRequest searchRequest = new SearchRequest();
        List<String> props = new ArrayList<>();
        props.add("Temperature");
        searchRequest.setObserved_property(props);
        SearchResponse response = handler.search(searchRequest);

        assertNotNull(response);
        assertNotNull( response.getResourceList() );
        assertEquals("Should return 2 response",2,response.getResourceList().size());
        SearchResponseResource res1 = response.getResourceList().get(0);
        ensureFieldsNotNull(res1);
        printResource(res1);
        SearchResponseResource res2 = response.getResourceList().get(1);
        ensureFieldsNotNull(res2);
        printResource(res2);
    }

    @Test
    public void testHandlerResourceNameStartsWith() {
        String nameToSearch = "Poll*";
        SearchHandler handler = new SearchHandler(triplestore);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setName(nameToSearch);
        SearchResponse response = handler.search(searchRequest);

        assertNotNull(response);
        assertNotNull( response.getResourceList() );
        assertEquals("Should return 1 response",1,response.getResourceList().size());
        SearchResponseResource res1 = response.getResourceList().get(0);
        ensureFieldsNotNull(res1);
        printResource(res1);
        assertTrue(res1.getName().startsWith(nameToSearch.substring(0,nameToSearch.length()-1)));
    }

    @Test
    public void testHandlerResourceNameEndsWith() {
        String nameToSearch = "*101";
        SearchHandler handler = new SearchHandler(triplestore);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setName(nameToSearch);
        SearchResponse response = handler.search(searchRequest);

        assertNotNull(response);
        assertNotNull( response.getResourceList() );
        assertEquals("Should return 1 response",1,response.getResourceList().size());
        SearchResponseResource res1 = response.getResourceList().get(0);
        ensureFieldsNotNull(res1);
        printResource(res1);
        assertTrue(res1.getName().endsWith(nameToSearch.substring(1,nameToSearch.length())));
    }

    @Test
    public void testHandlerResourceNameContains() {
        String nameToSearch = "*source*";
        SearchHandler handler = new SearchHandler(triplestore);

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.setName(nameToSearch);
        SearchResponse response = handler.search(searchRequest);

        assertNotNull(response);
        assertNotNull( response.getResourceList() );
        assertEquals("Should return 3 responses",3,response.getResourceList().size());
        for( SearchResponseResource res: response.getResourceList() ) {
            ensureFieldsNotNull(res);
            printResource(res);
            assertTrue(res.getName().contains(nameToSearch.substring(1,nameToSearch.length()-1)));
        }
    }

    private void ensureFieldsNotNull( SearchResponseResource response ) {
        assertNotNull(response.getId());
        assertFalse(response.getId().isEmpty());
        assertNotNull(response.getName());
        assertFalse(response.getName().isEmpty());
        assertNotNull(response.getDescription());
        assertFalse(response.getDescription().isEmpty());
        assertNotNull(response.getPlatformId());
        assertFalse(response.getPlatformId().isEmpty());
        assertNotNull(response.getPlatformName());
        assertFalse(response.getPlatformName().isEmpty());
        assertNotNull(response.getLocationName());
        assertFalse(response.getLocationName().isEmpty());
        assertNotNull(response.getLocationLatitude());
        assertNotNull(response.getLocationLongitude());
        assertNotNull(response.getLocationAltitude());
        assertNotNull(response.getObservedProperties());
    }

    private void printResource( SearchResponseResource response ) {
        System.out.println("=== Resource ===== ");
        System.out.print("        id: " );
        System.out.println(response.getId());
        System.out.print("      name: " );
        System.out.println(response.getName());
        System.out.print("      desc: " );
        System.out.println(response.getDescription());
        System.out.print("    platId: " );
        System.out.println(response.getPlatformId());
        System.out.print("  platName: " );
        System.out.println(response.getPlatformName());
        System.out.print("   locName: " );
        System.out.println(response.getLocationName());
        System.out.print("    locLat: " );
        System.out.println(response.getLocationLatitude());
        System.out.print("   locLong: " );
        System.out.println(response.getLocationLongitude());
        System.out.print("    locAlt: " );
        System.out.println(response.getLocationAltitude());
        int x = 0;
        for( String obsProperty: response.getObservedProperties() ) {
            x++;
            System.out.println("     prop"+x+": " + obsProperty );
        }
        System.out.println("================== ");

    }

}
