package eu.h2020.symbiote;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.ResourceType;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Created by Mael on 23/01/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class QueryGenerationTests {

    private Registry registry;
    private TripleStore triplestore;
    @Mock
    private SecurityManager securityManager;

    @Before
    public void setUp() throws Exception {
        when(securityManager.checkPolicyByResourceId(anyString(),any(),any())).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(anyString(),any(),any())).thenReturn(Boolean.TRUE);

        triplestore = new TripleStore( securityManager, false);
        registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle);

            String platformB = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_B_FILENAME));
            registry.registerPlatform(PLATFORM_B_ID, platformB, RDFFormat.Turtle);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD");
            Model mobileModel = loadFileAsModel(RESOURCE_MOBILE_FILENAME, "JSONLD");
            Model serviceModel = loadFileAsModel(RESOURCE_SERVICE_FILENAME, "JSONLD");
//            Model actuatingServiceModel = loadFileAsModel(RESOURCE_ACTUATING_SERVICE_FILENAME, "JSONLD");
            Model actuatorModel = loadFileAsModel(RESOURCE_ACTUATOR_FILENAME, "JSONLD");
//            Model stationaryDeviceModel = loadFileAsModel(RESOURCE_STATIONARYDEVICE_FILENAME, "JSONLD");
            Model deviceModel = loadFileAsModel("/exampleDevice.json", "JSONLD");
//            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, "http://www.nextworks.it/sensors/device1", deviceModel);

            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_STATIONARY_URI, stationaryModel);
            registry.registerResource(PLATFORM_B_URI, PLATFORM_B_SERVICE_URI, RESOURCE_MOBILE_URI, mobileModel);
            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_SERVICE_URI, serviceModel);
            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_ACTUATOR_URI, actuatorModel);
