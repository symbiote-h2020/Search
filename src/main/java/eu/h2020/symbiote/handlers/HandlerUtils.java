package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.ResourceType;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static eu.h2020.symbiote.query.QueryVarName.*;

/**
 * Utility class for translation between event objects and RDF models. Contains also some helpers method used by event
 * consumers and executors.
 *
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
    public static Model generateModelFromPlatform(Platform platform ) {
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
                .addProperty(CIM.id,platform.getId());
        for( String comment: platform.getDescription() ) {
            platformResource.addProperty(CIM.description, comment);
        }

        platformResource.addProperty(CIM.name,platform.getName());


        for( InterworkingService service: platform.getInterworkingServices() ) {
            Resource interworkingServiceResource = model.createResource(generateInterworkingServiceUri(ModelHelper.getPlatformURI(platform.getId()), service.getUrl()))
                    .addProperty(RDF.type, MIM.InterworkingService)
                    .addProperty(MIM.usesInformationModel,model.createResource(ModelHelper.getInformationModelURI(service.getInformationModelId())))
                    .addProperty(MIM.url,service.getUrl());
            platformResource.addProperty(MIM.hasService, interworkingServiceResource);
        }

//        Model serviceModel = generateInterworkingService(platform);
//        model.add(serviceModel);

        model.write(System.out,"TURTLE");
        return model;
    }

//    /**
//     * Generates a model containing RDF statements equivalent to specified resource.
//     *
//     * @param resource Resource to be translated into RDF.
//     * @return Model containing RDF statements.
//     */
//    public static Model generateModelFromResource( Resource resource ) {
//        log.debug("Generating model from resource");
//        Model model = ModelFactory.createDefaultModel();
//
//        List<String> properties = resource.getObservedProperties();
//        org.apache.jena.rdf.model.Resource res = model.createResource();
//        for( String prop: properties ) {
//            res.addProperty(CoreInformationModel.RDFS_LABEL,prop);
//            res.addProperty(CoreInformationModel.RDFS_COMMENT,"");
//        }
//
//        model.createResource(Ontology.getResourceGraphURI(resource.getId()))
//                .addProperty(MetaInformationModel.RDF_TYPE,CoreInformationModel.CIM_RESOURCE)
//                .addProperty(CoreInformationModel.CIM_ID,resource.getId())
//                .addProperty(CoreInformationModel.RDFS_LABEL,resource.getName())
//                .addProperty(CoreInformationModel.RDFS_COMMENT,resource.getDescription()!=null?resource.getDescription():"")
//                .addProperty(CoreInformationModel.CIM_LOCATED_AT,model.createResource(Ontology.getResourceGraphURI(resource.getId())+"/location"))
//                .addProperty(CoreInformationModel.CIM_OBSERVES,res);
//
//        Model locationModel = generateLocation(resource);
//        model.add(locationModel);
//
//        model.write(System.out,"TURTLE");
//        return model;
//    }
//
//    /**
//     * Generates a model containing RDF statements describing resource's location.
//     *
//     * @param resource Resource, for which location model will be created.
//     * @return Model containing location RDF statements.
//     */
//    public static Model generateLocation( Resource resource ) {
//        Model model = ModelFactory.createDefaultModel();
//        model.createResource(Ontology.getResourceGraphURI(resource.getId())+"/location")
//                .addProperty(CoreInformationModel.RDFS_LABEL,resource.getLocation().getName())
//                .addProperty(CoreInformationModel.RDFS_COMMENT, resource.getLocation().getDescription()!=null?resource.getLocation().getDescription():"")
//                .addProperty(CoreInformationModel.GEO_LAT, resource.getLocation().getLatitude().toString())
//                .addProperty(CoreInformationModel.GEO_LONG, resource.getLocation().getLongitude().toString())
//                .addProperty(CoreInformationModel.GEO_ALT, resource.getLocation().getAltitude().toString());
//
//        return model;
//    }
//
//    /**
//     * Generates a model containing RDF statements describing interworking service of the specified platform.
//     *
//     * @param platform Platform, whose interworking Ssrvice will be translated into RDF.
//     * @return Model containing RDF statements.
//     */
//    public static Model generateInterworkingService( Platform platform ) {
//        Model model = ModelFactory.createDefaultModel();
//        model.createResource(generateInterworkingServiceUri(Ontology.getPlatformGraphURI(platform.getId()),platform.getUrl()))
//                .addProperty(MetaInformationModel.RDF_TYPE,MetaInformationModel.MIM_INTERWORKINGSERVICE)
//                .addProperty(MetaInformationModel.MIM_HASURL, platform.getUrl() )
//                .addProperty(MetaInformationModel.MIM_HASINFORMATIONMODEL, model.createResource()
//                        .addProperty(MetaInformationModel.CIM_HASID,platform.getInformationModelId()));
//        return model;
//    }

    /**
     * Generates interworking service uri combining unique platform (graph) URI with service URL of the interwroking service.
     *
     * @param platformUri Unique graph URI of the platform for which interworking service is created.
     * @param serviceUrl URL of the interworking service.
     * @return Graph URI of the interworking service.
     */
    public static String generateInterworkingServiceUri( String platformUri, String serviceUrl ) {
        String cutServiceUrl = "";
        if( serviceUrl.startsWith( "http://" ) ) {
            cutServiceUrl = serviceUrl.substring(7);
        } else if ( serviceUrl.startsWith("https://")) {
            cutServiceUrl = serviceUrl.substring(8);
        }
        return platformUri + "/service/" + cutServiceUrl;
    }

    /**
     *
     *
     * @param request
     * @return
     */
    public static QueryGenerator generateQueryFromSearchRequest( CoreQueryRequest request ) {
        QueryGenerator q = new QueryGenerator();

        if( request.getPlatform_id() != null && !request.getPlatform_id().isEmpty()) {
            q.addPlatformId(request.getPlatform_id());
        }
        if(request.getPlatform_name() != null && !request.getPlatform_name().isEmpty() ) {
            q.addPlatformName(request.getPlatform_name());
        }
        if(request.getName() != null && !request.getName().isEmpty() ) {
            q.addResourceName(request.getName());
        }
        if(request.getId() != null && !request.getId().isEmpty()) {
            q.addResourceId(request.getId());
        }
        if( request.getDescription() != null && !request.getDescription().isEmpty() ) {
            q.addResourceDescription(request.getDescription());
        }
        if( request.getLocation_name() != null && !request.getLocation_name().isEmpty() ) {
            q.addResourceLocationName(request.getLocation_name());
        }
        if( request.getObserved_property() != null && !request.getObserved_property().isEmpty() ) {
            if( request.getObserved_property().size() == 1 ) {
                q.addResourceObservedPropertyName(request.getObserved_property().get(0));
            } else {
                q.addResourceObservedPropertyNames(request.getObserved_property());
            }
        }
        if( request.getResource_type() != null && !request.getResource_type().isEmpty() ) {
            try {
                ResourceType type = ResourceType.getTypeForName(request.getResource_type());
                q.addResourceType(type.getUri());
            } catch( Exception e ) {
                log.warn("Wrong resource type specified: " + request.getResource_type());
            }
        }
        if( request.getLocation_lat() != null && request.getLocation_long() != null && request.getMax_distance() != null) {
            q.addResourceLocationDistance(request.getLocation_lat(),request.getLocation_long(),request.getMax_distance());
        }
        return q;
    }

    public static QueryResponse generateSearchResponseFromResultSet( ResultSet resultSet) {
        Map<String,QueryResourceResult> responses = new HashMap<String,QueryResourceResult>();
        if( !resultSet.hasNext() ) {
            System.out.println( "Could not generate search response from result set, cause resultSet is empty");
        }
        System.out.println( "Found vars: " + resultSet.getResultVars() );
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String resId = solution.get(RESOURCE_ID).toString();
            String resName = solution.get(RESOURCE_NAME).toString();
            String resDescription = solution.get(RESOURCE_DESCRIPTION).toString();
            String platformId = solution.get(PLATFORM_ID).toString();
            String platformName = solution.get(PLATFORM_NAME).toString();
            RDFNode locationNode = solution.get(LOCATION_NAME);
            String locationName = locationNode!=null?locationNode.toString():"";

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
            } catch( NumberFormatException e ) {
                log.error("Number format exception occurred when reading location values: " + e.getMessage(), e);
            }

            RDFNode propertyNode = solution.get(PROPERTY_NAME);
            String propertyName = propertyNode!=null?propertyNode.toString():"";
            String type = solution.get(TYPE).toString();
            //TODO potential change with inference
            if( !type.equals(CIM.Resource.toString()) ) {

                if (!responses.containsKey(resId)) {
                    List<String> properties = new ArrayList<>();
                    properties.add(propertyName);

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
                    responses.put(resId, resource);
                } else {
                    QueryResourceResult existingResource = responses.get(resId);
//                //Do equals
                    if( propertyName != null && !propertyName.isEmpty() ) {
                        if( !existingResource.getObservedProperties().contains(propertyName)) {
                            existingResource.getObservedProperties().add(propertyName);
                        }
                    }
                    if( type != null && !type.isEmpty() ) {
                        if( !existingResource.getResourceType().contains(type) ) {
                            if( isPossibleType(type) ) {
                                existingResource.getResourceType().add(type);
                            }
                        }
                    }
                }
            }

        }

        QueryResponse response = null;
        if( responses!=null ) {
            response = new QueryResponse();
            response.setResources(responses.values().stream().collect(Collectors.toList()));
        };
        return response;
    }

    private static boolean isPossibleType(String type) {
        if( type.equals(CIM.MobileSensor) ||
                type.equals(CIM.StationarySensor) ||
                type.equals(CIM.Actuator) ||
                type.equals(CIM.Service) ) {
            return true;
        }
        return false;
    }


}
