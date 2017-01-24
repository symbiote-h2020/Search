package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.ontology.model.CoreInformationModel;
import eu.h2020.symbiote.ontology.model.MetaInformationModel;
import eu.h2020.symbiote.ontology.model.Ontology;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import java.util.List;

/**
 * Utility class for translation between event objects and RDF models.
 *
 * Created by Mael on 12/01/2017.
 */
public class HandlerUtils {

    private static final Log log = LogFactory.getLog(HandlerUtils.class);

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

        // construct proper Platform entry
                model.createResource(Ontology.getPlatformGraphURI(platform.getPlatformId()))
                .addProperty(MetaInformationModel.RDF_TYPE,MetaInformationModel.OWL_ONTOLOGY)
                .addProperty(MetaInformationModel.CIM_HASID,platform.getPlatformId())
                .addProperty(MetaInformationModel.MIM_HASDESCRIPTION, platform.getDescription())
                .addProperty(MetaInformationModel.MIM_HASNAME, platform.getName())
                .addProperty(MetaInformationModel.MIM_HASSERVICE, generateInterworkingServiceUri(Ontology.getPlatformGraphURI(platform.getPlatformId()),platform.getUrl()));
        // Write
        model.write(System.out,"TURTLE");

        Model serviceModel = generateInterworkingService(platform);
        model.add(serviceModel);
        return model;
    }

    /**
     * Generates a model containing RDF statements equivalent to specified resource.
     *
     * @param resource Resource to be translated into RDF.
     * @return Model containing RDF statements.
     */
    public static Model generateModelFromResource( Resource resource ) {
        log.debug("Generating model from resource");
        Model model = ModelFactory.createDefaultModel();

        List<String> properties = resource.getObservedProperties();
        org.apache.jena.rdf.model.Resource res = model.createResource();
        for( String prop: properties ) {
            res.addProperty(CoreInformationModel.RDFS_LABEL,prop);
        }

        model.createResource(Ontology.getResourceGraphURI(resource.getId()))
                .addProperty(MetaInformationModel.RDF_TYPE,CoreInformationModel.CIM_SENSOR)
                .addProperty(CoreInformationModel.CIM_ID,resource.getId())
                .addProperty(CoreInformationModel.RDFS_LABEL,resource.getName())
                .addProperty(CoreInformationModel.RDFS_COMMENT,resource.getDescription())
                .addProperty(CoreInformationModel.CIM_LOCATED_AT,model.createResource()
                        .addProperty(CoreInformationModel.RDFS_LABEL,resource.getLocation().getName())
                        .addProperty(CoreInformationModel.RDFS_COMMENT, resource.getLocation().getDescription())
                        .addProperty(CoreInformationModel.GEO_LAT, resource.getLocation().getLatitude().toString())
                        .addProperty(CoreInformationModel.GEO_LONG, resource.getLocation().getLongitude().toString())
                        .addProperty(CoreInformationModel.GEO_ALT, resource.getLocation().getAltitude().toString()))
                .addProperty(CoreInformationModel.CIM_OBSERVES,res);

        return model;
    }

    /**
     * Generates a model containing RDF statements describing interworking service of the specified platform.
     *
     * @param platform Platform, whose interworking Ssrvice will be translated into RDF.
     * @return Model containing RDF statements.
     */
    public static Model generateInterworkingService( Platform platform ) {
        Model model = ModelFactory.createDefaultModel();
        model.createResource(generateInterworkingServiceUri(Ontology.getPlatformGraphURI(platform.getPlatformId()),platform.getUrl()))
                .addProperty(MetaInformationModel.RDF_TYPE,MetaInformationModel.MIM_INTERWORKINGSERVICE)
                .addProperty(MetaInformationModel.MIM_HASURL, platform.getUrl() )
                .addProperty(MetaInformationModel.MIM_HASINFORMATIONMODEL, model.createResource()
                        .addProperty(MetaInformationModel.CIM_HASID,platform.getInformationModelId()));
        return model;
    }

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

}
