package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryOutputFormat;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.MultiSearchHandler;
import eu.h2020.symbiote.mappings.MappingManager;
import eu.h2020.symbiote.model.cim.WGS84Location;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.ranking.RankingHandler;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Mael on 26/01/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class SearchHandlerTests {

//    private SearchStorage storage;
    private Registry registry;
    private TripleStore triplestore;
    private MultiSearchHandler searchHandler;
    @Mock
    private SecurityManager securityManager;
    @Mock
    private RankingHandler rankingHandler;
    @Mock
    private MappingManager mappingManager;


    @Before
    public void setUp() throws Exception {
        when(securityManager.checkPolicyByResourceId(anyString(),any(),any())).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(anyString(),any(),any())).thenReturn(Boolean.TRUE);


//        storage = SearchStorage.getInstance(SearchStorage.TESTCASE_STORAGE_NAME);
        triplestore = new TripleStore(securityManager,false);
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD" );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_STATIONARY_URI,stationaryModel);
            searchHandler = new MultiSearchHandler(triplestore, true, securityManager, rankingHandler, mappingManager , false,5,10,5);

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
        query.append("\t\tcim:name \""+ location.getName()+ "\" ;\n");
        query.append("\t\tcim:description \"" + location.getDescription().get(0) + "\" .\n");

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

            WGS84Location location = new WGS84Location(2.349014d,48.864716d,15.0d, "Paris",Arrays.asList("This is paris"));
            String query = getWGS84SparqlQuery(location,"1");

            request.setBody(query);
            request.setOutputFormat(SparqlQueryOutputFormat.CSV);

            request.setSecurityRequest(null);

            SearchCommunicationHandler comm1 = new SearchCommunicationHandler("1",null,null,null,null);
            SparqlQueryResponse result = searchHandler.sparqlSearch(comm1,request);
            assertNotNull(result);
            assertFalse(result.getBody().isEmpty());

            BufferedReader reader = new BufferedReader(new StringReader(result.getBody()));
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

            request.setBody(query);
            request.setOutputFormat(SparqlQueryOutputFormat.CSV);

            request.setSecurityRequest(null);

            SearchCommunicationHandler comm1 = new SearchCommunicationHandler("1",null,null,null,null);
            SparqlQueryResponse result = searchHandler.sparqlSearch(comm1,request);
            assertNotNull(result);
            assertFalse(result.getBody().isEmpty());

            BufferedReader reader = new BufferedReader(new StringReader(result.getBody()));
            String line;
            while((line = reader.readLine()) != null ) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultivalueSearch() throws InterruptedException {
        when(securityManager.checkGroupPolicies(anyList(),any())).thenAnswer(i -> i.getArguments()[0]);
        CoreQueryRequest searchReq = new CoreQueryRequest();
        searchReq.setObserved_property(Arrays.asList("temperature"));
        SearchCommunicationHandler comm1 = Mockito.mock( SearchCommunicationHandler.class); //new SearchCommunicationHandler("1",null,null,null,null);
        QueryResponse searchResponse = sendSearchAndGetResponse(searchHandler,comm1,searchReq);

        assertNotNull("Response must not be null", searchResponse);
        assertNotNull("Response resources must not be null",searchResponse.getResources());
        assertEquals("Should return 1 temperature sensor",1,searchResponse.getResources().size());

        searchReq = new CoreQueryRequest();
        searchReq.setObserved_property(Arrays.asList("pH"));
        SearchCommunicationHandler comm2 = Mockito.mock( SearchCommunicationHandler.class);
        searchResponse = sendSearchAndGetResponse(searchHandler,comm2,searchReq);
        assertNotNull("Response must not be null", searchResponse);
        assertNotNull("Response resources must not be null",searchResponse.getResources());
        assertEquals("Should return 0 pH sensors",0,searchResponse.getResources().size());
    }



    private void ensureFieldsNotNull( QueryResourceResult response ) {
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
