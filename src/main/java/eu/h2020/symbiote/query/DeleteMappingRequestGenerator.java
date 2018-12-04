package eu.h2020.symbiote.query;

import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the delete update operation.
 *
 * Created by Mael on 26/01/2017.
 */
public class DeleteMappingRequestGenerator extends AbstractDeleteRequest {

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the platform with specified id
     * and connected statements.
     * To generate the request use {@link #generateRequest()}.
     *
     * @param mappingId Id of the resource to be deleted.
     */
    public DeleteMappingRequestGenerator(String mappingId ) {
        request = UpdateFactory.create();
        request.add(generateMappingDelete(mappingId));
    }

    private String generateMappingDelete( String mappingId ) {
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?s ?p ?o } WHERE {\n");
        q.append("\t?s ?p ?o ;\n");
        q.append("\t\tcim:id \""+mappingId+"\" .\n");
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
