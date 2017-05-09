package eu.h2020.symbiote;

import eu.h2020.symbiote.core.ci.SparqlQueryOutputFormat;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.core.model.WGS84Location;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.SearchResponseResource;
import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.JSON_Parser;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;

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

//            String platformB = IOUtils.toString(this.getClass()
//                    .getResource(PLATFORM_B_FILENAME));
//            registry.registerPlatform(PLATFORM_B_ID, platformB, RDFFormat.Turtle, PLATFORM_B_MODEL_ID);
//
//            String platformC = IOUtils.toString(this.getClass()
//                    .getResource(PLATFORM_C_FILENAME));
//            registry.registerPlatform(PLATFORM_C_ID, platformC, RDFFormat.Turtle, PLATFORM_C_MODEL_ID);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD" );
//            Model mobileModel = loadFileAsModel(RESOURCE_MOBILE_FILENAME, "JSONLD" );
//            Model serviceModel = loadFileAsModel(RESOURCE_SERVICE_FILENAME, "JSONLD" );
//            Model actuatingServiceModel = loadFileAsModel(RESOURCE_ACTUATING_SERVICE_FILENAME, "JSONLD" );
//            Model actuatorModel = loadFileAsModel(RESOURCE_ACTUATOR_FILENAME, "JSONLD" );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_STATIONARY_URI,stationaryModel);
//            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI,RESOURCE_MOBILE_URI,mobileModel);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_SERVICE_URI,serviceModel);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATOR_URI,actuatorModel);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATING_SERVICE_URI,actuatingServiceModel);

            searchHandler = new SearchHandler(triplestore);

            triplestore.printDataset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private String getWGS84SparqlQuery(WGS84Location location, String platformId ) {

        StringBuilder query = new StringBuilder();
        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n");
        query.append("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n");

        //Location test //dziala ok
        query.append("SELECT ?location WHERE {\n" );
        query.append("\t?location a cim:Location ;\n");
        query.append("\t\ta cim:WGS84Location ;\n");
        query.append("\t\trdfs:label \""+ location.getLabel() + "\" ;\n");
        query.append("\t\trdfs:comment \"" + location.getComment() + "\" .\n");

        //Ensure that location is defined for this platform...
        query.append("\t?platform a owl:Ontology ;\n");
        query.append("\t\tcim:id \"" + platformId + "\" ;\n");
        query.append("\t\tmim:hasService ?service .\n");
        query.append("\t?service mim:hasResource ?sensor .\n");
        query.append("\t?sensor a cim:Resource ;\n");
        query.append("\t\tcim:locatedAt ?location .\n");

        //WGS84-specific
        query.append("\t?location geo:lat \""+ location.getLatitude() + "\" ;\n");
        query.append("\t\tgeo:long \""+ location.getLongitude() + "\" ;\n");
        query.append("\t\tgeo:alt \""+ location.getAltitude() + "\" .\n");
        query.append("}");

        //Original


//        query.append("SELECT ?location WHERE {\n" );
//        query.append("\t?location a cim:Location ;\n");
//        query.append("\t\ta cim:WGS84Location ;\n");
//        query.append("\t\trdfs:label \""+ location.getLabel() + "\" ;\n");
//        query.append("\t\trdfs:comment \"" + location.getComment() + "\" .\n");
//
//        //Ensure that location is defined for this platform...
//        query.append("\t?platform a owl:Ontology ;\n");
//        query.append("\t\tcim:id \"" + platformId + "\" ;\n");
//        query.append("\t\tmim:hasService ?service .\n");
//        query.append("\t?service mim:hasResource ?sensor .\n");
//        query.append("\t?sensor a cim:Resource ;\n");
//        query.append("\t\tcim:locatedAt ?location .\n");
//
//        //WGS84-specific
//        query.append("\t?location geo:lat \""+ location.getLatitude() + "\" ;\n");
//        query.append("\t\tgeo:long \""+ location.getLongitude() + "\" ;\n");
//        query.append("\t\tgeo:alt \""+ location.getAltitude() + "\" .\n");
//        query.append("}");

        return query.toString();
    }

    @Test
    public void testSparqlQuery1() {
        CoreSparqlQueryRequest request = new CoreSparqlQueryRequest();
        try {

//            String query = IOUtils.toString(this.getClass()
//                    .getResource("/q1.sparql"));

            WGS84Location location = new WGS84Location(2.349014d,48.864716d,15.0d,"Paris","This is paris");
            String query = getWGS84SparqlQuery(location,"1");

            request.setQuery(query);
            request.setOutputFormat(SparqlQueryOutputFormat.CSV);

            request.setToken("This is token");

            String result = searchHandler.sparqlSearch(request);
            assertNotNull(result);
            assertFalse(result.isEmpty());

            BufferedReader reader = new BufferedReader(new StringReader(result));
            String l1 = reader.readLine();
            String l2 = reader.readLine();
            String l3 = reader.readLine();
            assertEquals("location",l1);
            String uri = l2;
            System.out.println("Found uri: <" + uri + ">");
            assertNull("Found too many locations",l3);
            reader.close();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testListLocationTypesQuery() {
        CoreSparqlQueryRequest request = new CoreSparqlQueryRequest();
        try {

            String query = IOUtils.toString(this.getClass()
                    .getResource("/qListLocationTypes.sparql"));

            request.setQuery(query);
            request.setOutputFormat(SparqlQueryOutputFormat.CSV);

            request.setToken("This is token");

            String result = searchHandler.sparqlSearch(request);
            assertNotNull(result);
            assertFalse(result.isEmpty());

            BufferedReader reader = new BufferedReader(new StringReader(result));
            String line;
            while((line = reader.readLine()) != null ) {
                System.out.println(line);
            }
        } catch (Exception e) {
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
