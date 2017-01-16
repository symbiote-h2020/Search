package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.ontology.model.MetaInformationModel;
import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

/**
 * Implementation of the handler for the platform related events.
 *
 * Created by Mael on 11/01/2017.
 */
public class PlatformHandler implements IPlatformEvents{


    private final SearchStorage storage;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param storage Storage on which the events should be executed.
     */
    public PlatformHandler( SearchStorage storage ) {
        this.storage = storage;
    }

    @Override
    public boolean registerPlatform(Platform platform) {

        Model model = HandlerUtils.generateModelFromPlatform(platform);
        storage.registerPlatform(platform.getPlatformId(), model, platform.getInformationModelId() );

        return true;
    }



    @Override
    public boolean updatePlatform(Platform platform) {
        return false;
    }

    @Override
    public boolean deletePlatform(Platform platform) {
        return false;
    }

    @Override
    public boolean deletePlatform(String platformId) {
        return false;
    }
}
