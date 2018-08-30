package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceType;
import eu.h2020.symbiote.core.internal.RDFFormat;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * Created by Szymon Mueller on 21/08/2018.
 */
public class JsonLDTemplates {

    public static CoreResource generateActuatorFromTemplate(String sensorName, String sensorId, String platformId, String serviceUrl) {
        CoreResource res = new CoreResource();
        res.setDescription(Arrays.asList(""));
        res.setName(sensorName);
        res.setId(sensorId);
        res.setInterworkingServiceURL(serviceUrl);
        res.setType(CoreResourceType.ACTUATOR);
        res.setRdf(getActuatorTemplate(sensorName,sensorId,platformId,serviceUrl));
        res.setRdfFormat(RDFFormat.JSONLD);
        return res;
    }

    public static CoreResource generateMobileSensorFromTemplate(String sensorName, String sensorId, String platformId, String serviceUrl) {
        CoreResource res = new CoreResource();
        res.setDescription(Arrays.asList(""));
        res.setName(sensorName);
        res.setId(sensorId);
        res.setInterworkingServiceURL(serviceUrl);
        res.setType(CoreResourceType.MOBILE_SENSOR);
        res.setRdf(getMobileSensorTemplate(sensorName,sensorId,platformId,serviceUrl));
        res.setRdfFormat(RDFFormat.JSONLD);
        return res;
    }

    public static CoreResource generateStationarySensorFromTemplate(String sensorName, String sensorId, String platformId, String serviceUrl) {
        CoreResource res = new CoreResource();
        res.setDescription(Arrays.asList(""));
        res.setName(sensorName);
        res.setId(sensorId);
        res.setInterworkingServiceURL(serviceUrl);
        res.setType(CoreResourceType.STATIONARY_SENSOR);
        res.setRdf(getStationarySensorTemplate(sensorName,sensorId,platformId,serviceUrl));
        res.setRdfFormat(RDFFormat.JSONLD);
        return res;
    }

