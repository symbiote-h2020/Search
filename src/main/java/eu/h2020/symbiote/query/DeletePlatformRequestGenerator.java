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
public class DeletePlatformRequestGenerator {

    private static final Log log = LogFactory.getLog(DeletePlatformRequestGenerator.class);

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

    private StringBuilder generateBaseQuery() {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");

        return query;
    }

    private String generatePlatformDelete( String platformId ) {
        StringBuilder q = generateBaseQuery();
        q.append("DELETE { ?platform ?p ?o } WHERE {\n");
        q.append("\t?platform ?p ?o ;\n");
        q.append("\t\tcim:id \""+platformId+"\" .\n");
        q.append("}");
        System.out.println(q.toString());
        return q.toString();
    }

//    private String generateInformationServiceDelete3( String platformId ) {
//        StringBuilder q = generateBaseQuery();
//        q.append("DELETE { ?service ?p ?o } WHERE {\n");
//        q.append("\t?service ?p ?o .\n");
//        q.append("\t?platform mim:hasService ?service ;\n");
//        q.append("\t\tcimowl:hasID \""+platformId+"\" .\n");
//        q.append("}");
//        System.out.println(q.toString());
//        return q.toString();
//    }

    private String generateInformationServiceDelete( String platformId ) {
        StringBuilder q = generateBaseQuery();
        q.append("DELETE { ?service ?p ?o ; \n" );
        q.append("\t\tmim:hasInformationModel ?imodel .\n");
        q.append("\t?imodel cim:id ?id . \n");
        q.append("} WHERE {\n");
        q.append("\t?service ?p ?o ;\n");
        q.append("\t\tmim:hasInformationModel ?imodel .\n");
        q.append("\t?imodel cim:id ?id . \n");
        q.append("\t?platform mim:hasService ?service ;\n");
        q.append("\t\tcim:id \""+platformId+"\" .\n");
        q.append("}");
        System.out.println( q.toString());
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
