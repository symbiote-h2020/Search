package eu.h2020.symbiote;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceType;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Created by Mael on 23/01/2017.
 */
public class TestSetupConfig {

    public static final String PLATFORM_A_ID = "1";
    public static final String PLATFORM_A_NAME = "Platform A";
    public static final String PLATFORM_A_MODEL_ID = "11";
    public static final String PLATFORM_A_DESCRIPTION = "Test platform A";
    public static final String PLATFORM_A_FILENAME = "/platformA.ttl";
    //RDF URIs
    public static final String PLATFORM_A_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/1";
    public static final String PLATFORM_A_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/1/service/somehost1.com/resourceAccessProxy";

    //LINK to Interworking Service
    //TODO check
    public static final String PLATFORM_A_URL = "http://somehost1.com/resourceAccessProxy";

    public static final String PLATFORM_A_NAME_UPDATED = "Platform1Updated";
    public static final String PLATFORM_A_MODEL_ID_UPDATED = "11Updated";
    public static final String PLATFORM_A_DESCRIPTION_UPDATED = "11descUpdated";
    public static final String PLATFORM_A_URL_UPDATED = "http://somehost1.com/resourceAccessProxyUpdated";

    public static final String PLATFORM_B_ID = "2";
    public static final String PLATFORM_B_NAME = "Platform B";
    public static final String PLATFORM_B_MODEL_ID = "21";
    public static final String PLATFORM_B_DESCRIPTION = "Test platform B";
    public static final String PLATFORM_B_FILENAME = "/platformB.ttl";
    public static final String PLATFORM_B_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/2";
    public static final String PLATFORM_B_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/2/service/somehost2.com/resourceAccessProxy";

    public static final String PLATFORM_C_ID = "3";
//    public static final String PLATFORM_C_NAME = "Platform C";
//    public static final String PLATFORM_C_MODEL_ID = "31";
//    public static final String PLATFORM_C_DESCRIPTION = "Test platform C";
    public static final String PLATFORM_C_FILENAME = "/platformC.ttl";
    public static final String PLATFORM_C_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/3";
    public static final String PLATFORM_C_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/3/service/somehost3.com/resourceAccessProxy";

    public static final String RESOURCE_PREDICATE = "http://www.symbiote-h2020.eu/ontology/internal/resources/";

    public static final String RESOURCE_101_FILENAME = "/exampleStationarySensor.json";
    public static final String RESOURCE_101_URI = RESOURCE_PREDICATE + "stationary1";
    public static final String RESOURCE_101_LABEL = "Stationary 1";
    public static final String RESOURCE_101_COMMENT = "This is stationary 1";
    public static final String RESOURCE_101_ID = "stationary1";
//    public static final String RESOURCE_101_LOC_LABEL = "Poznan";
//    public static final String RESOURCE_101_LOC_COMMENT = "Poznan Malta";
//    public static final String RESOURCE_101_LOC_LAT = "52.401790";
//    public static final String RESOURCE_101_LOC_LONG = "16.960144";
//    public static final String RESOURCE_101_LOC_ALT = "200";
//    public static final String RESOURCE_101_OBS1_LABEL = "Temperature";
//    public static final String RESOURCE_101_OBS2_LABEL = "Humidity";
//    public static final String RESOURCE_101_LABEL_UPDATE = "Resource Hundred One";

    public static final String RESOURCE_102_FILENAME = "/old_r2_models/resource102.ttl";
    public static final String RESOURCE_102_URI = RESOURCE_PREDICATE + "102";
    public static final String RESOURCE_103_FILENAME = "/old_r2_models/resource103.ttl";
    public static final String RESOURCE_103_URI = RESOURCE_PREDICATE + "103";
    public static final String RESOURCE_201_FILENAME = "/old_r2_models/resource201.ttl";
    public static final String RESOURCE_201_URI = RESOURCE_PREDICATE + "201";
    public static final String RESOURCE_202_FILENAME = "/old_r2_models/resource202.ttl";
    public static final String RESOURCE_202_URI = RESOURCE_PREDICATE + "202";
    public static final String RESOURCE_301_FILENAME = "/old_r2_models/resource301.ttl";
    public static final String RESOURCE_301_URI = RESOURCE_PREDICATE + "301";


