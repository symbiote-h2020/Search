package eu.h2020.symbiote.query;

import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the delete update operation.
 * <p>
 * Created by Mael on 26/01/2017.
 */
public class DeleteResourceRequestGenerator extends AbstractDeleteRequest {

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the resource with specified id,
     * as well as all informations connected to it. To generate the request use {@link #generateRequest()}.
     *
     * @param resourceId Id of the resource to be deleted.
     */
    public DeleteResourceRequestGenerator(String resourceId) {
        request = UpdateFactory.create();
        request.add(generateResourceRemoval(resourceId));
    }

//    private String generateServiceToResourceLinkRemoval(String resourceId) {
//        StringBuilder q = generateBaseQuery();
//        q.append("DELETE { ?service mim:hasResource ?sensor } WHERE {\n");
//        q.append("\t?service mim:hasResource ?sensor .\n");
//        q.append("\t?sensor cim:id \"" + resourceId + "\" .\n");
//        q.append("}");
//        return q.toString();
//    }
//
//    private String generateLocationRemoval(String resourceId) {
//        StringBuilder q = generateBaseQuery();
//        q.append("DELETE { ?location ?p ?o } WHERE {\n");
//        q.append("\t?location ?p ?o .\n");
//        q.append("\t?sensor cim:locatedAt ?location ;\n");
//        q.append("\t\tcim:id \"" + resourceId + "\" .\n");
//        q.append("}");
//        return q.toString();
//    }

    private String generateResourceRemoval(String resourceId) {
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?sensor ?p ?o . \n");
//        q.append(" \t?o ?p1 ?o1 . \n");
        q.append(" \t?service mim:hasResource ?sensor . \n");
        q.append(" \t?foi ?foip ?foio . \n");

        q.append("} WHERE {\n");
        q.append("\t?sensor ?p ?o ;\n");
        q.append("\t\tcim:id \"" + resourceId + "\" .\n");
        q.append("\t?service mim:hasResource ?sensor .\n");

        q.append("OPTIONAL {");
        q.append("\t?foi a cim:FeatureOfInterest .\n");
        q.append("\t?sensor cim:hasFeatureOfInterest ?foi .\n");
        q.append("\t?foi ?foip ?foio .\n");
        q.append("}");

//        q.append("OPTIONAL {");
//        q.append("\t?o ?p1 ?o1 .\n");
//        q.append("FILTER isBlank(?o) ");
//        q.append("FILTER NOT EXISTS {");
//        q.append("?ss ?pp ?o .");
//        q.append("}");
//        q.append("}");

        q.append("}");
        return q.toString();
    }

//    private String generateBlankLeavesRemoval() {
//        StringBuilder q = generateBaseQuery();
//        q.append("DELETE { ?s ?p ?o . \n");
//
//        q.append("} WHERE {\n");
//        q.append("\t?s ?p ?o .\n");
//        q.append("FILTER isBlank(?s) ");
//
//        q.append("FILTER NOT EXISTS {");
//        q.append("?s1 ?p1 ?s .");
//        q.append("}");
//
//        q.append("}");
//
//        q.append("}");
//        return q.toString();
//    }

    /**
     * Generates the update request, containing delete queries for resource and data linked to the resource.
     *
     * @return Update request which allows deletion of the resource and linked information.
     */
    public UpdateRequest generateRequest() {
        return request;
    }

}
