package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.cloud.model.ssp.SspRegInfo;
import eu.h2020.symbiote.core.cci.InfoModelMappingRequest;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.ResourceType;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.model.cim.*;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.OntologyMapping;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.model.mim.SmartSpace;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.INTERNAL;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;

import java.util.*;
import java.util.stream.Collectors;

import static eu.h2020.symbiote.query.QueryVarName.*;

/**
 * Utility class for translation between event objects and RDF models. Contains also some helpers method used by event
 * consumers and executors.
 * <p>
 * Created by Mael on 12/01/2017.
 */
public class HandlerUtils {

    private static final Log log = LogFactory.getLog(HandlerUtils.class);

    private HandlerUtils() {

    }

    /**
     * Generates a model containing RDF statements equivalent to specified platform.
     *
     * @param platform Platform to be translated into RDF.
     * @return Model containing RDF statements.
     */
    public static Model generateModelFromPlatform(Platform platform) {
        log.debug("Generating model from platform");
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("core", "http://www.symbiote-h2020.eu/ontology/core#");
        model.setNsPrefix("meta", "http://www.symbiote-h2020.eu/ontology/meta#");
        model.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        model.setNsPrefix("qu", "http://purl.oclc.org/NET/ssnx/qu/quantity#");

        // construct proper Platform entry
        Resource platformResource = model.createResource(ModelHelper.getPlatformURI(platform.getId()))
                .addProperty(RDF.type, MIM.Platform)
                .addProperty(CIM.id, platform.getId());
        if( platform.getDescription() != null ) {
            for (String comment : platform.getDescription()) {
                platformResource.addProperty(CIM.description, comment);
            }
        }

        platformResource.addProperty(CIM.name, platform.getName());


        for (InterworkingService service : platform.getInterworkingServices()) {
            log.debug("Linking platform's service to infromationModelUri " + ModelHelper.getInformationModelURI(service.getInformationModelId()));
            Resource interworkingServiceResource = model.createResource(generateInterworkingServiceUri(ModelHelper.getPlatformURI(platform.getId()), service.getUrl()))
                    .addProperty(RDF.type, MIM.InterworkingService)
                    .addProperty(MIM.usesInformationModel, model.createResource(ModelHelper.getInformationModelURI(service.getInformationModelId())))
                    .addProperty(MIM.url, service.getUrl());
            platformResource.addProperty(MIM.hasService, interworkingServiceResource);
        }

//        Model serviceModel = generateInterworkingService(platform);
//        model.add(serviceModel);

        model.write(System.out, "TURTLE");
        return model;
    }

    /**
     * Generates interworking service uri combining unique platform (graph) URI with service URL of the interwroking service.
     *
     * @param platformUri Unique graph URI of the platform for which interworking service is created.
     * @param serviceUrl  URL of the interworking service.
     * @return Graph URI of the interworking service.
     */
    public static String generateInterworkingServiceUri(String platformUri, String serviceUrl) {
        String cutServiceUrl = "";
        if (serviceUrl.startsWith("http://")) {
            cutServiceUrl = serviceUrl.substring(7);
        } else if (serviceUrl.startsWith("https://")) {
            cutServiceUrl = serviceUrl.substring(8);
        }
        return platformUri + "/service/" + cutServiceUrl;
    }

    public static String generateInterworkingServiceUriForSdev( String sdevUri ) {
        return sdevUri + "/service/internal";
    }

