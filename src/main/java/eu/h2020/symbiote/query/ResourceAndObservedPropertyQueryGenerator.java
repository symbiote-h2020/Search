package eu.h2020.symbiote.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by Mael on 26/01/2017.
 */
public class ResourceAndObservedPropertyQueryGenerator {

    private static final Log log = LogFactory.getLog(ResourceAndObservedPropertyQueryGenerator.class);

    private StringBuilder query;

    public ResourceAndObservedPropertyQueryGenerator( String resId) {
        query = new StringBuilder();
        generateBaseQuery(resId);
    }

    private void generateBaseQuery(String resId) {

        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");

        query.append("SELECT ?sensor ?propName WHERE {\n");
        query.append("\t?sensor a cim:Sensor ;\n");
        query.append("\t\tcim:id \""+resId+"\" ;\n");
        query.append("\t\tcim:observes ?property .\n");
        query.append("\t?property rdfs:label ?propName .\n");
    }

    @Override
    public String toString() {
        String resp = query.toString() +"\n}";
        log.debug( "SPARQL: " + resp );
        return resp;
    }

}
