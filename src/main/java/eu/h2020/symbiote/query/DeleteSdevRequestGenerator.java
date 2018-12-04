package eu.h2020.symbiote.query;

import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the delete update operation for sdev.
 *
 * Created by Mael on 28/05/2018.
 */
public class DeleteSdevRequestGenerator extends AbstractDeleteRequest {

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the sdev with specified id
     * and connected statements.
     * To generate the request use {@link #generateRequest()}.
     *
     * @param sdevId Id of the sdev to be deleted.
     */
    public DeleteSdevRequestGenerator(String sdevId ) {
        request = UpdateFactory.create();
//        request.add(generateInformationServiceDelete(sspId));
        request.add(generateSdevDelete(sdevId));
    }

    private String generateSdevDelete( String sdevId ) {
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?sdev ?p ?o } WHERE {\n");
        q.append("\t?sdev ?p ?o ;\n");
        q.append("\t\tcim:id \""+sdevId+"\" .\n");
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