    public static Model generateModelFromSsp(SmartSpace ssp) {
        log.debug("Generating model from ssp");
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("core", "http://www.symbiote-h2020.eu/ontology/core#");
        model.setNsPrefix("meta", "http://www.symbiote-h2020.eu/ontology/meta#");
        model.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        model.setNsPrefix("qu", "http://purl.oclc.org/NET/ssnx/qu/quantity#");

        // construct proper Platform entry
        Resource platformResource = model.createResource(ModelHelper.getSspURI(ssp.getId()))
                .addProperty(RDF.type, MIM.SmartSpace)
                .addProperty(CIM.id, ssp.getId());
        if( ssp.getDescription() != null ) {
            for (String comment : ssp.getDescription()) {
                platformResource.addProperty(CIM.description, comment);
            }
        }

        platformResource.addProperty(CIM.name, ssp.getName());

        for (InterworkingService service : ssp.getInterworkingServices()) {
            Resource interworkingServiceResource = model.createResource(generateInterworkingServiceUri(ModelHelper.getSspURI(ssp.getId()), service.getUrl()))
                    .addProperty(RDF.type, MIM.InterworkingService)
                    .addProperty(MIM.usesInformationModel, model.createResource(ModelHelper.getInformationModelURI(service.getInformationModelId())))
                    .addProperty(MIM.url, service.getUrl());
            platformResource.addProperty(MIM.hasService, interworkingServiceResource);
        }

//        Model serviceModel = generateInterworkingService(platform);
//        model.add(serviceModel);

        model.write(System.out, "TURTLE");
        return model;
    }

    /**
     * Generates a model containing RDF statements equivalent to specified Smart Device.
     *
     * @param sdev Smart Device to be translated into RDF.
     * @return Model containing RDF statements.
     */
    public static Model generateModelFromSdev(SspRegInfo sdev) {
        log.debug("Generating model from sdev");
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("core", "http://www.symbiote-h2020.eu/ontology/core#");
        model.setNsPrefix("meta", "http://www.symbiote-h2020.eu/ontology/meta#");
        model.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
        model.setNsPrefix("qu", "http://purl.oclc.org/NET/ssnx/qu/quantity#");

        String interworkingServiceUrl = sdev.getPluginURL(); //Not needed
        String sspId = sdev.getPluginId(); // plugin = platform = ssp
        String sspIri = ModelHelper.getSspURI(sspId);
        log.debug("Connecting sdev with ssp: " + sspIri);


        // construct proper Platform entry
        Resource sdevResource = model.createResource(ModelHelper.getSdevURI(sdev.getSymId()))
                .addProperty(RDF.type, MIM.SmartDevice)
                .addProperty(CIM.id, sdev.getSymId())
                .addProperty(CIM.name,sdev.getSspId())
                .addProperty(MIM.isConnectedTo,model.createResource(sspIri) );

        sdevResource.addProperty(MIM.hasService, model.createResource(generateInterworkingServiceUriForSdev(ModelHelper.getSdevURI(sdev.getSymId()))));

        //No name or description in the sdev model
//        for (InterworkingService service : platform.getInterworkingServices()) {
//            Resource interworkingServiceResource = model.createResource(
//                    .addProperty(RDF.type, MIM.InterworkingService)
//                    .addProperty(MIM.usesInformationModel, model.createResource(ModelHelper.getInformationModelURI(service.getInformationModelId())))
//                    .addProperty(MIM.url, service.getUrl());


//        }
//        Model serviceModel = generateInterworkingService(platform);
//        model.add(serviceModel);

        model.write(System.out, "TURTLE");
        return model;
    }

    /**
     * Generates a model containing RDF statements equivalent to information model mappings.
     *
     * @param mapping Mapping to be translated
     * @return Model containing RDF statements.
     */
    public static Model generateModelFromMapping(OntologyMapping mapping) {
        log.debug("Generating model from mapping");
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("core", "http://www.symbiote-h2020.eu/ontology/core#");
        model.setNsPrefix("meta", "http://www.symbiote-h2020.eu/ontology/meta#");

        // construct proper Mapping entry
        Resource mappingResource = model.createResource(ModelHelper.getMappingURI(mapping.getId()))
                .addProperty(RDF.type, MIM.Mapping)
                .addProperty(CIM.id, mapping.getId());
        if( mapping.getName() != null ) {
            mappingResource.addProperty(CIM.name, mapping.getName());
        }

        if( mapping.getDefinition() != null ) {
            mappingResource.addProperty(MIM.hasDefinition,mapping.getDefinition());
        }

        if( mapping.getSourceModelId() != null ) {
            mappingResource.addProperty(MIM.hasSourceModel,ModelHelper.getInformationModelURI(mapping.getSourceModelId()));
        }
        if( mapping.getDestinationModelId() != null ) {
            mappingResource.addProperty(MIM.hasDestinationModel,ModelHelper.getInformationModelURI(mapping.getDestinationModelId()));
        }

//        Model serviceModel = generateInterworkingService(platform);
//        model.add(serviceModel);

        model.write(System.out, "TURTLE");
        return model;
    }

