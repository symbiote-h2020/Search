package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.ontology.model.MetaInformationModel;
import eu.h2020.symbiote.ontology.model.Ontology;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * Created by Mael on 12/01/2017.
 */
public class HandlerUtils {

    public static Model generateModelFromPlatform(Platform platform ) {
        // create an empty Model
        Model model = ModelFactory.createDefaultModel();

        // construct proper Platform entry
        Resource platformReosurce
                = model.createResource(Ontology.getPlatformGraphURI(platform.getPlatformId()))
                .addProperty(MetaInformationModel.RDF_TYPE,MetaInformationModel.OWL_ONTOLOGY)
                .addProperty(MetaInformationModel.MIM_HASDESCRIPTION, platform.getDescription())
                .addProperty(MetaInformationModel.MIM_HASNAME, platform.getName())
                .addProperty(MetaInformationModel.MIM_HASSERVICE,
                        model.createResource()
                                .addProperty(MetaInformationModel.MIM_HASURL, platform.getUrl() )
                                .addProperty(MetaInformationModel.MIM_HASINFORMATIONMODEL, model.createResource()
                                        .addProperty(MetaInformationModel.MIM_HASID,platform.getInformationModelId())));

        model.write(System.out,"TURTLE");
        return model;
    }

}