    public static String getActuatorTemplate(String sensorName, String sensorId, String platformId, String platformUrl) {
        String locationId = UUID.randomUUID().toString();
        String s = "{\n" +
                "  \"@graph\" : [ {\n" +
                "    \"@id\" : \"_:b0\",\n" +
                "    \"@type\" : \"core:Capability\",\n" +
                "    \"name\" : \"cap1\",\n" +
                "    \"hasEffect\" : \"_:b1\",\n" +
                "    \"hasParameter\" : \"_:b2\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:b1\",\n" +
                "    \"@type\" : \"core:Effect\",\n" +
                "    \"hasFeatureOfInterest\" : \"_:b3\",\n" +
                "    \"hasProperty\" : [ \"qu:humidity\", \"qu:temperature\" ]\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:b2\",\n" +
                "    \"@type\" : \"core:Parameter\",\n" +
                "    \"hasDatatype\" : \"xsd:string\",\n" +
                "    \"hasRestriction\" : \"_:b4\",\n" +
                "    \"mandatory\" : \"true\",\n" +
                "    \"name\" : \"inputParam1\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:b3\",\n" +
                "    \"@type\" : \"core:FeatureOfInterest\",\n" +
                "    \"hasProperty\" : \"qu:temperature\",\n" +
                "    \"description\" : \"This is room 1\",\n" +
                "    \"name\" : \"Room1\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"_:b4\",\n" +
                "    \"@type\" : \"core:RangeRestriction\",\n" +
                "    \"max\" : \"10.0\",\n" +
                "    \"min\" : \"2.0\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"http://www.symbiote-h2020.eu/ontology/internal/platforms/"+platformId+"/location/"+locationId+"\",\n" +
                "    \"@type\" : \"core:WGS84Location\",\n" +
                "    \"description\" : \"This is paris\",\n" +
                "    \"name\" : \"Paris\",\n" +
                "    \"alt\" : \"15.0\",\n" +
                "    \"lat\" : \"48.864716\",\n" +
                "    \"long\" : \"2.349014\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"http://www.symbiote-h2020.eu/ontology/internal/resources/"+sensorId+"\",\n" +
                "    \"@type\" : \"core:Actuator\",\n" +
                "    \"hasCapability\" : \"_:b0\",\n" +
                "    \"id\" : \""+sensorId+"\",\n" +
                "    \"locatedAt\" : \"http://www.symbiote-h2020.eu/ontology/internal/platforms/"+platformId+"/location/"+locationId+"\",\n" +
                "    \"description\" : \"This is actuator 1\",\n" +
                "    \"name\" : \""+sensorName+"\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"xsd:string\",\n" +
                "    \"@type\" : \"rdfs:Datatype\"\n" +
                "  } ],\n" +
                "  \"@context\" : {\n" +
                "    \"hasCapability\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasCapability\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"locatedAt\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#locatedAt\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"description\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#description\"\n" +
                "    },\n" +
                "    \"name\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#name\"\n" +
                "    },\n" +
                "    \"id\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#id\"\n" +
                "    },\n" +
                "    \"hasEffect\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasEffect\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"hasParameter\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasParameter\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"alt\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#alt\"\n" +
                "    },\n" +
                "    \"long\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#long\"\n" +
                "    },\n" +
                "    \"lat\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#lat\"\n" +
                "    },\n" +
                "    \"hasProperty\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasProperty\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"max\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#max\"\n" +
                "    },\n" +
                "    \"min\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#min\"\n" +
                "    },\n" +
                "    \"hasFeatureOfInterest\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasFeatureOfInterest\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"hasRestriction\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasRestriction\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"mandatory\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#mandatory\"\n" +
                "    },\n" +
                "    \"name\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#name\"\n" +
                "    },\n" +
                "    \"hasDatatype\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasDatatype\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"geo\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#\",\n" +
                "    \"core\" : \"http://www.symbiote-h2020.eu/ontology/core#\",\n" +
                "    \"qu\" : \"http://purl.oclc.org/NET/ssnx/qu/quantity#\",\n" +
                "    \"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n" +
                "    \"owl\" : \"http://www.w3.org/2002/07/owl#\",\n" +
                "    \"meta\" : \"http://www.symbiote-h2020.eu/ontology/meta#\",\n" +
                "    \"xsd\" : \"http://www.w3.org/2001/XMLSchema#\",\n" +
                "    \"rdfs\" : \"http://www.w3.org/2000/01/rdf-schema#\"\n" +
                "  }\n" +
                "}";
        return s;
    }

    public static String getMobileSensorTemplate(String sensorName, String sensorId, String platformId, String platformUrl) {
        String locationId = UUID.randomUUID().toString();
        String s = "{\n" +
                "  \"@graph\" : [ {\n" +
                "    \"@id\" : \"http://www.symbiote-h2020.eu/ontology/internal/platforms/"+platformId+"/location/"+locationId+"\",\n" +
                "    \"@type\" : \"core:WGS84Location\",\n" +
                "    \"description\" : \"This is paris\",\n" +
                "    \"name\" : \"Paris\",\n" +
                "    \"alt\" : \"15.0\",\n" +
                "    \"lat\" : \"48.864716\",\n" +
                "    \"long\" : \"2.349014\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"http://www.symbiote-h2020.eu/ontology/internal/resources/"+sensorId+"\",\n" +
                "    \"@type\" : \"core:MobileSensor\",\n" +
                "    \"id\" : \""+sensorId+"\",\n" +
                "    \"locatedAt\" : \"http://www.symbiote-h2020.eu/ontology/internal/platforms/"+platformId+"/location/"+locationId+"\",\n" +
                "    \"observesProperty\" : \"qu:temperature\",\n" +
                "    \"description\" : \"This is mobile 1\",\n" +
                "    \"name\" : \""+sensorName+"\"\n" +
                "  } ],\n" +
                "  \"@context\" : {\n" +
                "    \"alt\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#alt\"\n" +
                "    },\n" +
                "    \"long\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#long\"\n" +
                "    },\n" +
                "    \"lat\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#lat\"\n" +
                "    },\n" +
                "    \"description\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#description\"\n" +
                "    },\n" +
                "    \"name\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#name\"\n" +
                "    },\n" +
                "    \"locatedAt\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#locatedAt\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"observesProperty\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#observesProperty\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"id\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#id\"\n" +
                "    },\n" +
                "    \"geo\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#\",\n" +
                "    \"core\" : \"http://www.symbiote-h2020.eu/ontology/core#\",\n" +
                "    \"qu\" : \"http://purl.oclc.org/NET/ssnx/qu/quantity#\",\n" +
                "    \"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n" +
                "    \"owl\" : \"http://www.w3.org/2002/07/owl#\",\n" +
                "    \"meta\" : \"http://www.symbiote-h2020.eu/ontology/meta#\",\n" +
                "    \"xsd\" : \"http://www.w3.org/2001/XMLSchema#\",\n" +
                "    \"rdfs\" : \"http://www.w3.org/2000/01/rdf-schema#\"\n" +
                "  }\n" +
                "}";
        return s;
    }

