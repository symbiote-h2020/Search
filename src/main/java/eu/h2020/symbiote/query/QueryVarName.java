package eu.h2020.symbiote.query;

/**
 * Interface containing names of variables for sparql queries.
 *
 * Created by Mael on 26/01/2017.
 */
public interface QueryVarName {

    String RESOURCE_ID = "resId";
    String RESOURCE_NAME = "resName";
    String RESOURCE_DESCRIPTION = "resDescription";
    String PLATFORM_ID = "platformId";
    String PLATFORM_NAME = "platformName";
    String LOCATION_NAME = "locationName";
    String LOCATION_LAT = "locationLat";
    String LOCATION_LONG ="locationLong";
    String LOCATION_ALT ="locationAlt";
    String PROPERTY_NAME = "propName";
    String PROPERTY_IRI = "property";
    String PROPERTY_DESC = "propDesc";
    String TYPE = "type";
    String VALUE = "value";
    String OWNER = "owner";

    //service inputs
    String PARAMETER = "parameter";
    String PARAMETER_NAME = "parameterName";
    String PARAMETER_MANDATORY = "parameterMandatory";
    String PARAMETER_DATATYPE = "parameterDatatype";
    String PARAMETER_DATATYPE_PRED = "dataPred";
    String PARAMETER_DATATYPE_OBJ = "dataObj";

    //Actuator capabilities inputs
    String CAPABILITY = "capability";
    String CAPABILITY_NAME = "capName";
    String CAP_PARAMETER = "capParameter";
    String CAP_PARAMETER_NAME = "capParameterName";
    String CAP_PARAMETER_MANDATORY = "capParameterMandatory";
    String CAP_PARAMETER_DATATYPE = "capParameterDatatype";
    String CAP_PARAMETER_DATATYPE_PRED = "capDataPred";
    String CAP_PARAMETER_DATATYPE_OBJ = "capDataObj";

}
