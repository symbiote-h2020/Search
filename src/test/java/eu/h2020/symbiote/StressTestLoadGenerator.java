package eu.h2020.symbiote;

import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.model.Platform;
import eu.h2020.symbiote.core.model.RDFFormat;
import eu.h2020.symbiote.core.model.WGS84Location;
import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 01/07/2017.
 */
public class StressTestLoadGenerator {

    public static final String TEMPLATE_STATIONARY = "/templates/templateStationarySensor.json";
    public static final String TEMPLATE_MOBILE = "/templates/templateMobileSensor.json";

    static final List<WGS84Location> locations;

    static final List<String> properties;


    static {
        locations = Arrays.asList(
                new WGS84Location(2.183370319d, 41.38329958d, 10.0d, Arrays.asList("Barcelona"), Arrays.asList("Barcelona")),
                new WGS84Location(2.333335326d, 48.86669293d, 25.0d, Arrays.asList("Paris"), Arrays.asList("Paris")),
                new WGS84Location(20.99999955d, 52.25000063d, 10.0d, Arrays.asList("Warsaw"), Arrays.asList("Warsaw")),
                new WGS84Location(23.73332108d, 37.98332623d, 10.0d, Arrays.asList("Athens"), Arrays.asList("Athens")),
                new WGS84Location(139.7514074d, 35.68501691d, 10.0d, Arrays.asList("Tokyo"), Arrays.asList("Tokyo")),
                new WGS84Location(151.1851798d, -33.92001097d, 10.0d, Arrays.asList("Sydney"), Arrays.asList("Sydney")),
                new WGS84Location(12.48325842d, 41.89595563d, 10.0d, Arrays.asList("Rome"), Arrays.asList("Rome")));

        properties = Arrays.asList(
                "http://purl.oclc.org/NET/ssnx/qu/quantity#temperature",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#humidity",
                "http://www.symbiote-h2020.eu/ontology/bim/property#1-HexeneConcentration",
                "http://www.symbiote-h2020.eu/ontology/bim/property#ammonium_precip_Concentration",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#reflectance",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#partialPressure",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#loadPressure",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#width",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#zeroSequenceVoltageComponent",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#impedance",
                "http://purl.oclc.org/NET/ssnx/qu/quantity#ceilingVoltage"
        );
    }

    public void testGenerate() {
        try {
            SearchStorage searchStorage = SearchStorage.getInstance( "F:\\stressTest\\4" );
            generatePlatformsWithResources(searchStorage, 100, 100);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void testCountResources() {
        SearchStorage searchStorage = SearchStorage.getInstance( "F:\\stressTest\\4" );

        String query = new QueryGenerator().toString();
        ResultSet resultSet = searchStorage.getTripleStore().executeQuery(query);
        QueryResponse searchResponse = HandlerUtils.generateSearchResponseFromResultSet(resultSet);
        int i = searchResponse.getResources().size();

        System.out.println("Counted: " + i);
    }

    public void generatePlatformsWithResources(SearchStorage searchStorage, int numberOfPlatforms, int numberOfResourcesPerPlatform) throws IOException {
        for (int platformNumber = 0; platformNumber < numberOfPlatforms; platformNumber++) {
            System.out.println("Generating platform " + platformNumber);
            String platformId = UUID.randomUUID().toString();
            String platformDesc = "This is platform " + platformId;
            String platformName = "Platform_" + platformId;
            String platformUrl = "http://example.com/platform/" + platformId;


            Platform platform = generatePlatform(platformId,PLATFORM_A_FILENAME, RDFFormat.Turtle,platformUrl,"BIM1",platformDesc,platformName);

            Model platformModel = HandlerUtils.generateModelFromPlatform(platform);
            searchStorage.registerPlatform(platform.getId(), platformModel);

            Map<String, String> parameters = new HashMap<String, String>();
            for(int resourceNumber = 0; resourceNumber< numberOfResourcesPerPlatform; resourceNumber++ ) {
                System.out.println(" r " + resourceNumber);
//                System.out.println("");
//                System.out.println("======================== " + resourceNumber + " ===============================");
                parameters.clear();
                String resourceId = UUID.randomUUID().toString();
                String resourceName = "Resource_" + resourceId;
                String resourceDesc = "This is resource " + resourceId;

                parameters.put("resource.id", resourceId);
                parameters.put("resource.name", resourceName);
                parameters.put("resource.desc", resourceDesc);
                addRandomPropertiesForKey("resource.properties", parameters);
                addRandomLocationProperties(parameters);

                //FOI
                parameters.put("foi.name",UUID.randomUUID().toString());
                parameters.put("foi.desc","randomFoi");
                addRandomPropertiesForKey("foi.properties",parameters);

                //Use mapping for file
                String templateString = IOUtils.toString(this.getClass()
                        .getResource(new Random().nextBoolean()?TEMPLATE_STATIONARY:TEMPLATE_MOBILE));
                StrSubstitutor sub = new StrSubstitutor(parameters);
                String resolvedString = sub.replace(templateString);
//
//                System.out.println(" ==== ");
//                System.out.println(resolvedString);
//                System.out.println(" ==== ");

                InputStream resourceInputStream = IOUtils.toInputStream(resolvedString);

                Model mFromFile = ModelFactory.createDefaultModel();
                mFromFile.read(resourceInputStream,null,"JSONLD");

                searchStorage.registerResource(Ontology.getPlatformGraphURI(platform.getId()),
                        HandlerUtils.generateInterworkingServiceUri(Ontology.getPlatformGraphURI(platform.getId()),platform.getInterworkingServices().get(0).getUrl()),RESOURCE_PREDICATE+resourceId, mFromFile);

//                System.out.println(resolvedString);
//                System.out.println("========================================================");
            }
        }
    }

    private void addRandomLocationProperties(Map<String, String> map) {
        WGS84Location location = locations.get(new Random().nextInt(locations.size()));
        map.put("location.name", location.getLabels().get(0));
        map.put("location.desc", location.getComments().get(0));
        map.put("location.alt", String.valueOf(location.getAltitude()));
        map.put("location.lat", String.valueOf(location.getLatitude()));
        map.put("location.long", String.valueOf(location.getLongitude()));
    }

    private void addRandomPropertiesForKey( String key, Map<String, String> map) {
        StringBuilder propertiesString = new StringBuilder();

        Collections.shuffle(properties);
        Iterator<String> iterator = properties.subList(0, new Random().nextInt(properties.size() - 2) + 2).iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            propertiesString.append("\"" + next + "\"");
            if (iterator.hasNext()) {
                propertiesString.append(",");
            }
        }

        map.put(key,propertiesString.toString());
    }

}