    public static String getStationarySensorTemplate(String sensorName, String sensorId, String platformId, String platformUrl) {
        String locationId = UUID.randomUUID().toString();
        String s= "{\n" +
                "  \"@graph\" : [ {\n" +
                "    \"@id\" : \"_:b0\",\n" +
                "    \"@type\" : \"core:FeatureOfInterest\",\n" +
                "    \"hasProperty\" : \"qu:temperature\",\n" +
                "    \"description\" : \"This is room 1\",\n" +
                "    \"name\" : \"Room1\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"http://www.symbiote-h2020.eu/ontology/internal/platforms/"+platformId+"/location/"+locationId+"\",\n" +
                "    \"@type\" : \"core:WGS84Location\",\n" +
                "    \"description\" : \"This is paris\",\n" +
                "    \"name\" : \"Paris\",\n" +
                "    \"alt\" : \"15.0\",\n" +
                "    \"lat\" : \"48.864716\",\n" +
                "    \"long\" : \"2.349014\"\n" +
                "  }, {\n" +
                "    \"@id\" : \"http://www.symbiote-h2020.eu/ontology/internal/resources/"+sensorId+"\",\n" +
                "    \"@type\" : \"core:StationarySensor\",\n" +
                "    \"hasFeatureOfInterest\" : \"_:b0\",\n" +
                "    \"id\" : \""+sensorId+"\",\n" +
                "    \"locatedAt\" : \"http://www.symbiote-h2020.eu/ontology/internal/platforms/"+platformId+"/location/"+locationId+"\",\n" +
                "    \"observesProperty\" : [ \"qu:humidity\", \"qu:temperature\" ],\n" +
                "    \"description\" : \"This is stationary 1\",\n" +
                "    \"name\" : \""+sensorName+"\"\n" +
                "  } ],\n" +
                "  \"@context\" : {\n" +
                "    \"hasProperty\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasProperty\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"description\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#description\"\n" +
                "    },\n" +
                "    \"name\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#name\"\n" +
                "    },\n" +
                "    \"hasFeatureOfInterest\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#hasFeatureOfInterest\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"locatedAt\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#locatedAt\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"observesProperty\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#observesProperty\",\n" +
                "      \"@type\" : \"@id\"\n" +
                "    },\n" +
                "    \"id\" : {\n" +
                "      \"@id\" : \"http://www.symbiote-h2020.eu/ontology/core#id\"\n" +
                "    },\n" +
                "    \"alt\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#alt\"\n" +
                "    },\n" +
                "    \"long\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#long\"\n" +
                "    },\n" +
                "    \"lat\" : {\n" +
                "      \"@id\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#lat\"\n" +
                "    },\n" +
                "    \"geo\" : \"http://www.w3.org/2003/01/geo/wgs84_pos#\",\n" +
                "    \"core\" : \"http://www.symbiote-h2020.eu/ontology/core#\",\n" +
                "    \"qu\" : \"http://purl.oclc.org/NET/ssnx/qu/quantity#\",\n" +
                "    \"rdf\" : \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\",\n" +
                "    \"owl\" : \"http://www.w3.org/2002/07/owl#\",\n" +
                "    \"meta\" : \"http://www.symbiote-h2020.eu/ontology/meta#\",\n" +
                "    \"xsd\" : \"http://www.w3.org/2001/XMLSchema#\",\n" +
                "    \"rdfs\" : \"http://www.w3.org/2000/01/rdf-schema#\"\n" +
                "  }\n" +
                "}";
        return s;
    }

}
