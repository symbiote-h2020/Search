package eu.h2020.symbiote.query;

import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the delete update operation for ssp.
 *
 * Created by Mael on 28/05/2018.
 */
public class DeleteSspRequestGenerator extends AbstractDeleteRequest {

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the ssp with specified id
     * and connected statements.
     * To generate the request use {@link #generateRequest()}.
     *
     * @param sspId Id of the ssp to be deleted.
     */
    public DeleteSspRequestGenerator(String sspId ) {
        request = UpdateFactory.create();
        request.add(generateInformationServiceDelete(sspId));
        request.add(generateSspDelete(sspId));
        request.add(generateSdevDelete(sspId));
    }

    private String generateSspDelete( String sspId ) {
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?ssp ?p ?o } WHERE {\n");
        q.append("\t?ssp ?p ?o ;\n");
        q.append("\t\tcim:id \""+sspId+"\" .\n");
        q.append("}");
        return q.toString();
    }

    private String generateSdevDelete( String sspId ) {
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?sdev ?p ?o \n");
        q.append("} WHERE {\n");
        q.append("\t?sdev ?p ?o .\n");
        q.append("\t?sdev mim:isConnectedTo ?platform .\n");
        q.append("\t?platform cim:id \""+sspId+"\" .\n");
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
