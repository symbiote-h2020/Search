package eu.h2020.symbiote.query;

import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the operation for deleting all blank nodes that are orphans.
 *
 * Created by Mael on 26/01/2017.
 */
public class CleanupBlankOrphansRequestGenerator extends AbstractDeleteRequest {

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the resource with specified id,
     * as well as all informations connected to it. To generate the request use {@link #generateRequest()}.
     *
     */
    public CleanupBlankOrphansRequestGenerator() {
        request = UpdateFactory.create();

        String s = "DELETE { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER NOT EXISTS { ?s1 ?p1 ?s } FILTER isBlank(?s) }";
        request.add(s);
    }

    /**
     * Generates the update request, containing delete queries for resource and data linked to the resource.
     *
     * @return Update request which allows deletion of the resource and linked information.
     */
    public UpdateRequest generateRequest() {
        return request;
    }

}
