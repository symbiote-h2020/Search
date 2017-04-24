package eu.h2020.symbiote;

import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.query.SearchResponse;
import eu.h2020.symbiote.query.SearchResponseResource;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by Mael on 23/01/2017.
 */

@RunWith(MockitoJUnitRunner.class)
public class QueryGenerationTests {

    private Registry registry;
    private TripleStore triplestore;

    @Before
    public void setUp() {
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

//            Model res101Model = loadFileAsModel( RESOURCE_101_FILENAME );
//            Model res102Model = loadFileAsModel( RESOURCE_102_FILENAME);
//            Model res103Model = loadFileAsModel( RESOURCE_103_FILENAME);
//            Model res201Model = loadFileAsModel( RESOURCE_201_FILENAME );
//            Model res202Model = loadFileAsModel( RESOURCE_202_FILENAME );
//            Model res301Model = loadFileAsModel( RESOURCE_301_FILENAME );



            //TODO delete
//            triplestore.insertGraph(null,res101Model);

//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res101Model);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res102Model);
//            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res103Model);
//
//            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI, RESOURCE_201_URI,res201Model);
//            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI, RESOURCE_202_URI,res202Model);
//            registry.registerResource(PLATFORM_C_URI,PLATFORM_C_SERVICE_URI, RESOURCE_301_URI,res301Model);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD" );
            Model mobileModel = loadFileAsModel(RESOURCE_MOBILE_FILENAME, "JSONLD" );
            Model serviceModel = loadFileAsModel(RESOURCE_SERVICE_FILENAME, "JSONLD" );
            Model actautingServiceModel = loadFileAsModel(RESOURCE_ACTUATING_SERVICE_FILENAME, "JSONLD" );
            Model actuatorModel = loadFileAsModel(RESOURCE_ACTUATOR_FILENAME, "JSONLD" );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_STATIONARY_URI,stationaryModel);
            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI,RESOURCE_MOBILE_URI,mobileModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_SERVICE_URI,serviceModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATOR_URI,actuatorModel);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_ACTUATING_SERVICE_URI,actautingServiceModel);


            triplestore.printDataset();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void testSearchByPlatformId() {
//        String query = new QueryGenerator().addPlatformId("1").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 4 + " but got " + size, 4, size);
//
//        //Platform 2
//        query = new QueryGenerator().addPlatformId("2").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 2 + " but got " + size, 2, size);
//
//        //Platform 3
//        query = new QueryGenerator().addPlatformId("3").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 1 + " but got " + size, 1, size);
//    }
//
//    @Test
//    public void testSearchByPlatformId_notExistingPlatform() {
//
//        //Not existing platform
//        String query = new QueryGenerator().addPlatformId("123456").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByPlatformName() {
//        String query = new QueryGenerator().addPlatformName("Platform A").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 4 + " but got " + size, 4, size);
//
//        //Platform 2
//        query = new QueryGenerator().addPlatformName("Platform B").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 2 + " but got " + size, 2, size);
//
//        //Platform 3
//        query = new QueryGenerator().addPlatformName("Platform C").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 1 + " but got " + size, 1, size);
//    }
//
//    @Test
//    public void testSearchByPlatformName_notExistingPlatform() {
//
//        //Not existing platform
//        String query = new QueryGenerator().addPlatformName("Platform 12345").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByLikePlatformName() {
//        String query = new QueryGenerator().addLikePlatformName("Plat","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 7 + " but got " + size, 7, size);
//
//        //Platform 2
//        query = new QueryGenerator().addLikePlatformName("form B","CONTAINS").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 2 + " but got " + size, 2, size);
//    }
//
//    @Test
//    public void testSearchByLikePlatformName_notExistingPlatform() {
//        //Not existing platform
//        String query = new QueryGenerator().addLikePlatformName("Platform 12345","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Platform query should return " + 0 + " but got " + size, 0, size);
//    }
//
    @Test
    public void testSearchByResourceName() {

        System.out.println("----------- TEST -------------");
        String query = new QueryGenerator()
                .addResourceName(RESOURCE_STATIONARY_LABEL)
                .toString();
        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        System.out.println("SIze " + size);
        printSearchResponses(resultSet);
//        printSearchResponsesFormatter(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//
//        //Platform 2
//        query = new QueryGenerator().addResourceName("Resource 202").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);

//        //Platform 3
//        query = new QueryGenerator().addResourceName("Resource 301").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
    }
//
//    @Test
//    public void testSearchByResourceName_notExistingResource() {
//
//        //Not existing platform
//        String query = new QueryGenerator().addResourceName("Resource 12345").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceName() {
//        String query = new QueryGenerator().addLikeResourceName("res","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 7 + " but got " + size, 7, size);
//
//        query = new QueryGenerator().addLikeResourceName("01","CONTAINS").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 4 + " but got " + size, 4, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceName_notExisting() {
//        String query = new QueryGenerator().addResourceName("aaaaa").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByResourceId() {
//        String query = new QueryGenerator().addResourceId("101").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//
//        //Platform 2
//        query = new QueryGenerator().addResourceId("202").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
//
//        //Platform 3
//        query = new QueryGenerator().addResourceId("301").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
//    }
//
//    @Test
//    public void testSearchByResourceId_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addResourceId("12345").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByResourceDescription() {
//        String query = new QueryGenerator().addResourceDescription("This is resource 101").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//
//        //Platform 2
//        query = new QueryGenerator().addResourceDescription("This is resource 201").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
//
//        //Platform 3
//        query = new QueryGenerator().addResourceDescription("This is resource 301").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
//    }
//
//    @Test
//    public void testSearchByResourceDescription_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addResourceDescription("This is resource 10").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceDescription() {
//        String query = new QueryGenerator().addLikeResourceDescription("This is resource ","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 7 + " but got " + size, 7, size);
//
//        //Platform 2
//        query = new QueryGenerator().addLikeResourceDescription("This is resource 20","CONTAINS").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//
//        //Platform 3
//        query = new QueryGenerator().addLikeResourceDescription("01","CONTAINS").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 4 + " but got " + size, 4, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceDescription_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addResourceDescription("This is resource 12345").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByResourceLocationName() {
//        String query = new QueryGenerator().addResourceLocationName("Poznan").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 4 + " but got " + size, 4, size);
//
//        //Platform 2
//        query = new QueryGenerator().addResourceLocationName("Somewhere").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//    }
//
//    @Test
//    public void testSearchByResourceLocationName_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addResourceLocationName("12345location").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceLocationName() {
//        String query = new QueryGenerator().addLikeResourceLocationName("Poznan","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 4 + " but got " + size, 4, size);
//
//        //Platform 2
//        query = new QueryGenerator().addLikeResourceLocationName("Somewhere","CONTAINS").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceLocationName_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addLikeResourceLocationName("12345location","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByResourceObservedPropertyName() {
//        String query = new QueryGenerator().addResourceObservedPropertyName("Temperature").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
//
//        //Platform 2
//        query = new QueryGenerator().addResourceObservedPropertyName("Humidity").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//    }
//
//    @Test
//    public void testSearchByResourceObservedPropertyName_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addResourceObservedPropertyName("12345property").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceObservedPropertyName() {
//        String query = new QueryGenerator().addLikeResourceObservedPropertyName("Temp","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 3 + " but got " + size, 3, size);
//
//        //Platform 2
//        query = new QueryGenerator().addLikeResourceObservedPropertyName("Hum","CONTAINS").toString();
//        resultSet = triplestore.executeQuery(query);
//        size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//    }
//
//    @Test
//    public void testSearchByLikeResourceObservedPropertyName_NotExistingResource() {
//        //Not existing platform
//        String query = new QueryGenerator().addLikeResourceObservedPropertyName("12345property","CONTAINS").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testSearchByResourceObservedPropertyNames() {
//        List<String> names = new ArrayList<String>();
//        names.add("Temperature");
//        names.add("Humidity");
//
//        String query = new QueryGenerator().addResourceObservedPropertyNames(names).toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
//    }
//
//    @Test
//    public void testSearchByNearby() {
//        Double latitude = Double.valueOf("52.401790");
//        Double longitude = Double.valueOf("16.960144");
//        Integer distance = Integer.valueOf("1000");
//
//        String query = new QueryGenerator().addResourceLocationDistance(latitude,longitude,distance).toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 4 + " but got " + size, 4, size);
//    }
//
//    @Test
//    public void testSearchByNearby_NotExisting() {
//        Double latitude = Double.valueOf("72.401790");
//        Double longitude = Double.valueOf("26.960144");
//        Integer distance = Integer.valueOf("1000");
//
//        String query = new QueryGenerator().addResourceLocationDistance(latitude,longitude,distance).toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 0 + " but got " + size, 0, size);
//    }
//
//    @Test
//    public void testNearbyAndObservedProperty() {
//        Double latitude = Double.valueOf("52.401790");
//        Double longitude = Double.valueOf("16.960144");
//        Integer distance = Integer.valueOf("1000");
//
//        String query = new QueryGenerator().addResourceLocationDistance(latitude,longitude,distance)
//                .addResourceObservedPropertyName("Temperature").toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 2 + " but got " + size, 2, size);
//    }
//
//    @Test
//    public void testNearbyAndObservedPropertyAndComment() {
//        Double latitude = Double.valueOf("52.401790");
//        Double longitude = Double.valueOf("16.960144");
//        Integer distance = Integer.valueOf("1000");
//
//        String query = new QueryGenerator().addResourceObservedPropertyName("Temperature")
//                .addResourceLocationDistance(latitude,longitude,distance)
//                .addLikeResourceDescription("01","CONTAINS")
//                .toString();
//        ResultSet resultSet = triplestore.executeQuery(query);
//        int size = countResultSetSize(resultSet);
//        assertEquals("Resource query should return " + 1 + " but got " + size, 1, size);
//    }
//
    private int countResultSetSize( ResultSet resultSet ) {
        int i = 0;
        while (resultSet.hasNext()) {
            QuerySolution next = resultSet.next();

            i++;
        }
        return i;
    }

    private void printSearchResponses( ResultSet resultSet) {
        SearchResponse searchResponse = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        int i = 0;
        for(SearchResponseResource response: searchResponse.getResourceList()) {
            i++;
            System.out.println(i + "  " + response.getName() + "  |  " + response.getObservedProperties() );
            System.out.println("     " + response.getType() );

        }
    }

    private void printSearchResponsesFormatter( ResultSet resultSet) {
        ResultSetFormatter.out(System.out,resultSet);
    }

    private void executeQuery( TripleStore store, String filename ) throws IOException {
        String query = IOUtils.toString(this.getClass()
                .getResource(filename));
        ResultSet resultSet = store.executeQuery(query);
        System.out.println(">>>>>>>>>>>>> Executing query " + filename);
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            Iterator<String> varNames = solution.varNames();
            String temp = "";
            while (varNames.hasNext()) {
                String var = varNames.next();
                if (!temp.isEmpty()) {
                    temp += ", ";
                }
                temp += var + " = " + solution.get(var).toString();
            }
            System.out.println( temp );
        }
        System.out.println("<<<<<<<<<<<<< SearchRequest finished ");
    }

}
