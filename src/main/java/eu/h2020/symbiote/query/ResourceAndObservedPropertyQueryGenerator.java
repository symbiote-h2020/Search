package eu.h2020.symbiote.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Created by Mael on 26/01/2017.
 */
public class ResourceAndObservedPropertyQueryGenerator {

    private static final Log log = LogFactory.getLog(ResourceAndObservedPropertyQueryGenerator.class);

    private StringBuilder query;

    public ResourceAndObservedPropertyQueryGenerator( List<String> resIds) {
        query = new StringBuilder();
        generateBaseQuery(resIds);
    }

    private void generateBaseQuery(List<String> resIds) {

        //Generate list of ids: ("5a5e099c0f1ad43334f3131c")("a5f4ce90f1ad43334f31340")
        StringBuilder sb = new StringBuilder();
        resIds.stream().forEach(s -> sb.append("(\""+s+"\")") );

        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");

        query.append("SELECT ?sensor ?propName ?property ?propDesc ?value WHERE {\n");
        query.append("VALUES (?value) {" + sb.toString() + "}");
//        query.append("\t?sensor a cim:Sensor ;\n");
        query.append("\t?sensor cim:id ?value ;\n");
        query.append("\t\tcim:observesProperty ?property .\n");
        query.append("\t?property cim:name ?propName .\n");
        query.append("\tOPTIONAL {\n");
        query.append("\t?property cim:description ?propDesc .\n");
        query.append("\t}\n");
    }

    @Override
    public String toString() {
        String resp = query.toString() +"\n}";
//        log.debug( "SPARQL: " + resp );
        return resp;
    }

}
