package eu.h2020.symbiote;

import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.model.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Created by Mael on 23/01/2017.
 */
public class TestSetupConfig {

    public static final String QUERY_OBSERVEDPROERTY = "/queryByObservedProperty.sparql";

    public static final String PLATFORM_A_ID = "1";
    public static final String PLATFORM_A_NAME = "Platform1";
    public static final String PLATFORM_A_MODEL_ID = "11";
    public static final String PLATFORM_A_DESCRIPTION = "11desc";
    public static final String PLATFORM_A_FILENAME = "/platformA.ttl";
    //RDF URIs
    public static final String PLATFORM_A_URI = "http://www.symbiote-h2020.eu/ontology/platforms/1";
    public static final String PLATFORM_A_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/1/service/somehost1.com/resourceAccessProxy";

    //LINK to Interworking Service
    public static final String PLATFORM_A_URL = "http://somehost1.com/resourceAccessProxy";

    public static final String PLATFORM_A_NAME_UPDATED = "Platform1Updated";
    public static final String PLATFORM_A_MODEL_ID_UPDATED = "11Updated";
    public static final String PLATFORM_A_DESCRIPTION_UPDATED = "11descUpdated";
    public static final String PLATFORM_A_URL_UPDATED = "http://somehost1.com/resourceAccessProxyUpdated";

    public static final String PLATFORM_B_ID = "2";
    public static final String PLATFORM_B_NAME = "Platform2";
    public static final String PLATFORM_B_MODEL_ID = "21";
    public static final String PLATFORM_B_DESCRIPTION = "21desc";
    public static final String PLATFORM_B_FILENAME = "/platformB.ttl";
    public static final String PLATFORM_B_URI = "http://www.symbiote-h2020.eu/ontology/platforms/2";
    public static final String PLATFORM_B_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/2/service/somehost2.com/resourceAccessProxy";

    public static final String PLATFORM_C_ID = "3";
    public static final String PLATFORM_C_NAME = "Platform3";
    public static final String PLATFORM_C_MODEL_ID = "31";
    public static final String PLATFORM_C_DESCRIPTION = "31desc";
    public static final String PLATFORM_C_FILENAME = "/platformC.ttl";
    public static final String PLATFORM_C_URI = "http://www.symbiote-h2020.eu/ontology/platforms/3";
    public static final String PLATFORM_C_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/3/service/somehost3.com/resourceAccessProxy";

    public static final String RESOURCE_PREDICATE = "http://www.symbiote-h2020.eu/ontology/resources/";

    public static final String RESOURCE_101_FILENAME = "/resource101.ttl";
    public static final String RESOURCE_101_URI = RESOURCE_PREDICATE + "101";
    public static final String RESOURCE_101_LABEL = "Resource 101";
    public static final String RESOURCE_101_COMMENT = "Resource 101 comment";
    public static final String RESOURCE_101_ID = "101";
    public static final String RESOURCE_101_LOC_LABEL = "Poznan";
    public static final String RESOURCE_101_LOC_COMMENT = "Poznan Malta";
    public static final String RESOURCE_101_LOC_LAT = "52.401790";
    public static final String RESOURCE_101_LOC_LONG = "16.960144";
    public static final String RESOURCE_101_LOC_ALT = "200";
    public static final String RESOURCE_101_OBS1_LABEL = "Temperature";
    public static final String RESOURCE_101_OBS2_LABEL = "Humidity";
    public static final String RESOURCE_101_LABEL_UPDATE = "Resource Hundred One";

    public static final String RESOURCE_102_FILENAME = "/resource102.ttl";
    public static final String RESOURCE_102_URI = RESOURCE_PREDICATE + "102";
    public static final String RESOURCE_103_FILENAME = "/resource103.ttl";
    public static final String RESOURCE_103_URI = RESOURCE_PREDICATE + "103";
    public static final String RESOURCE_201_FILENAME = "/resource201.ttl";
    public static final String RESOURCE_201_URI = RESOURCE_PREDICATE + "201";
    public static final String RESOURCE_202_FILENAME = "/resource202.ttl";
    public static final String RESOURCE_202_URI = RESOURCE_PREDICATE + "202";
    public static final String RESOURCE_301_FILENAME = "/resource301.ttl";
    public static final String RESOURCE_301_URI = RESOURCE_PREDICATE + "301";

    public static final String RESOURCE_501_FILENAME = "/resource501.ttl";
    public static final String RESOURCE_501_URI = RESOURCE_PREDICATE + "501";

    public static final String RESOURCE_STATIONARY_FILENAME = "/exampleStationarySensor.json";
    public static final String RESOURCE_STATIONARY_LABEL = "Stationary 1";
    public static final String RESOURCE_STATIONARY_URI = RESOURCE_PREDICATE + "stationary1";

    public static final String RESOURCE_MOBILE_FILENAME = "/exampleMobileSensor.json";
    public static final String RESOURCE_MOBILE_LABEL = "Mobile 1";
    public static final String RESOURCE_MOBILE_URI = RESOURCE_PREDICATE + "mobile1";

    public static final String RESOURCE_SERVICE_FILENAME = "/exampleService.json";
    public static final String RESOURCE_SERVICE_LABEL = "Service 1";
    public static final String RESOURCE_SERVICE_URI = RESOURCE_PREDICATE + "service1";

    public static final String RESOURCE_ACTUATING_SERVICE_FILENAME = "/exampleActuatingService.json";
    public static final String RESOURCE_ACTUATING_SERVICE_LABEL = "Actuating Service 1";
    public static final String RESOURCE_ACTUATING_SERVICE_URI = RESOURCE_PREDICATE + "actuatingService1";

    public static final String RESOURCE_ACTUATOR_FILENAME = "/exampleActuator.json";
    public static final String RESOURCE_ACTUATOR_LABEL = "Actuator 1";
    public static final String RESOURCE_ACTUATOR_URI = RESOURCE_PREDICATE + "590b617566e02516806462e4";


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
        Platform platform = new Platform();
        platform.setPlatformId(PLATFORM_A_ID);
        platform.setInformationModelId(PLATFORM_A_MODEL_ID);
        platform.setDescription(PLATFORM_A_DESCRIPTION);
        platform.setName(PLATFORM_A_NAME);
        platform.setUrl(PLATFORM_A_URL);
        return platform;
    }

    public static Platform generatePlatformAUpdate() {
        Platform platform = new Platform();
        platform.setPlatformId(PLATFORM_A_ID);
        platform.setInformationModelId(PLATFORM_A_MODEL_ID_UPDATED);
        platform.setDescription(PLATFORM_A_DESCRIPTION_UPDATED);
        platform.setName(PLATFORM_A_NAME_UPDATED);
        platform.setUrl(PLATFORM_A_URL_UPDATED);
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
        CoreResource res = new CoreResource();
        res.setComments(Arrays.asList(RESOURCE_101_COMMENT));
        res.setLabels(Arrays.asList(RESOURCE_101_LABEL));
        res.setId(RESOURCE_101_ID);
        res.setInterworkingServiceURL(PLATFORM_A_URL);
        try {
            res.setRdf(IOUtils.toString(TestSetupConfig.class
                    .getResourceAsStream(RESOURCE_STATIONARY_FILENAME)));
            res.setRdfFormat(RDFFormat.JSONLD);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }



}