    /**
     * @param request
     * @return
     */
    public static QueryGenerator generateQueryFromSearchRequest(CoreQueryRequest request) {
        QueryGenerator q = new QueryGenerator();

        if (request.getPlatform_id() != null && !request.getPlatform_id().isEmpty()) {
            q.addPlatformId(request.getPlatform_id());
        }
        if (request.getPlatform_name() != null && !request.getPlatform_name().isEmpty()) {
            q.addPlatformName(request.getPlatform_name());
        }
        if (request.getName() != null && !request.getName().isEmpty()) {
            q.addResourceName(request.getName());
        }
        if (request.getId() != null && !request.getId().isEmpty()) {
            q.addResourceId(request.getId());
        }
        if (request.getDescription() != null && !request.getDescription().isEmpty()) {
            q.addResourceDescription(request.getDescription());
        }
        if (request.getLocation_name() != null && !request.getLocation_name().isEmpty()) {
            q.addResourceLocationName(request.getLocation_name());
        }
        if (request.getObserved_property() != null && !request.getObserved_property().isEmpty()) {
            if (request.getObserved_property().size() == 1) {
                q.addResourceObservedPropertyName(request.getObserved_property().get(0));
            } else {
                q.addResourceObservedPropertyNames(request.getObserved_property());
            }
        }
        if (request.getObserved_property_iri() != null && !request.getObserved_property_iri().isEmpty()) {
            if (request.getObserved_property_iri().size() == 1) {
                q.addResourceObservedPropertyIri(request.getObserved_property_iri().get(0));
            } else {
                q.addResourceObservedPropertyIris(request.getObserved_property_iri());
            }
        }
        if (request.getResource_type() != null && !request.getResource_type().isEmpty()) {
            try {
                if( request.getResource_type().startsWith("http")) {
                    q.addResourceType(request.getResource_type());
                } else {
                    ResourceType type = ResourceType.getTypeForName(request.getResource_type());
                    q.addResourceType(type.getUri());
                }
            } catch (Exception e) {
                log.warn("Wrong resource type specified: " + request.getResource_type());
            }
        }
        if (request.getLocation_lat() != null && request.getLocation_long() != null && request.getMax_distance() != null) {
            q.addResourceLocationDistance(request.getLocation_lat(), request.getLocation_long(), request.getMax_distance());
        }
        if(StringUtils.isNotEmpty(request.getOwner())) {
            q.addSdevOwner(request.getOwner());
        }

        return q;
    }

