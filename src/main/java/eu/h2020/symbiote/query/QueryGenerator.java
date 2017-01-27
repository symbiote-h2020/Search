package eu.h2020.symbiote.query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * Class used to generate SPARQL query reflecting specified parameters of the query to the Search
 *
 * Created by Mael on 23/01/2017.
 */
public class QueryGenerator {

    private static final Log log = LogFactory.getLog(QueryGenerator.class);

    private StringBuilder query;

    private boolean platformAdded = false;
    private boolean multivaluequery = false;

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
        query.append("PREFIX spatial: <http://jena.apache.org/spatial#> \n");
        query.append("PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> \n\n");

//        query.append("SELECT ?sensor WHERE {\n");
//        query.append("\t?sensor a cim:Sensor .\n");

        query.append("SELECT ?resId ?resName ?resDescription ?platformId ?platformName ?locationName ?locationLat ?locationLong ?locationAlt ?propName WHERE {\n" );
        query.append("\t?sensor a cim:Sensor ;\n");
        query.append("\t\tcim:id ?resId ;\n");
        query.append("\t\trdfs:label ?resName ;\n");
        query.append("\t\trdfs:comment ?resDescription ;\n");
        query.append("\t\tcim:locatedAt ?location ;\n");
        query.append("\t\tcim:observes ?property .\n");
        query.append("\t?platform cimowl:hasID ?platformId ;\n");
        query.append("\t\tmim:hasName ?platformName .\n");
        query.append("\t?location rdfs:label ?locationName ;\n");
        query.append("\t\tgeo:lat ?locationLat ;\n");
        query.append("\t\tgeo:long ?locationLong ;\n");
        query.append("\t\tgeo:alt ?locationAlt .\n");
        query.append("\t?property rdfs:label ?propName .\n");
//        query.append("\t?item rdfs:label ?propertyName .\n" );
        ensurePlatformAdded();
    }

    public QueryGenerator addPlatformId( String platformId ) {
        ensurePlatformAdded();
        query.append("\t?platform cimowl:hasID \"" + platformId + "\" .\n");
        return this;
    }

    public QueryGenerator addPlatformName( String platformName ) {
        ensurePlatformAdded();
        if( containsRegex(platformName) ) {
            Command command = modifyInputAndGetCommand(platformName);
            addLikePlatformName(command.getRegex(), command.getRegexCommand());
        } else {
            query.append("\t?platform mim:hasName \"" + platformName + "\" .\n");
        }
        return this;
    }

    public QueryGenerator addLikePlatformName( String platformName, String command ) {
        ensurePlatformAdded();
        query.append("\t?platform mim:hasName ?platNamePattern . \n");
        query.append("\tFILTER ("+command+"(LCASE(?platNamePattern), LCASE(\"" + platformName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceName( String resourceName ) {
        if( containsRegex(resourceName) ) {
            Command command = modifyInputAndGetCommand(resourceName);
            addLikeResourceName(command.getRegex(), command.getRegexCommand());
        } else {
            query.append("\t?sensor rdfs:label \"" + resourceName + "\" .\n");
        }
        return this;
    }

    public QueryGenerator addLikeResourceName( String resourceName, String command ) {
        query.append("\t?sensor rdfs:label ?resNamePattern .\n");
        query.append("\tFILTER ("+command+"(LCASE(?resNamePattern), LCASE(\"" + resourceName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceId( String resourceId ) {
        query.append("\t?sensor cim:id \"" + resourceId + "\" .\n");
        return this;
    }

    public QueryGenerator addResourceDescription( String resourceDescription ) {
        if( containsRegex(resourceDescription) ) {;
            Command command = modifyInputAndGetCommand(resourceDescription);
            addLikeResourceDescription(command.getRegex(), command.getRegexCommand());
        } else {
            query.append("\t?sensor rdfs:comment \"" + resourceDescription + "\" .\n");
        }
        return this;
    }

    public QueryGenerator addLikeResourceDescription( String resourceDescription, String command ) {
        query.append("\t?sensor rdfs:comment ?resDescriptionPattern .\n");
        query.append("\tFILTER ("+command+"(LCASE(?resDescriptionPattern), LCASE(\"" + resourceDescription + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceLocationName( String locationName ) {
        if( containsRegex(locationName) ) {
            Command command = modifyInputAndGetCommand(locationName);
            addLikeResourceLocationName(command.getRegex(), command.getRegexCommand());
        } else {
            query.append("\t?sensor cim:locatedAt ?location .\n");
            query.append("\t?location rdfs:label \"" + locationName + "\" .\n");
        }
        return this;
    }

    public QueryGenerator addLikeResourceLocationName( String locationName, String command ) {
        query.append("\t?sensor cim:locatedAt ?location .\n");
        query.append("\t?location rdfs:label ?resLocationNamePattern .\n");
        query.append("\tFILTER ("+command+"(LCASE(?resLocationNamePattern), LCASE(\"" + locationName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceLocationDistance( Double latitude, Double longitude, Integer distance ) {
        query.append("\t?location spatial:nearby ("+latitude.toString()+" " + longitude.toString() + " " + distance + " 'm') .\n");
        query.append("\t?sensor cim:locatedAt ?location .\n");
        return this;
    }

    public QueryGenerator addResourceObservedPropertyName( String propertyName ) {
        multivaluequery = true;
        if( containsRegex(propertyName) ) {
            Command command = modifyInputAndGetCommand(propertyName);
            addLikeResourceObservedPropertyName(command.getRegex(), command.getRegexCommand());
        } else {
//            query.append("\t?sensor cim:observes ?property .\n");
            query.append("\t?property rdfs:label \"" + propertyName + "\" .\n");
        }
        return this;
    }

    public QueryGenerator addLikeResourceObservedPropertyName( String propertyName, String command ) {
        multivaluequery = true;
        query.append("\t?sensor cim:observes ?property .\n");
        query.append("\t?property rdfs:label ?propertyNamePattern .\n");
        query.append("\tFILTER ("+command+"(LCASE(?propertyNamePattern), LCASE(\"" + propertyName + "\"))) .");
        return this;
    }

    public QueryGenerator addResourceObservedPropertyNames( List<String> propertyNames ) {
        multivaluequery = true;
        int i = 0;
        for( String property: propertyNames ) {
            i++;
            if( i == 1 ) {
                query.append("\t?property rdfs:label \"" + property + "\" .\n");
            } else {
                query.append("\t?sensor cim:observes ?property" + i + " .\n");
                query.append("\t?property" + i + " rdfs:label \"" + property + "\" .\n");
            }
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

    private boolean containsRegex( String input ) {
        return input.startsWith("*")||input.endsWith("*");
    }

    /**
     * <b>Modifies</b> input parameter by removing preceding/trailing <b>*</b> and returns proper command to be
     * used by FILTER clause.
     *
     * @param input Input starting and/or ending with <b>*</b>. Method modifies it by removing that character(s).
     * @return Command to be used in FILTER clause. In case input without preceding/trailing <b>*</b> has been used throws exception.
     */
    private Command modifyInputAndGetCommand( String input ) throws InvalidParameterException {
        System.out.println("Modify: " + input);
        Command command = null;
        if( !containsRegex(input) ) {
            throw new InvalidParameterException("Input must contain * character at start and/or end of the string");
        }
        String result;
        if( input.startsWith("*") && input.endsWith("*") ) {
            command = new Command("CONTAINS",input.substring(1,input.length() - 1));
        } else if( input.startsWith("*")){
            command = new Command("STRENDS",input.substring(1));
        } else {
            command = new Command("STRSTARTS",input.substring(0,input.length() - 1));
        }
        System.out.println("After: " + input);
        return command;
    }

    @Override
    public String toString() {
        String resp = query.toString() +"\n}";
        log.debug( "SPARQL: " + resp );
        return resp;
    }

    public boolean isMultivaluequery() {
        return multivaluequery;
    }

    private class Command {
        String regexCommand;
        String regex;

        public Command(String regexCommand, String regex) {
            this.regexCommand = regexCommand;
            this.regex = regex;
        }

        public String getRegexCommand() {
            return regexCommand;
        }

        public void setRegexCommand(String regexCommand) {
            this.regexCommand = regexCommand;
        }

        public String getRegex() {
            return regex;
        }

        public void setRegex(String regex) {
            this.regex = regex;
        }
    }
}