//            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_ACTUATING_SERVICE_URI, actuatingServiceModel);
//            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_STATIONARYDEVICE_URI, stationaryDeviceModel);

            triplestore.printDataset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchAll() {
        String query = new QueryGenerator().toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 4 + " but got " + size, 4, size);
    }

    @Test
    public void testSearchByPlatformId() {
        String query = new QueryGenerator().addPlatformId("1").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 3 + " but got " + size, 3, size);
    }

    @Test
    public void testSearchByPlatformId_notExistingPlatform() {

        //Not existing platform
        String query = new QueryGenerator().addPlatformId("123456").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByPlatformName() {
        String query = new QueryGenerator()
                .addPlatformName("Platform A")
                .toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
    }

    @Test
    public void testSearchByPlatformName_notExistingPlatform() {
        String query = new QueryGenerator().addPlatformName("Platform 12345").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByLikePlatformName() {
        String query = new QueryGenerator().addLikePlatformName("Plat", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 4 + " but got " + size, 4, size);

        //Platform 2
        query = new QueryGenerator().addLikePlatformName("form B", "CONTAINS").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByLikePlatformName_notExistingPlatform() {
        //Not existing platform
        String query = new QueryGenerator().addLikePlatformName("Platform 12345", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Platform query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByResourceName() {

        String query = new QueryGenerator()
                .addResourceName(RESOURCE_STATIONARY_LABEL)
                .toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

        query = new QueryGenerator()
                .addResourceName(RESOURCE_MOBILE_LABEL)
                .toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

        query = new QueryGenerator()
                .addResourceName(RESOURCE_SERVICE_LABEL)
                .toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

//        query = new QueryGenerator()
//                .addResourceName(RESOURCE_ACTUATING_SERVICE_LABEL)
//                .toString();
//        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
//        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

        query = new QueryGenerator()
                .addResourceName(RESOURCE_ACTUATOR_LABEL)
                .toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

//        query = new QueryGenerator()
//                .addResourceName(RESOURCE_STATIONARYDEVICE_LABEL)
//                .toString();
//        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);

        QueryResponse resp = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        for (QueryResourceResult singleResp : resp.getResources()) {
            System.out.println(singleResp.getId());
        }
    }

    @Test
    public void testSearchByResourceName_notExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addResourceName("Resource 12345").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByLikeResourceName() {
        String query = new QueryGenerator().addLikeResourceName("tionary", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

        query = new QueryGenerator().addLikeResourceName("service", "CONTAINS").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByLikeResourceName_notExisting() {
        String query = new QueryGenerator().addResourceName("aaaaa").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByResourceId() {
        String query = new QueryGenerator().addResourceId("stationary1").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

        query = new QueryGenerator().addResourceId("mobile1").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

        //Platform 3
        query = new QueryGenerator().addResourceId("service1").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByResourceId_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addResourceId("12345").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByResourceDescription() {
        String query = new QueryGenerator().addResourceDescription("This is stationary 1").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByResourceDescription_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addResourceDescription("This is resource 10").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByLikeResourceDescription() {
        String query = new QueryGenerator().addLikeResourceDescription("This is ", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 4 + " but got " + size, 4, size);

        //Platform 2
        query = new QueryGenerator().addLikeResourceDescription("service", "CONTAINS").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByLikeResourceDescription_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addResourceDescription("This is resource 12345").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByResourceLocationName() {
        String query = new QueryGenerator().addResourceLocationName("Paris").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
    }

    @Test
    public void testSearchByResourceLocationName_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addResourceLocationName("12345location").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByLikeResourceLocationName() {
        String query = new QueryGenerator().addLikeResourceLocationName("ris", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
    }

    @Test
    public void testSearchByLikeResourceLocationName_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addLikeResourceLocationName("12345location", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByResourceObservedPropertyName() {
        String query = new QueryGenerator()
                .addResourceObservedPropertyName("temperature")
                .toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);

        //Platform 2
        query = new QueryGenerator().addResourceObservedPropertyName("humidity").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByResourceObservedPropertyName_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addResourceObservedPropertyName("12345property").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    //
    @Test
    public void testSearchByLikeResourceObservedPropertyName() {
        String query = new QueryGenerator().addLikeResourceObservedPropertyName("temp", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);

        query = new QueryGenerator().addLikeResourceObservedPropertyName("mperat", "CONTAINS").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);

        //Platform 2
        query = new QueryGenerator().addLikeResourceObservedPropertyName("hum", "CONTAINS").toString();
        resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByLikeResourceObservedPropertyName_NotExistingResource() {
        //Not existing platform
        String query = new QueryGenerator().addLikeResourceObservedPropertyName("12345property", "CONTAINS").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testSearchByResourceObservedPropertyNames() {
        List<String> names = new ArrayList<>();
        names.add("temperature");
        names.add("humidity");

        String query = new QueryGenerator().addResourceObservedPropertyNames(names).toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    //TODO fix nearby
    @Test
    public void testSearchByNearby() {
        Double latitude = Double.valueOf("48.864716");
        Double longitude = Double.valueOf("2.349014");
        Integer distance = Integer.valueOf("1000");

        String query = new QueryGenerator().addResourceLocationDistance(latitude, longitude, distance).toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);

        int size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
    }

    //TODO fix
    @Test
    public void testSearchByNearby_NotExisting() {
        Double latitude = Double.valueOf("72.401790");
        Double longitude = Double.valueOf("55.960144");
        Integer distance = Integer.valueOf("1");

        String query = new QueryGenerator().addResourceLocationDistance(latitude, longitude, distance).toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
    }

    @Test
    public void testNearbyAndObservedProperty() {
        Double latitude = Double.valueOf("48.864716");
        Double longitude = Double.valueOf("2.349014");
        Integer distance = Integer.valueOf("1000");

        String query = new QueryGenerator().addResourceLocationDistance(latitude, longitude, distance)
                .addResourceObservedPropertyName("humidity").toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testNearbyAndObservedPropertyAndComment() {
        Double latitude = Double.valueOf("48.864716");
        Double longitude = Double.valueOf("2.349014");
        Integer distance = Integer.valueOf("1000");

        String query = new QueryGenerator().addResourceObservedPropertyName("temperature")
                .addResourceLocationDistance(latitude, longitude, distance)
                .addLikeResourceDescription("stat", "CONTAINS")
                .toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countResultSetSizeByGeneratingResponseFromResultSet(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    @Test
    public void testSearchByResourceType() {
        //Not existing platform
        String query = new QueryGenerator().addResourceType(ResourceType.ACTUATOR.getUri()).toString();
        ResultSet resultSet = triplestore.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
        int size = countAndPrintSearchResponses(resultSet);
        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }

    private void printSearchResponsesFormatter(ResultSet resultSet) {
        ResultSetFormatter.out(System.out, resultSet);
    }

//    private void executeQueryOnUnionGraph(TripleStore store, String filename) throws IOException {
//        String query = IOUtils.toString(this.getClass()
//                .getResource(filename));
//        ResultSet resultSet = store.executeQueryOnGraph(query,TripleStore.UNION_GRAPH,null,false);
//        printSearchResponsesFormatter(resultSet);
//    }

}