    public static QueryResponse generateSearchResponseFromResultSet(ResultSet resultSet) {
        Map<String, QueryResourceResult> responses = new HashMap<String, QueryResourceResult>();
        Map<String, Map<String, ParameterInfo>> serviceParams = new HashMap<>();
        Map<String, Map<String, CapabilitiesInfo>> capabilitiesInfos = new HashMap<>();

        if (!resultSet.hasNext()) {
            log.debug("Could not generate search response from result set, cause resultSet is empty");
        }
//        System.out.println("Found vars: " + resultSet.getResultVars());
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
//            printSolution(solution);

            String owner = solution.get(OWNER)!=null?solution.get(OWNER).toString():"";
            String resId = solution.get(RESOURCE_ID).toString();
            String resName = solution.get(RESOURCE_NAME).toString();
            RDFNode resDescNode = solution.get(RESOURCE_DESCRIPTION);
            String resDescription = resDescNode!=null?resDescNode.toString(): "";
            String platformId = solution.get(PLATFORM_ID).toString();
            String platformName = solution.get(PLATFORM_NAME).toString();
            RDFNode locationNode = solution.get(LOCATION_NAME);
            String locationName = locationNode != null ? locationNode.toString() : "";

            RDFNode locationLatNode = solution.get(LOCATION_LAT);
            RDFNode locationLongNode = solution.get(LOCATION_LONG);
            RDFNode locationAltNode = solution.get(LOCATION_ALT);
            Double latVal = null;
            Double longVal = null;
            Double altVal = null;
            try {
                latVal = locationLatNode != null ? Double.valueOf(locationLatNode.toString()) : null;
                longVal = locationLongNode != null ? Double.valueOf(locationLongNode.toString()) : null;
                altVal = locationAltNode != null ? Double.valueOf(locationAltNode.toString()) : null;
            } catch (NumberFormatException e) {
                log.error("Number format exception occurred when reading location values: " + e.getMessage(), e);
            }

            RDFNode propertyNameNode = solution.get(PROPERTY_NAME);
            String propertyName = propertyNameNode != null ? propertyNameNode.toString() : "";

            RDFNode propertyIriNode = solution.get(PROPERTY_IRI);
            String propertyIri = propertyIriNode != null ? propertyIriNode.toString() : "";

            RDFNode propertyDescNode = solution.get(PROPERTY_DESC);
            String propertyDesc = propertyDescNode != null ? propertyDescNode.toString() : "";

            String type = solution.get(TYPE).toString();

            //Services
            RDFNode parameter = solution.get(PARAMETER);
            String parameterName = null;
            String parameterMandatory = null;
            String parameterDatatype = null;
            String paramDataPred = null;
            String paramDataObj = null;

            if (parameter != null) {
                parameterName = readValueFromSolution(solution, PARAMETER_NAME);
                parameterMandatory = readValueFromSolution(solution, PARAMETER_MANDATORY);
                parameterDatatype = readValueFromSolution(solution, PARAMETER_DATATYPE);
                paramDataPred = readValueFromSolution(solution, PARAMETER_DATATYPE_PRED);
                paramDataObj = readValueFromSolution(solution, PARAMETER_DATATYPE_OBJ);
            }

            //Capabilities of Actuators
            RDFNode capability = solution.get(CAPABILITY);
            String capabilityName = null;
            RDFNode capParameter = solution.get(CAP_PARAMETER);
            String capParameterName = null;
            String capParameterMandatory = null;
            String capParameterDatatype = null;
            String capDataPred = null;
            String capDataObj = null;

            if (capability != null) {
//                System.out.println("Got capability " + capability.asResource().getId());
                capabilityName = readValueFromSolution(solution, CAPABILITY_NAME);
                if( capabilityName == null ) { // In case capability has been added without name, use anonymousId
                    capabilityName = capability.asResource().getId().toString();
                }

                if (capParameter != null) {
                    capParameterName = readValueFromSolution(solution, CAP_PARAMETER_NAME);
                    capParameterMandatory = readValueFromSolution(solution, CAP_PARAMETER_MANDATORY);
                    capParameterDatatype = readValueFromSolution(solution, CAP_PARAMETER_DATATYPE);
                    capDataPred = readValueFromSolution(solution, CAP_PARAMETER_DATATYPE_PRED);
                    capDataObj = readValueFromSolution(solution, CAP_PARAMETER_DATATYPE_OBJ);
                }
            }


            //TODO potential change with inference
            if (!type.equals(CIM.Resource.toString())) {

                if (!responses.containsKey(resId)) {
                    List<Property> properties = new ArrayList<>();
                    if (!propertyName.isEmpty() && !propertyIri.isEmpty()) {
                        properties.add(new Property(propertyName, propertyIri, Arrays.asList(propertyDesc)));
                    }

                    List<String> types = new ArrayList<>();
                    types.add(type);

                    QueryResourceResult resource = new QueryResourceResult();
                    resource.setId(resId);
                    resource.setName(resName);
                    resource.setDescription(resDescription);
                    resource.setPlatformId(platformId);
                    resource.setPlatformName(platformName);
                    resource.setLocationName(locationName);
                    resource.setLocationLatitude(latVal);
                    resource.setLocationLongitude(longVal);
                    resource.setLocationAltitude(altVal);
                    resource.setObservedProperties(properties);
                    resource.setResourceType(types);
                    resource.setOwner(owner);
                    responses.put(resId, resource);

                    serviceParams.put(resId, new HashMap<>());
                    capabilitiesInfos.put(resId, new HashMap<>());
                } else {
                    QueryResourceResult existingResource = responses.get(resId);
//                //Do equals
                    if (propertyName != null && !propertyName.isEmpty()) {
                        Property newProp = new Property(propertyName, propertyIri, Arrays.asList(propertyDesc));
                        if (!existingResource.getObservedProperties().contains(newProp)) {
                            existingResource.getObservedProperties().add(newProp);
                        }
                    }
                    if (type != null && !type.isEmpty()) {
                        if (!existingResource.getResourceType().contains(type)) {
                            if (isPossibleType(type)) {
                                existingResource.getResourceType().add(type);
                            }
                        }
                    }

                }


                if (parameter != null && parameterName != null) {
                    updateParameterMapWithNewEntry(serviceParams.get(resId), parameterName, parameterMandatory,
                            parameterDatatype, paramDataPred, paramDataObj);
                }

                if (capability != null && capabilityName != null ) {
                    if (!capabilitiesInfos.get(resId).containsKey(capabilityName)) {
                        CapabilitiesInfo capabilitiesInfo = new CapabilitiesInfo(capabilityName, new HashMap<>());
                        capabilitiesInfos.get(resId).put(capabilityName, capabilitiesInfo);
                    }
                    CapabilitiesInfo capabilitiesInfo = capabilitiesInfos.get(resId).get(capabilityName);
                    updateParameterMapWithNewEntry(capabilitiesInfo.getParameters(), capParameterName,
                            capParameterMandatory, capParameterDatatype, capDataPred, capDataObj);
                }

            }

        }

//        System.out.println("");