    public static final String RESOURCE_STATIONARY_FILENAME = "/exampleStationarySensor.json";
    public static final String RESOURCE_STATIONARY_FILENAME_MODIFIED = "/exampleStationarySensorModified.json";
    public static final String RESOURCE_STATIONARY_LABEL = "Stationary 1";
    public static final String RESOURCE_STATIONARY_LABEL_MODIFIED = "New sensor 1";
    public static final String RESOURCE_STATIONARY_COMMENT = "This is stationary 1";
    public static final String RESOURCE_STATIONARY_COMMENT_MODIFIED = "New sensor 1 description";
    public static final String RESOURCE_STATIONARY_URI = RESOURCE_PREDICATE + "stationary1";
    public static final String RESOURCE_STATIONARY_ID = "stationary1";

//    public static final String RESOURCE_STATIONARYDEVICE_FILENAME = "/r2_models/exampleStationaryDevice.json";
//    public static final String RESOURCE_STATIONARYDEVICE_LABEL = "Stationary device 1";
//    public static final String RESOURCE_STATIONARYDEVICE_COMMENT = "This is Stationary Device 1";
//    public static final String RESOURCE_STATIONARYDEVICE_URI = RESOURCE_PREDICATE + "stationarydevice1";
//    public static final String RESOURCE_STATIONARYDEVICE_ID = "stationardevice1";

    public static final String RESOURCE_MOBILE_FILENAME = "/exampleMobileSensor.json";
    public static final String RESOURCE_MOBILE_LABEL = "Mobile 1";
    public static final String RESOURCE_MOBILE_URI = RESOURCE_PREDICATE + "mobile1";
    public static final String RESOURCE_MOBILE_ID = "mobile1";

    public static final String RESOURCE_SERVICE_FILENAME = "/exampleService.json";
    public static final String RESOURCE_SERVICE_LABEL = "Service 1";
    public static final String RESOURCE_SERVICE_URI = RESOURCE_PREDICATE + "service1";
//    public static final String RESOURCE_SERVICE_ID = "service1";

//    public static final String RESOURCE_ACTUATING_SERVICE_FILENAME = "/r2_models/exampleActuatingService.json";
//    public static final String RESOURCE_ACTUATING_SERVICE_LABEL = "Actuating Service 1";
//    public static final String RESOURCE_ACTUATING_SERVICE_URI = RESOURCE_PREDICATE + "actuatingService1";

    public static final String RESOURCE_ACTUATOR_FILENAME = "/exampleActuator.json";
    public static final String RESOURCE_ACTUATOR_LABEL = "Actuator 1";
    public static final String RESOURCE_ACTUATOR_URI = RESOURCE_PREDICATE + "actuator1";


