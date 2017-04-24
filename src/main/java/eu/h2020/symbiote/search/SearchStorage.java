package eu.h2020.symbiote.search;


import eu.h2020.symbiote.SearchApplication;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.SearchEngine;
import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;
import java.util.*;

/**
 * Created by Mael on 31/08/2016.
 */
public class SearchStorage {

    public static final String TESTCASE_STORAGE_NAME = "memory_test_storage";

    private static Log log = LogFactory.getLog( SearchStorage.class );

    private static Map<String,SearchStorage> storages = Collections.synchronizedMap( new HashMap<>() );
    private String storageLocation;

    private Registry core;
    private SearchEngine searchEngine;

    private TripleStore tripleStore;


    private SearchStorage(String storageLocation ) {
        log.info( "Starting platform storage based on Apache Jena");
        if( storageLocation == null ) {
            tripleStore = new TripleStore(  );
        } else {
            tripleStore = new TripleStore( storageLocation );
        }

        this.storageLocation = storageLocation;
        core = new Registry(tripleStore);
        searchEngine = new SearchEngine(tripleStore);
        log.info( "" );
    }

    /**
     * Gets or creates Search Storage singleton for default location
     *
     * @return Singleton of the storage for default location.
     */
    public static SearchStorage getInstance() {
        return getInstance(SearchApplication.DIRECTORY);
    }

    /**
     * Gets or creates Search Storage singleton for specified location.
     *
     * Use TESTCASE_STORAGE_NAME to use in-memory, not persistable storage (for testing/demo purpose).
     *
     * @param storageName Name of the storage, which corresponds to it's location.
     * @return Singleton of the storage for location with specified name
     */
    public static SearchStorage getInstance(String storageName ) {
        SearchStorage storage;
        synchronized ( storages ) {
            storage = storages.get(storageName);
            if( storage == null ) {
                log.debug("Creating Search Storage instance for " + storageName);
                if( storageName.equals(TESTCASE_STORAGE_NAME) ) {
                    storage = new SearchStorage( null );
                } else {
                    storage = new SearchStorage(storageName);
                }
                storages.put(storageName, storage);
            }
         }
        return storage;
    }

//    /**
//     * Registers ontology model in the search engine, using specified model's id to generate model graph uri.
//     *
//     * @param model Model to be registered
//     */
//    public void registerModel(OntologyModel model) {
//        log.info( "Registering model in search " + model.getId() + " ...");
//        core.registerModel(model.getId(),model.getInstance(), model.getFormat());
//        log.info( "Model registered!");
//    }

    /**
     *
     */
    public TripleStore getTripleStore() {
        return tripleStore;
    }


    /**
     * Registers platform in the search engine, using specified platform's id to generate platform graph uri
     *
     * @param platformId
     * @param rdfModel
     * @param modelId
     */
    public void registerPlatform(String platformId, Model rdfModel, String modelId ) {
        log.info( "Registering platform in search " + platformId + " ...");
        core.registerPlatform(platformId, rdfModel, modelId );
        log.info( "Platform registered!");
    }

    /**
     * Registers resource in the search engine for specified platform
     *
     * @param platformUri
     * @param rdfModel
     */
    public void registerResource( String platformUri, String serviceUri, String resourceUri, Model rdfModel ) {
        log.info( "Registering resource: | platformUri: " + platformUri + " | serviceUri: " + serviceUri + " | resourceUri: " + resourceUri + " ...");
        core.registerResource(platformUri, serviceUri, resourceUri, rdfModel );
        log.info( "Resource registered!");
    }

//    public void registerMapping(Mapping mapping ) {
//        log.info( "Registering mapping in search " + mapping.getId() + " ...");
//        try {
//            core.registerMapping(mapping.getId(),mapping.getModelId1(),mapping.getModelId2(),mapping.getMapping());
//        } catch (UnsupportedEncodingException e) {
//            log.error("Error when registering mapping in the core search", e);
//        }
//        log.info( "Mapping registered!");
//    }

    public List<String> query( String modelGraphUri, String query) {
        List<String> result = null;
        try {
            result = query(searchEngine, modelGraphUri, query);
//            Gson gson = new GsonBuilder().disableHtmlEscaping().create();
//            result = gson.toJson(resultList);
        } catch (IOException e) {
            log.error("Error during executing query",e);
        }
        return result;
    }

    public List<String> query( String modelGraphUri, Query query) {
        List<String> result = null;
        try {
            result = query(searchEngine, modelGraphUri, query);
        } catch (IOException e) {
            log.error("Error during executing query",e);
        }
        return result;
    }

    private static List<String> query(SearchEngine searchEngine, String modelGraphUri, Query sparql) throws IOException {

        log.info(String.format("executing query: modelId={}, sparql=\n{}", modelGraphUri, sparql));
        ResultSet result = searchEngine.search(sparql);
        List<String> results = generateOutputFromResultSet( result );

        log.info("----- result finish ----");
        return results;
    }

    private static List<String> query(SearchEngine searchEngine, String modelGraphUri, String sparql) throws IOException {

        log.info(String.format("executing query: modelId={}, sparql=\n{}", modelGraphUri, sparql));
        ResultSet result = searchEngine.search(modelGraphUri, sparql);
        List<String> results = generateOutputFromResultSet( result );

        log.info("----- result finish ----");
        return results;
    }

    private static List<String> generateOutputFromResultSet( ResultSet result ) {
        List<String> results = new ArrayList<String>();
        log.info("----- result ----");
        while (result.hasNext()) {
            QuerySolution solution = result.next();
            Iterator<String> varNames = solution.varNames();
            String temp = "";
            while (varNames.hasNext()) {
                String var = varNames.next();
                if (!temp.isEmpty()) {
                    temp += ", ";
                }
                temp += var + " = " + solution.get(var).toString();
            }
            results.add(temp);
            log.info(temp);
        }
        return results;
    }

}