        //Need to go through the params and cabalities lists and construct objects for query response
//        MapUtils.verbosePrint(System.out,"params map",serviceParams);
        serviceParams.keySet().stream().filter(resId -> serviceParams.get(resId) != null && !serviceParams.get(resId).isEmpty()).forEach(resId -> {
            List<Parameter> parameters = serviceParams.get(resId).values().stream().map(parameterInfo -> createModelParameter(parameterInfo)).collect(Collectors.toList());
            if (responses.get(resId) != null) {
                responses.get(resId).setInputParameters(parameters);
            }
        });


//        MapUtils.verbosePrint(System.out,"capabilities map",capabilitiesInfos);
        capabilitiesInfos.keySet().stream().filter(resId -> capabilitiesInfos.get(resId) != null && !capabilitiesInfos.get(resId).isEmpty()).forEach(resId -> {
            List<Capability> capabilities = capabilitiesInfos.get(resId).values().stream().map(capabilitiesInfo -> createModelCapability(capabilitiesInfo)).collect(Collectors.toList());
            if (responses.get(resId) != null) {
                responses.get(resId).setCapabilities(capabilities);
            }
        });

        QueryResponse response = new QueryResponse();
        response.setBody(responses.values().stream().collect(Collectors.toList()));
//        MapUtils.verbosePrint(System.out,"responses",responses);
//        responses.values().stream().forEach(rsp -> printParamsAndCapabilities(rsp));