    public static Model loadFileAsModel(String fileLocation, String format ) {
        Model model = null;
        InputStream modelToSave = null;
        try {
            modelToSave = IOUtils.toInputStream(IOUtils.toString(TestSetupConfig.class
                    .getResource(fileLocation)));
            model = ModelFactory.createDefaultModel();
            model.read(modelToSave, null, format);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelToSave != null) {
                try {
                    modelToSave.close();
                } catch (Exception e) {
                }
            }
        }
        return model;
    }

    public static Platform generatePlatformA() {
        return generatePlatform(PLATFORM_A_ID,PLATFORM_A_FILENAME, RDFFormat.Turtle,PLATFORM_A_URL, PLATFORM_A_MODEL_ID,  PLATFORM_A_DESCRIPTION, PLATFORM_A_NAME);
    }

    public static Platform generatePlatformAUpdate() {
        return generatePlatform(PLATFORM_A_ID,PLATFORM_A_FILENAME,RDFFormat.Turtle,PLATFORM_A_URL_UPDATED, PLATFORM_A_MODEL_ID_UPDATED,  PLATFORM_A_DESCRIPTION_UPDATED, PLATFORM_A_NAME_UPDATED);
    }

    public static Platform generatePlatform( String platformId, String platformRdfFilename, RDFFormat rdfFormat, String interworkingServiceUrl,
                                                         String modelId, String comment, String label) {
        Platform platform = new Platform();
        platform.setId(platformId);
        try {
            platform.setRdf(IOUtils.toString(TestSetupConfig.class
                    .getResourceAsStream(platformRdfFilename)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        platform.setRdfFormat(rdfFormat);
        InterworkingService interworkingService = new InterworkingService();
        interworkingService.setUrl(interworkingServiceUrl);
        interworkingService.setInformationModelId(modelId);
        platform.setInterworkingServices( Arrays.asList(interworkingService) );
        platform.setDescription(Arrays.asList(comment));
        platform.setName(label);
        return platform;
    }

//    public static Resource generateResource() {
//        Resource res = new Resource();
//        res.setPlatformId(PLATFORM_A_ID);
//        res.setDescription(RESOURCE_101_COMMENT);
////        res.setFeatureOfInterest();
//        res.setId(RESOURCE_101_ID);
//        res.setName(RESOURCE_101_LABEL);
////        res.setOwner();
//        res.setResourceURL(PLATFORM_A_URL);
//        res.setLocation(
//                new Location("id",
//                        RESOURCE_101_LOC_LABEL,
//                        RESOURCE_101_LOC_COMMENT,
//                        Double.valueOf(RESOURCE_101_LOC_LAT),
//                        Double.valueOf(RESOURCE_101_LOC_LONG),
//                        Double.valueOf(RESOURCE_101_LOC_ALT)));
//        ArrayList<String> list = new ArrayList<>();
//        list.add(RESOURCE_101_OBS1_LABEL);
//        list.add(RESOURCE_101_OBS2_LABEL);
//        res.setObservedProperties(list);
//        return res;
//    }


    public static CoreResource generateResource() {
        return generateSensor(RESOURCE_101_LABEL, RESOURCE_101_COMMENT, RESOURCE_101_ID, PLATFORM_A_URL, RESOURCE_STATIONARY_FILENAME, RDFFormat.JSONLD);
    }

//    public static CoreResource generateStationarySensor() {
//        return generateSensor(RESOURCE_STATIONARY_LABEL,RESOURCE_STATIONARY_COMMENT,RESOURCE_STATIONARY_ID,PLATFORM_A_URL, RESOURCE_STATIONARY_FILENAME, RDFFormat.JSONLD );
//    }

    public static CoreResource generateModifiedStationarySensor() {
        return generateSensor(RESOURCE_STATIONARY_LABEL_MODIFIED,RESOURCE_STATIONARY_COMMENT_MODIFIED,RESOURCE_STATIONARY_ID,PLATFORM_A_URL, RESOURCE_STATIONARY_FILENAME_MODIFIED, RDFFormat.JSONLD );
    }

    public static CoreResource generateSensor(String label, String comment, String id, String serviceUrl, String rdfFilename, RDFFormat format) {
        CoreResource res = new CoreResource();
        res.setDescription(Arrays.asList(comment));
        res.setName(label);
        res.setId(id);
        res.setInterworkingServiceURL(serviceUrl);
        res.setType(CoreResourceType.STATIONARY_SENSOR);
        try {
            res.setRdf(IOUtils.toString(TestSetupConfig.class
                    .getResourceAsStream(rdfFilename)));
            res.setRdfFormat(format);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    public static int countResultSetSizeByNextMethod(ResultSet resultSet) {
        //Old method for generic query
//        QueryResponse searchResponse = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
//        int i = searchResponse.getResources().size();

        //New method, just go through result set...
        int i = 0;

        while( resultSet.hasNext() ) {
            QuerySolution next = resultSet.next();
            Iterator<String> varIterator = next.varNames();
            if( i == 0 ) {
                System.out.print( "||||" );
                while( varIterator.hasNext() ) {
                    System.out.print( "  " + varIterator.next() + "  |");
                }
                System.out.println( "|||" );
                varIterator = next.varNames();
            }

            while( varIterator.hasNext() ) {
                System.out.print("  " + next.get(varIterator.next()).toString() + "  " );
            }
            System.out.println("");
            i++;
        }
        return i;
    }

    public static int countResultSetSizeByGeneratingResponseFromResultSet(ResultSet resultSet) {
        QueryResponse searchResponse = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        int i = searchResponse.getResources().size();
        return i;
    }

    public static int countAndPrintSearchResponses(ResultSet resultSet) {
        QueryResponse searchResponse = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        int i = 0;
        for (QueryResourceResult response : searchResponse.getResources()) {
            i++;
            System.out.println(i + "  " + response.getName() + "  |  " + response.getObservedProperties() + "  typee   " + response.getResourceType());
            System.out.println("  lat " + response.getLocationLatitude() + "  long " + response.getLocationLongitude()  );

        }
        return i;
    }

}
