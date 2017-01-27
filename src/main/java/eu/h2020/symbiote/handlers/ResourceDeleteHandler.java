package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.DeleteRequestGenerator;
import org.apache.jena.update.UpdateRequest;

/**
 * Implementation of resource delete event handler.
 *
 * Created by Mael on 26/01/2017.
 */
public class ResourceDeleteHandler implements IResourceDeleteHandler {

    private TripleStore triplestore;

    public ResourceDeleteHandler(TripleStore triplestore) {
        this.triplestore = triplestore;
    }

    @Override
    public void deleteResource(String resourceId) {
        UpdateRequest updateRequest = new DeleteRequestGenerator(resourceId).generateRequest();
        triplestore.executeUpdate(updateRequest);
    }
}
