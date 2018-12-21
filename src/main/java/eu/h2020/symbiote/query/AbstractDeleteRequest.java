package eu.h2020.symbiote.query;

import eu.h2020.symbiote.ontology.model.TripleStore;

/**
 * Created by Szymon Mueller on 28/05/2018.
 */
public abstract class AbstractDeleteRequest {

    StringBuilder generateBaseQuery() {
        StringBuilder query = new StringBuilder();
        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");

        return query;
    }

    String generateInformationServiceDelete( String platformId ) {
        //TODO r2
//        StringBuilder q = generateBaseQuery();
//        q.append("DELETE { ?service ?p ?o ; \n" );
//        q.append("\t\tmim:hasInformationModel ?imodel .\n");
//        q.append("\t?imodel cim:id ?id . \n");
//        q.append("} WHERE {\n");
//        q.append("\t?service ?p ?o ;\n");
//        q.append("\t\tmim:hasInformationModel ?imodel .\n");
//        q.append("\t?imodel cim:id ?id . \n");
//        q.append("\t?platform mim:hasService ?service ;\n");
//        q.append("\t\tcim:id \""+platformId+"\" .\n");
//        q.append("}");
        StringBuilder q = generateBaseQuery();
        q.append("WITH <" + TripleStore.DEFAULT_GRAPH + "> ");
        q.append("DELETE { ?service ?p ?o \n" );
        q.append("} WHERE {\n");
        q.append("\t?service ?p ?o .\n");
        q.append("\t?platform mim:hasService ?service ;\n");
        q.append("\t\tcim:id \""+platformId+"\" .\n");
        q.append("}");
        return q.toString();

    }

}
