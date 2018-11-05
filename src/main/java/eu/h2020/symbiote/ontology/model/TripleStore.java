/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model;

import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.FilteringEvaluator;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.semantics.GraphHelper;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.permissions.Factory;
import org.apache.jena.query.*;
import org.apache.jena.query.spatial.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * Class representing a triplestore - connected to in-memory or disk (TDB) jena repository. Creates a spatial index
 * based on Lucene
 *
 * Contains useful methods to access datastore: inserting graphs (save), querying graphs (read).
 *
 * @author jab
 */
public class TripleStore {

    private static final String CIM_ID = "CIM";
    private static final String BIM_ID = "BIM";
    private static final String MIM_ID = "MIM";
    private static final String QU_ID = "QU";

    private final static boolean SHOULD_PRINT_DATASET = false;

    private SecurityManager securityManager;
    private boolean filteringEnabled;
    private final String currentDirectory;
    private final Dataset dataset;
//    private final Dataset modelsDataset;
    private static final String BASE_REPO = "base";
    private static final String SPATIAL_REPO = "spatial";
    private final Model securedModel;
    private final Model model;
    private FilteringEvaluator evaluator;
    private int sparqlQueryTimeout = 300000;


//    private static final String CIM_FILE = "/core-v0.6.owl";
//    private static final String BIM_FILE = "/bim-0.3.owl";
//    private static final String MIM_FILE = "/meta-v0.3.owl";
//    private static final String QU_FILE = "/qu-rec20.owl";

    private static final Log log = LogFactory.getLog(TripleStore.class);

    //For tests only - in memory
    public TripleStore(SecurityManager securitymanager, boolean filteringEnabled ) {
        this.filteringEnabled = filteringEnabled;
        this.securityManager = securitymanager;

        currentDirectory = null;

        EntityDefinition entDef = new EntityDefinition("entityField", "geoField");

        Dataset baseDataset = DatasetFactory.create();

        Directory ramDir = new RAMDirectory();

        dataset = SpatialDatasetFactory.createLucene(baseDataset, ramDir, entDef);
//        modelsDataset = DatasetFactory.create();

        loadModels();

        model = ModelFactory.createRDFSModel(dataset.getDefaultModel());


        if( filteringEnabled ) {
            evaluator = new FilteringEvaluator(model,securityManager);
            evaluator.setPrincipal("test");
            securedModel = Factory.getInstance(evaluator,"http://symbiote-h2020.eu/secureModel",model);
        } else {
            securedModel = model;
        }
    }

    public TripleStore( String directory, SecurityManager securitymanager, boolean filteringEnabled ) {
        this.filteringEnabled = filteringEnabled;
        this.securityManager = securitymanager;
        boolean newRepo = false;
        currentDirectory = directory;

        EntityDefinition entDef = new EntityDefinition("entityField", "geoField");

        File dir = new File( directory + (directory.endsWith("/")?"":"/") + SPATIAL_REPO );

        if( !dir.exists() ) {
            dir.mkdirs();
        }

        String baseRepoLocation = directory + (directory.endsWith("/") ? "" : "/") + BASE_REPO;
        File baseDir = new File( baseRepoLocation );
        if( !baseDir.exists() ) {
            baseDir.mkdirs();

            newRepo = true;
        }
        Dataset baseDataset= TDBFactory.createDataset(baseRepoLocation);
        Directory realDir = null;
        try {
            realDir = FSDirectory.open(dir.toPath());
        } catch (IOException e) {
            log.error(e);
        }

        dataset = SpatialDatasetFactory.createLucene(baseDataset, realDir, entDef);
//        DatasetGraphSpatial datasetGraph = (DatasetGraphSpatial) (dataset.asDatasetGraph());
//
//        SpatialIndex spatialIndex = datasetGraph.getSpatialIndex();

        if( newRepo ) {
//            try {
//                String cim_data = IOUtils.toString(TripleStore.class
//                        .getResourceAsStream(CIM_FILE));
//                insertGraph("", cim_data, RDFFormat.Turtle);
//
//                String bim_data = IOUtils.toString(TripleStore.class
//                        .getResourceAsStream(BIM_FILE));
//                insertGraph("", bim_data, RDFFormat.Turtle);
//
//                String mim_data = IOUtils.toString(TripleStore.class
//                        .getResourceAsStream(MIM_FILE));
//                insertGraph("", mim_data, RDFFormat.Turtle);
//
//                String qureq20_data = IOUtils.toString(TripleStore.class
//                        .getResourceAsStream(QU_FILE));
//                insertGraph("", qureq20_data, RDFFormat.RDFXML);
//
////                printDataset();
//            } catch (IOException e) {
//                log.fatal("Could not load CIM file: " + e.getMessage(),e);
//            }


            //TODO comment for local
            loadModels();
        }

        //TODO DELTETE
        //TODO Just once
//        String insertCapNames = "PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> PREFIX owl: <http://www.w3.org/2002/07/owl#> PREFIX spatial: <http://jena.apache.org/spatial#> PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> INSERT { ?cap cim:name \"cap1\" } WHERE { ?owner cim:hasCapability ?cap . ?cap a cim:Capability. }";
//        System.out.println("GONNA UPDATE ...");
//        UpdateRequest request = UpdateFactory.create();
//        request.add(insertCapNames);
//        executeUpdate(request);
//        System.out.println( " AFTER UPDATE !");

        model = ModelFactory.createRDFSModel(dataset.getDefaultModel());
        if( filteringEnabled ) {
            evaluator = new FilteringEvaluator(model,securityManager);
            evaluator.setPrincipal("test");
            securedModel = Factory.getInstance(evaluator,"http://symbiote-h2020.eu/secureModel",model);
        } else {
            securedModel = model;
        }



    }