        return response;
    }

    public static boolean isDistanceQuery(CoreQueryRequest request) {
        return request != null && request.getLocation_lat() != null && request.getLocation_long() != null && request.getMax_distance() != null;
    }

    private static String readValueFromSolution(QuerySolution solution, String paramName) {
        RDFNode rdfNode = solution.get(paramName);
        return rdfNode != null ? rdfNode.toString() : null;
    }

    private static boolean isPossibleType(String type) {
        if (type.equals(CIM.MobileSensor.toString()) ||
                type.equals(CIM.StationarySensor.toString()) ||
                type.equals(CIM.Actuator.toString()) ||
                type.equals(CIM.Service.toString())) {
            return true;
        }
        return false;
    }

    private static void updateParameterMapWithNewEntry(Map<String, ParameterInfo> parameterMap, String parameterName, String parameterMandatory,
                                                       String parameterDatatype, String paramDataPred, String paramDataObj) {
        if (!parameterMap.containsKey(parameterName)) {
            ParameterInfo parameterInfo = new ParameterInfo(parameterName, parameterMandatory, new ArrayList<>());
            parameterMap.put(parameterName, parameterInfo);
        }
        DatatypeInfo datatypeInfo = new DatatypeInfo(parameterDatatype, paramDataPred, paramDataObj);
        parameterMap.get(parameterName).getDatatypes().add(datatypeInfo);
    }


    public static Parameter createModelParameter(ParameterInfo paramInfo) {
        Parameter param = new Parameter();
        if (paramInfo != null) {
            param.setName(paramInfo.getParameterName());
            param.setMandatory(Boolean.valueOf(paramInfo.getParameterMandatory()));
            List<DatatypeInfo> datatypes = paramInfo.getDatatypes();
            List<DatatypeInfo> allTypes = getAllTripletsWithPredicate(datatypes, RDF.type.toString());
            //Get only CIM types
            List<DatatypeInfo> type = allTypes.stream().filter(dtype -> dtype.getDataObj().equals(CIM.PrimitiveDatatype.toString())
                    || dtype.getDataObj().equals(CIM.ComplexDatatype.toString())).collect(Collectors.toList());
            if (type != null && type.size() == 1) {
                String typeIRI = type.get(0).getDataObj();
                Datatype datatype = null;
                if (typeIRI.equals(RDFS.Datatype.toString()) || typeIRI.equals(CIM.PrimitiveDatatype.toString())) {
                    datatype = new PrimitiveDatatype();
                    ((PrimitiveDatatype) datatype).setBaseDatatype(type.get(0).getDatatype());
                } else if (typeIRI.equals(CIM.ComplexDatatype.toString())) {
                    datatype = new ComplexDatatype();
                    List<DatatypeInfo> basedOnClass = getAllTripletsWithPredicate(datatypes, CIM.basedOnClass.toString());
                    if (basedOnClass != null && basedOnClass.size() == 1) {
                        ((ComplexDatatype) datatype).setBasedOnClass(basedOnClass.get(0).getDataObj());
                    } else {
                        ((ComplexDatatype) datatype).setBasedOnClass("");
                    }
//                    ((ComplexDatatype)datatype).setBasedOnClass();
//                    ((ComplexDatatype)datatype).setArray();
                    //TODO dataproperties for later release if needed
//                    complexDatatype.setDataProperties();
                } else {
                    log.error("Could not find type for " + typeIRI);
                }
                if (datatype != null) {
                    List<DatatypeInfo> isArray = getAllTripletsWithPredicate(datatypes, CIM.isArray.toString());
                    if (isArray != null && isArray.size() == 1) {
                        datatype.setArray(Boolean.valueOf(isArray.get(0).getDataObj()));
                    } else {
                        datatype.setArray(false);
                    }
                    param.setDatatype(datatype);
                }
            } else {
//                log.warn("Error parsing parameter info into model: " + type == null ? "type is null" : "type has size " + type.size());
            }

        }
//        if( param.getDatatype() == null ) {
//            System.out.println("Create param - datatype null");
//        } else {
//            System.out.println("Create param - datatype not null - " + param.getDatatype());
//        }

        return param;
    }

    public static Capability createModelCapability(CapabilitiesInfo capabilityInfo) {
        Capability cap = new Capability();
        cap.setName(capabilityInfo.getCapabilityName());
        cap.setParameters(capabilityInfo.getParameters().values().stream().map(parameterInfo -> createModelParameter(parameterInfo)).collect(Collectors.toList()));
        //TODO affects
        return cap;
    }

    private static List<DatatypeInfo> getAllTripletsWithPredicate(List<DatatypeInfo> dInfos, String predicate) {
        return dInfos.stream().filter(dInfo -> dInfo.getDataPred().equals(predicate)).collect(Collectors.toList());
    }

}
