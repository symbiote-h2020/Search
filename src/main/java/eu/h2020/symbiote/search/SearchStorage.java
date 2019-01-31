package eu.h2020.symbiote.search;


import eu.h2020.symbiote.SearchApplication;
import eu.h2020.symbiote.core.cci.InfoModelMappingRequest;
import eu.h2020.symbiote.core.cci.InfoModelMappingResponse;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.SearchEngine;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.semantics.GraphHelper;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.vocabulary.RDF;

import java.io.IOException;
import java.util.*;

/**
 * Created by Mael on 31/08/2016.
 */
public class SearchStorage {

    public static final String TESTCASE_STORAGE_NAME = "memory_test_storage";

    private static Log log = LogFactory.getLog(SearchStorage.class);

    private static Map<String, SearchStorage> storages = Collections.synchronizedMap(new HashMap<>());
    private String storageLocation;

    private Registry core;
    private SearchEngine searchEngine;

    private TripleStore tripleStore;


    private SearchStorage(String storageLocation, SecurityManager securityManager, boolean securityEnabled) {
        log.info("Starting platform storage based on Apache Jena");
        if (storageLocation == null) {
            tripleStore = new TripleStore(securityManager, securityEnabled);
        } else {
            tripleStore = new TripleStore(storageLocation, securityManager, securityEnabled);
        }

        this.storageLocation = storageLocation;
        core = new Registry(tripleStore);
        searchEngine = new SearchEngine(tripleStore);
        log.info("");
    }

    /**
     * Gets or creates Search Storage singleton for default location
     *
     * @return Singleton of the storage for default location.
     */
    public static SearchStorage getInstance(SecurityManager securityManager, boolean securityEnabled) {
        return getInstance(SearchApplication.DIRECTORY, securityManager, securityEnabled);
    }

    /**
     * Gets or creates Search Storage singleton for specified location.
     * <p>
     * Use TESTCASE_STORAGE_NAME to use in-memory, not persistable storage (for testing/demo purpose).
     *
     * @param storageName Name of the storage, which corresponds to it's location.
     * @return Singleton of the storage for location with specified name
     */
    public static SearchStorage getInstance(String storageName, SecurityManager securityManager, boolean securityEnabled) {
        SearchStorage storage;
        synchronized (storages) {
            storage = storages.get(storageName);
            if (storage == null) {
                log.debug("Creating Search Storage instance for " + storageName);
                if (storageName.equals(TESTCASE_STORAGE_NAME)) {
                    storage = new SearchStorage(null, securityManager, securityEnabled);
                } else {
                    storage = new SearchStorage(storageName, securityManager, securityEnabled);
                }
                storages.put(storageName, storage);
            }
        }
        return storage;
    }

    /**
     * Used to clear all cached storages
     */
    public static void clearStorage() {
        synchronized (storages) {
            storages.clear();
        }
    }


    /**
     *
     */
    public TripleStore getTripleStore() {
        return tripleStore;
    }


    /**
     * Registers platform in the search engine, using specified platform's id to generate platform graph uri
     *
     * @param platformId symbIoTe Id of the platform
     * @param rdfModel
     */
    public void registerPlatform(String platformId, Model rdfModel) {
        log.info("Registering platform in search " + platformId + " ...");
        core.registerPlatform(platformId, rdfModel);
        log.info("Platform registered!");
    }

    /**
     * Registers ssp in the search engine, using specified ssp's id to generate graph uri
     *
     * @param sspId    symbIoTe Id of the ssp
     * @param rdfModel
     */
    public void registerSsp(String sspId, Model rdfModel) {
        log.info("Registering ssp in search " + sspId + " ...");
        core.registerSsp(sspId, rdfModel);
        log.info("Ssp registered!");
    }

    /**
     * Registers sdev in the search engine, using specified sdev's id to generate graph uri
     *
     * @param sdevId   symbIoTe Id of the sdev
     * @param rdfModel
     */
    public void registerSdev(String sdevId, Model rdfModel) {
        log.info("Registering sdev in search " + sdevId + " ...");
        core.registerSdev(sdevId, rdfModel);
        log.info("Sdev registered!");
    }


    /**
     * Registers resource in the search engine for specified platform
     *
     * @param platformUri
     * @param rdfModel
     */
    public void registerResource(String platformUri, String serviceUri, String resourceUri, Model rdfModel) {
        log.info("Registering resource: | platformUri: " + platformUri + " | serviceUri: " + serviceUri + " | resourceUri: " + resourceUri + " ...");
        core.registerResource(platformUri, serviceUri, resourceUri, rdfModel);
        log.info("Resource registered!");
    }

    public void registerSdevResourceLinkToSdevService(String sdevServiceUri, String resourceUri) {
        core.registerSdevResourceLinkToInterworkingService(sdevServiceUri, resourceUri);
    }

    public void registerModel(InformationModel informationModel) {
//        GraphHelper.insertGraph(pimDataset, ModelHelper.getInformationModelURI(model.getId()), model.getRdf(), model.getRdfFormat());
//        GraphHelper.insertGraph(tripleStore.get, informationModel.getUri(), informationModel.getRdf(), informationModel.getRdfFormat());

        tripleStore.registerInformationModel(informationModel);
    }


    public void registerMapping(InfoModelMappingRequest mapping ) {
        log.info("Generating and inserting metada about information model mapping");
        Model model = HandlerUtils.generateModelFromMapping(mapping.getBody());
        tripleStore.insertGraph(TripleStore.DEFAULT_GRAPH, model);
    }

    public void removeNamedGraph( String uri ) {
        tripleStore.removedNamedGraph(uri);
    }

    public OntModel getNamedGraphAsOntModel( String uri, boolean includeImport, boolean useInference ) {
        log.debug("Getting named ont model by uri " + uri);
        OntModel pimModel = tripleStore.getNamedOntModel(uri, includeImport, useInference);

        return pimModel;
    }

    public List<String> query
            (String modelGraphUri, String query) {
        List<String> result = null;
        try {
            result = query(searchEngine, modelGraphUri, query);
        } catch (IOException e) {
            log.error("Error during executing query", e);
        }
        return result;
    }

//    public List<String> query(String modelGraphUri, Query query) {
//        List<String> result = null;
//        try {
//            result = query(searchEngine, modelGraphUri, query);
//        } catch (IOException e) {
//            log.error("Error during executing query", e);
//        }
//        return result;
//    }

//    private static List<String> query(SearchEngine searchEngine, String modelGraphUri, Query sparql) throws IOException {
//
//        log.info(String.format("executing query: modelId={}, sparql=\n{}", modelGraphUri, sparql));
//        ResultSet result = searchEngine.search(sparql);
//        List<String> results = generateOutputFromResultSet(result);
//
//        log.info("----- result finish ----");
//        return results;
//    }

    private static List<String> query(SearchEngine searchEngine, String modelGraphUri, String sparql) throws IOException {

        log.info(String.format("executing query: modelId={}, sparql=\n{}", modelGraphUri, sparql));
        ResultSet result = searchEngine.search(modelGraphUri, sparql);
        List<String> results = generateOutputFromResultSet(result);

        log.info("----- result finish ----");
        return results;
    }

    private static List<String> generateOutputFromResultSet(ResultSet result) {
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