    public void setSparqlQueryTimeout(int sparqlQueryTimeout) {
        this.sparqlQueryTimeout = sparqlQueryTimeout;
    }

    private void loadModels() {
//        try {

            loadBaseModel(CIM.getURI(), ModelHelper.getInformationModelURI(CIM_ID), dataset);
            loadBaseModel(MIM.getURI(), ModelHelper.getInformationModelURI(MIM_ID), dataset);
            loadBaseModel(BIM.getURI(), ModelHelper.getInformationModelURI(BIM_ID), dataset);
            loadBaseModel(BIM_PROPERTY.getURI(), ModelHelper.getInformationModelURI("BIM_PROPERTY"),dataset);

            // should not be neccesarry if BIM is loaded with imports
            loadBaseModel(QU.getURI(), ModelHelper.getInformationModelURI(QU_ID), dataset);
            loadBaseModel(BIM_QU_ALIGN.getURI(), ModelHelper.getInformationModelURI("BIM_QU"), dataset);

//            String cimRdf = IOUtils.toString(URI.create(CIM.getURI()), Charset.defaultCharset());
//
//            String bimRdf = IOUtils.toString(URI.create(BIM.getURI()), Charset.defaultCharset());
//
//            String mimRdf = IOUtils.toString(URI.create(MIM.getURI()), Charset.defaultCharset());
//
//            String quRecRdf = IOUtils.toString(URI.create(QU.getURI()), Charset.defaultCharset());
//
//            insertGraph("", cimRdf, RDFFormat.Turtle);
//
////            String bim_data = IOUtils.toString(TripleStore.class
////                    .getResourceAsStream(BIM_FILE));
////            insertGraph("", bimRdf, RDFFormat.Turtle);
//
////            String mim_data = IOUtils.toString(TripleStore.class
////                    .getResourceAsStream(MIM_FILE));
//            insertGraph("", mimRdf, RDFFormat.Turtle);

//            String qureq20_data = IOUtils.toString(TripleStore.class
//                    .getResourceAsStream(QU_FILE));
//            insertGraph("", quRecRdf, RDFFormat.RDFXML);
//
//        } catch (IOException e) {
//            log.fatal("Could not load CIM file: " + e.getMessage(), e);
//        }
    }

    private void loadBaseModel(String loadUri, String insertUri, Dataset dataset) {
        try {
            Model model = ModelHelper.readModel(loadUri,true,false);
            insertGraph(null,  model);
        } catch (IOException ex) {
            log.error("could not load model '" + loadUri + "'. Reason: " + ex.getMessage());
        }
    }

