package eu.h2020.symbiote.query;

import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the delete update operation.
 *
 * Created by Mael on 26/01/2017.
 */
public class DeletePlatformRequestGenerator extends AbstractDeleteRequest {

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the platform with specified id
     * and connected statements.
     * To generate the request use {@link #generateRequest()}.
     *
     * @param platformId Id of the resource to be deleted.
     */
    public DeletePlatformRequestGenerator(String platformId ) {
        request = UpdateFactory.create();
        request.add(generateInformationServiceDelete(platformId));
        request.add(generatePlatformDelete(platformId));
    }

    private String generatePlatformDelete( String platformId ) {
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?platform ?p ?o } WHERE {\n");
        q.append("\t?platform ?p ?o ;\n");
        q.append("\t\tcim:id \""+platformId+"\" .\n");
        q.append("}");
        return q.toString();
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
