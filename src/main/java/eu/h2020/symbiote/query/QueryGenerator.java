package eu.h2020.symbiote.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;

/**
 * Class used to generate SPARQL query reflecting specified parameters of the query to the Search
 *
 * Created by Mael on 23/01/2017.
 */
public class QueryGenerator {

    private static final Log log = LogFactory.getLog(QueryGenerator.class);

    StringBuilder query;

    private boolean platformAdded = false;

    public QueryGenerator() {
        query = new StringBuilder();
        generateBaseQuery();
    }

    private void generateBaseQuery() {

        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
        query.append("PREFIX cimowl: <http://www.symbiote-h2020.eu/ontology/core.owl#> \n");
        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta.owl#> \n");
        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
        query.append("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n");
        query.append("PREFIX owl: <http://www.w3.org/2002/07/owl#> \n");
        query.append("PREFIX spatial: <http://jena.apache.org/spatial#> \n\n");

        query.append("SELECT ?sensor ?location WHERE {\n");
        query.append("\t?sensor a cim:Sensor .\n");
    }

    public QueryGenerator addPlatformId( String platformId ) {
        ensurePlatformAdded();
        query.append("\t?platform cimowl:hasID \"" + platformId + "\" .\n");
        return this;
    }

    public QueryGenerator addPlatformName( String platformName ) {
        ensurePlatformAdded();
        query.append("\t?platform mim:hasName \"" + platformName + "\" .\n");
        return this;
    }

    public QueryGenerator addLikePlatformName( String platformName ) {
        ensurePlatformAdded();
        query.append("\t?platform mim:hasName ?platNamePattern . \n");
        query.append("\tFILTER (CONTAINS(LCASE(?platNamePattern), LCASE(\"" + platformName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceName( String resourceName ) {
        query.append("\t?sensor rdfs:label \"" + resourceName + "\" .\n");
        return this;
    }

    public QueryGenerator addLikeResourceName( String resourceName ) {
        query.append("\t?sensor rdfs:label ?resNamePattern .\n");
        query.append("\tFILTER (CONTAINS(LCASE(?resNamePattern), LCASE(\"" + resourceName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceId( String resourceId ) {
        query.append("\t?sensor cim:id \"" + resourceId + "\" ;\n");
        return this;
    }

    public QueryGenerator addResourceDescription( String resourceDescription ) {
        query.append("\t?sensor rdfs:comment \"" + resourceDescription + "\" .\n");
        return this;
    }

    public QueryGenerator addLikeResourceDescription( String resourceDescription ) {
        query.append("\t?sensor rdfs:comment ?resDescriptionPattern .\n");
        query.append("\tFILTER (CONTAINS(LCASE(?resDescriptionPattern), LCASE(\"" + resourceDescription + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceLocationName( String locationName ) {
        query.append("\t?sensor cim:locatedAt ?location .\n");
        query.append("\t?location rdfs:label \""+locationName+"\" .\n");
        return this;
    }

    public QueryGenerator addLikeResourceLocationName( String locationName ) {
        query.append("\t?sensor cim:locatedAt ?location .\n");
        query.append("\t?location rdfs:label ?resLocationNamePattern .\n");
        query.append("\tFILTER (CONTAINS(LCASE(?resLocationNamePattern), LCASE(\"" + locationName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceLocationDistance( Double latitude, Double longitude, Integer distance ) {
        query.append("\t?location spatial:nearby ("+latitude.toString()+" " + longitude.toString() + " " + distance + " 'm') .\n");
        query.append("\t?sensor cim:locatedAt ?location .\n");
        return this;
    }

    public QueryGenerator addResourceObservedPropertyName( String propertyName ) {
        query.append("\t?sensor cim:observes ?property .\n");
        query.append("\t?property rdfs:label \""+propertyName+"\" .\n");
        return this;
    }

    public QueryGenerator addLikeResourceObservedPropertyName( String propertyName ) {
        query.append("\t?sensor cim:observes ?property .\n");
        query.append("\t?property rdfs:label ?propertyNamePattern .\n");
        query.append("\tFILTER (CONTAINS(LCASE(?propertyNamePattern), LCASE(\"" + propertyName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceObservedPropertyNames( List<String> propertyNames ) {
        int i = 0;
        for( String property: propertyNames ) {
            i++;
            query.append("\t?sensor cim:observes ?property"+i+" .\n");
            query.append("\t?property"+i+" rdfs:label \"" + property + "\" .\n");
        }
        return this;
    }


    private void ensurePlatformAdded() {
        if( !platformAdded ) {
            query.append("\t?platform a owl:Ontology ;\n");
            query.append("\t\tmim:hasService ?service .\n");
            query.append("\t?service mim:hasResource ?sensor .\n");
            platformAdded = true;
        }
    }

    @Override
    public String toString() {
        String resp = query.toString() +"\n}";
        log.debug( "SPARQL: " + resp );
        return resp;
    }
}