    public void insertGraph(String uri, String rdf, RDFFormat format) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(rdf.getBytes()), null, format.toString());
        insertGraph(uri, model);
    }

    public void insertGraph(String uri, Model model) {
//        dataset.begin(ReadWrite.WRITE);
//        if (!dataset.containsNamedModel(uri)) {
//            dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
//        }
//        dataset.getNamedModel(uri).add(model);
        dataset.begin(ReadWrite.WRITE);
        try {
            dataset.getDefaultModel().add(model);
            dataset.commit();
        } finally {
            dataset.end();
        }
    }



    public void insertModelGraph( String uri, String rdf, RDFFormat rdfFormat ) {
        GraphHelper.insertGraph(dataset , uri, rdf, rdfFormat);
        //Old implementation
//        dataset.begin(ReadWrite.WRITE);
//        try {
//            if (!dataset.containsNamedModel(uri)) {
//                dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
//            }
//            dataset.getNamedModel(uri).add(model);
//            dataset.getDefaultModel().add(model);
//            dataset.commit();
//        } finally {
//            dataset.end();
//        }
    }

    public void removedNamedGraph( String graphUri) {
        GraphHelper.removeGraph(dataset, graphUri);
    }


    public ResultSet executeQuery(String queryString, SecurityRequest securityRequest, boolean useSecureGraph) {
        return executeQueryOnGraph(queryString, "urn:x-arq:DefaultGraph", securityRequest, useSecureGraph);
    }

    public ResultSet executeQueryOnUnionGraph(String queryString, SecurityRequest securityRequest, boolean useSecureGraph) {
        return executeQueryOnGraph(queryString, "urn:x-arq:UnionGraph", securityRequest, useSecureGraph);
    }

    public ResultSet executeQuery(Query query, SecurityRequest securityRequest, boolean useSecureGraph) {
        return executeQueryOnGraph(query, "urn:x-arq:DefaultGraph", securityRequest, useSecureGraph);
    }

    public ResultSet executeQueryOnGraph(String queryString, String graphUri, SecurityRequest securityRequest, boolean useSecureGraph) {
        return executeQueryOnGraph(QueryFactory.create(queryString, Syntax.syntaxARQ), graphUri, securityRequest, useSecureGraph);
    }

    public void executeUpdate( UpdateRequest request ) {
        dataset.begin(ReadWrite.WRITE);
        try {
            UpdateAction.execute(request, dataset);
            dataset.commit();
        } finally {
            dataset.end();
        }
    }

    public ResultSet executeQueryOnGraph(Query query, String graphUri, SecurityRequest securityRequest, boolean useSecureGraph) {
        dataset.begin(ReadWrite.READ);
        ResultSet result;
        try {
            //TODO not sure if synchronization here is needed
//        synchronized( TripleStore.class ) {

//        Model model = dataset.containsNamedModel(graphUri)
//                ? dataset.getNamedModel(graphUri)
//                : dataset.getDefaultModel();
//        Model model = dataset.getDefaultModel();

//        Model model = dataset.getDefaultModel();
//        if( filteringEnabled ) {
//            FilteringEvaluator evaluator = new FilteringEvaluator(model,securityManager);
//            evaluator.setPrincipal("test");
//            model = Factory.getInstance(evaluator,"http://symbiote-h2020.eu/secureModel",model);

//            Model modelToUse = useSecureGraph ? securedModel:model;
            if (useSecureGraph) {
//                synchronized (TripleStore.class) {
                    setSecurityRequest(securityRequest);
                    try (QueryExecution qe = QueryExecutionFactory.create(query, securedModel)) {
                        qe.setTimeout(sparqlQueryTimeout);
                        result = ResultSetFactory.copyResults(qe.execSelect());
                    }
//                }
            } else {
//            Model mm = ModelFactory.createRDFSModel(dataset.getDefaultModel());
                try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
                    qe.setTimeout(sparqlQueryTimeout);
                    ResultSet resultSet = qe.execSelect();
                    //PRINTING INSTEAD OF RETURNING - comment
//                    log.debug("Copying resultSet, hasNext: " + resultSet.hasNext());
//                    while( resultSet.hasNext() ) {
//                        QuerySolution next = resultSet.next();
//                        Iterator<String> varIterator = next.varNames();
//
//                            System.out.print( "||||" );
//                            while( varIterator.hasNext() ) {
//                                System.out.print( "  " + varIterator.next() + "  |");
//                            }
//                            System.out.println( "|||" );
//                            varIterator = next.varNames();
//
//                        while( varIterator.hasNext() ) {
//                            System.out.print("  " + next.get(varIterator.next()).toString() + "  " );
//                        }
//                        System.out.println("");
//                    }

                    result = ResultSetFactory.copyResults(resultSet);

                }
            }
//        }
        } finally {
            dataset.end();
        }
        return result;
    }

    /**
     * Helper method for properly setting security request in both cases: when security is on and off
     * @param securityRequest
     */
    private void setSecurityRequest(SecurityRequest securityRequest) {
        if( evaluator != null ) {
            evaluator.setSecurityRequest(securityRequest);
        }
    }

    public Model getDefaultGraph() {
        dataset.begin(ReadWrite.READ);
        Model result = dataset.getDefaultModel();
        dataset.end();
        return result;
    }

    public Model getNamedModel( String uri ) {
        Model modelToReturn = ModelFactory.createDefaultModel();

        dataset.begin(ReadWrite.READ);
        Model namedModel = dataset.getNamedModel( uri );

        modelToReturn.add(namedModel);

        dataset.end();
        return modelToReturn;
    }

    public void printDataset() {
        if( SHOULD_PRINT_DATASET ) {
            dataset.begin(ReadWrite.READ);
            Model result = dataset.getDefaultModel();
            result.write(System.out, "TURTLE");
            dataset.end();
        }
    }

}
