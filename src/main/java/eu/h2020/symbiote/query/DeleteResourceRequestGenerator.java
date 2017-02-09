package eu.h2020.symbiote.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;

/**
 * Generator of the delete update operation.
 *
 * Created by Mael on 26/01/2017.
 */
public class DeleteResourceRequestGenerator {

    private static final Log log = LogFactory.getLog(DeleteResourceRequestGenerator.class);

    private UpdateRequest request;

    /**
     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the resource with specified id,
     * as well as all informations connected to it. To generate the request use {@link #generateRequest()}.
     *
     * @param resourceId Id of the resource to be deleted.
     */
    public DeleteResourceRequestGenerator(String resourceId ) {
        request = UpdateFactory.create();
        request.add(generateServiceToResourceLinkRemoval(resourceId));
        request.add(generateLocationRemoval(resourceId));
        request.add(generateResourceRemoval(resourceId));
    }

    private StringBuilder generateBaseQuery() {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta.owl#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");

        return query;
    }

    private String generateServiceToResourceLinkRemoval( String resourceId ) {
        StringBuilder q = generateBaseQuery();
        q.append("DELETE { ?service mim:hasResource ?sensor } WHERE {\n");
//        q.append("\t?service rdfs:type mim:InterworkingService ; \n");
        q.append("\t?service mim:hasResource ?sensor .\n");
        q.append("\t?sensor cim:id \""+resourceId+"\" .\n");
        q.append("}");
        return q.toString();
    }

    private String generateLocationRemoval( String resourceId ) {
        StringBuilder q = generateBaseQuery();
        q.append("DELETE { ?location ?p ?o } WHERE {\n");
//        q.append("\t?location rdfs:type cim:Location .\n");
        q.append("\t?location ?p ?o .\n");
        q.append("\t?sensor cim:locatedAt ?location ;\n");
        q.append("\t\tcim:id \""+resourceId+"\" .\n");
        q.append("}");
        return q.toString();
    }

    private String generateResourceRemoval( String resourceId ) {
        StringBuilder q = generateBaseQuery();
        q.append("DELETE { ?sensor ?p ?o ; \n" );
        q.append( " \tcim:observes ?property . \n");
        q.append( " ?property rdfs:label ?label ; \n");
        q.append( " \trdfs:comment ?comment . \n");
        q.append( "} WHERE {\n");
        q.append("\t?sensor ?p ?o ;\n");
        q.append("\t\tcim:observes ?property ;\n");
        q.append("\t\tcim:id \""+resourceId+"\" .\n");
        q.append("\t?property rdfs:label ?label ; \n");
        q.append("\t\trdfs:comment ?comment . \n");
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
