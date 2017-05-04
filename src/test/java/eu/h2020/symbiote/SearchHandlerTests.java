package eu.h2020.symbiote;

import eu.h2020.symbiote.core.ci.SparqlQueryOutputFormat;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.SearchResponseResource;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Mael on 26/01/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class SearchHandlerTests {

//    private SearchStorage storage;
    private Registry registry;
    private TripleStore triplestore;
    private SearchHandler searchHandler;


    @Before
    public void setUp() {

//        storage = SearchStorage.getInstance(SearchStorage.TESTCASE_STORAGE_NAME);
        triplestore = new TripleStore();
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);

            String platformB = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_B_FILENAME));
            registry.registerPlatform(PLATFORM_B_ID, platformB, RDFFormat.Turtle, PLATFORM_B_MODEL_ID);

            String platformC = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_C_FILENAME));
            registry.registerPlatform(PLATFORM_C_ID, platformC, RDFFormat.Turtle, PLATFORM_C_MODEL_ID);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD" );
            Model mobileModel = loadFileAsModel(RESOURCE_MOBILE_FILENAME, "JSONLD" );
            Model serviceModel = loadFileAsModel(RESOURCE_SERVICE_FILENAME, "JSONLD" );
            Model actuatingServiceModel = loadFileAsModel(RESOURCE_ACTUATING_SERVICE_FILENAME, "JSONLD" );
            Model actuatorModel = loadFileAsModel(RESOURCE_ACTUATOR_FILENAME, "JSONLD" );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_STATIONARY_URI,stationaryModel);
            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI,RESOURCE_MOBILE_URI,mobileModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_SERVICE_URI,serviceModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATOR_URI,actuatorModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATING_SERVICE_URI,actuatingServiceModel);

            searchHandler = new SearchHandler(triplestore);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSparqlQuery1() {
        CoreSparqlQueryRequest request = new CoreSparqlQueryRequest();
        try {
            String query = IOUtils.toString(this.getClass()
                    .getResource("/q1.sparql"));
            request.setQuery(query);
            request.setOutputFormat(SparqlQueryOutputFormat.TEXT);
            request.setToken("This is token");

            String result = searchHandler.sparqlSearch(request);
            assertNotNull(result);
            assertFalse(result.isEmpty());
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
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

}
