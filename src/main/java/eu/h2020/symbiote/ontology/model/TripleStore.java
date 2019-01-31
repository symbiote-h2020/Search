/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model;

import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.FilteringEvaluator;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.query.DeleteResourceRequestGenerator;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.semantics.GraphHelper;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.*;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Node;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.permissions.Factory;
import org.apache.jena.permissions.model.SecuredModel;
import org.apache.jena.query.*;
import org.apache.jena.query.spatial.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.RDF;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing a triplestore - connected to in-memory or disk (TDB) jena repository. Creates a spatial index
 * based on Lucene
 * <p>
 * Contains useful methods to access datastore: inserting graphs (save), querying graphs (read).
 *
 * @author jab
 */
public class TripleStore {

    public static final String DEFAULT_GRAPH = "http://www.symbiote-h2020.eu/ontology/internal/meta";
    public static final String UNION_GRAPH = "urn:x-arq:UnionGraph";

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
    public TripleStore(SecurityManager securitymanager, boolean filteringEnabled) {
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


        if (filteringEnabled) {
            evaluator = new FilteringEvaluator(model, securityManager);
            evaluator.setPrincipal("test");
            securedModel = Factory.getInstance(evaluator, "http://symbiote-h2020.eu/secureModel", model);
        } else {
            securedModel = model;
        }
    }

    public TripleStore(String directory, SecurityManager securitymanager, boolean filteringEnabled) {
        this.filteringEnabled = filteringEnabled;
        this.securityManager = securitymanager;
        boolean newRepo = false;
        currentDirectory = directory;

//        EntityDefinition entDef = new EntityDefinition("entityField", "geoField");

        EntityDefinition entDef = new EntityDefinition("uri","geo");

        File dir = new File(directory + (directory.endsWith("/") ? "" : "/") + SPATIAL_REPO);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String baseRepoLocation = directory + (directory.endsWith("/") ? "" : "/") + BASE_REPO;
        File baseDir = new File(baseRepoLocation);
        if (!baseDir.exists()) {
            baseDir.mkdirs();

            newRepo = true;
        }
        Dataset baseDataset = TDBFactory.createDataset(baseRepoLocation);
        Directory realDir = null;
        try {
            realDir = FSDirectory.open(dir.toPath());
        } catch (IOException e) {
            log.error(e);
        }

        dataset = SpatialDatasetFactory.createLucene(baseDataset, realDir, entDef);

        //Additional spatial setting test - 10_01_2019
//        DatasetGraphSpatial spatialDataset = (DatasetGraphSpatial) (dataset.asDatasetGraph());
//        SpatialIndex spatialIndex = spatialDataset.getSpatialIndex();
//
//        SpatialIndexContext context = new SpatialIndexContext(spatialIndex);
//        spatialIndex.startIndexing();
//
//        Iterator<Quad> quadIter = spatialDataset.find(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
//        for (; quadIter.hasNext();) {
//            Quad quad = quadIter.next();
//            context.index(quad.getGraph(), quad.getSubject(), quad.getPredicate(), quad.getObject());
//        }
//        spatialIndex.finishIndexing();

//        DatasetGraphSpatial datasetGraph = (DatasetGraphSpatial) (dataset.asDatasetGraph());
//
//        SpatialIndex spatialIndex = datasetGraph.getSpatialIndex();

        if (newRepo) {
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
            log.debug("Loading models for new repo...");
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
        if (filteringEnabled) {
            evaluator = new FilteringEvaluator(model, securityManager);
            evaluator.setPrincipal("test");
            securedModel = Factory.getInstance(evaluator, "http://symbiote-h2020.eu/secureModel", model);
        } else {
            securedModel = model;
        }


    }

    public void setSparqlQueryTimeout(int sparqlQueryTimeout) {
        this.sparqlQueryTimeout = sparqlQueryTimeout;
    }

    public void loadModelsToNamedGraphs() {
        loadModels();
    }

    private void loadModels() {
//        try {
        loadBaseModel(CIM.getURI());
        loadBaseModel(MIM.getURI());
        loadBaseModel(BIM.getURI());
        loadBaseModel(BIM_PROPERTY.getURI());

        // should not be neccesarry if BIM is loaded with imports
        loadBaseModel(QU.getURI());
        loadBaseModel(BIM_QU_ALIGN.getURI());

        //Add BIM as an information model
        addBIMasPIM();


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

    public void addBIMasPIM() {
        InformationModel informationModel = new InformationModel();
        informationModel.setId("BIM");
        informationModel.setName("BIM");
        informationModel.setUri(BIM.getURI());

        insertGraph(TripleStore.DEFAULT_GRAPH, getInformationModelMetadata(informationModel), RDFFormat.Turtle);
    }

    public void loadBaseModel(String loadUri) {
        try {
            Model model = ModelHelper.readModel(loadUri, true, true);
            insertGraph(loadUri, model);
        } catch (IOException ex) {
            log.error("could not load model '" + loadUri + "'. Reason: " + ex.getMessage());
        }
    }

//    private void loadBaseModel2(String loadUri, String insertUri, Dataset dataset) {
//        try {
//            Model model = ModelHelper.readModel(loadUri, true, false);
//            insertGraph(insertUri, model);
//        } catch (IOException ex) {
//            log.error("could not load model '" + loadUri + "'. Reason: " + ex.getMessage());
//        }
//    }

    public void insertGraph(String uri, String rdf, RDFFormat format) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(rdf.getBytes()), null, format.toString());
        insertGraph(uri, model);
    }

    public void insertGraph(String uri, Model model) {
        dataset.begin(ReadWrite.WRITE);
        log.debug("Inserting graph into " + uri);
        try {
            if (!dataset.containsNamedModel(uri)) {
                log.debug("creating named model " + uri);
                dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
            }
//        dataset.getNamedModel(uri).add(model);
//        dataset.begin(ReadWrite.WRITE);
            dataset.getNamedModel(uri).add(model);
            dataset.commit();
        } finally {
            log.debug("Closing dataset");
            dataset.end();
        }
    }


    public void insertModelGraph(String uri, String rdf, RDFFormat rdfFormat) {
        GraphHelper.insertGraph(dataset, uri, rdf, rdfFormat);
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

//    public void removedNamedGraph( String graphUri) {
//        GraphHelper.removeGraph(dataset, graphUri);
//    }


    public void removedNamedGraph(String graphUri) {
        UpdateRequest grapDropRequest = UpdateFactory.create("DROP GRAPH <" + graphUri + ">");
        executeUpdate(grapDropRequest);
    }


    public ResultSet executeQuery(String queryString, SecurityRequest securityRequest, boolean useSecureGraph) {
//        return executeQueryOnGraph(queryString, "urn:x-arq:UnionGraph", securityRequest, useSecureGraph);
//        return executeQueryOnGraph(queryString, "urn:x-arq:DefaultGraph", securityRequest, useSecureGraph);
        return executeQueryOnDataset(QueryFactory.create(queryString, Syntax.syntaxARQ), securityRequest, useSecureGraph);
    }

    public ResultSet executeQueryOnUnionGraph(String queryString, SecurityRequest securityRequest, boolean useSecureGraph) {
        return executeQueryOnGraph(queryString, TripleStore.DEFAULT_GRAPH, securityRequest, useSecureGraph);
//        return executeQueryOnGraph(queryString, "urn:x-arq:UnionGraph", securityRequest, useSecureGraph);
//        return executeQueryOnGraph(queryString, "urn:x-arq:DefaultGraph", securityRequest, useSecureGraph);
    }

//    public ResultSet executeQuery(Query query, SecurityRequest securityRequest, boolean useSecureGraph) {
//        return executeQueryOnGraph(query, TripleStore.DEFAULT_GRAPH, securityRequest, useSecureGraph);
////        return executeQueryOnGraph(query, "urn:x-arq:DefaultGraph", securityRequest, useSecureGraph);
//    }

    public ResultSet executeQueryOnGraph(String queryString, String graphUri, SecurityRequest securityRequest, boolean useSecureGraph) {
        return executeQueryOnGraph(QueryFactory.create(queryString, Syntax.syntaxARQ), graphUri, securityRequest, useSecureGraph);
    }

    public void executeUpdate(UpdateRequest request) {
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
        ResultSet result = null;
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
                //TODO check for querying named graphs
//                synchronized (TripleStore.class) {

                //TODO // FIXME: 02/01/2019
                Model m = dataset.getNamedModel(graphUri);
                SecuredModel securedM = Factory.getInstance(evaluator, "http://symbiote-h2020.eu/secureModel", m);

                setSecurityRequest(securityRequest);
                try (QueryExecution qe = QueryExecutionFactory.create(query, securedM)) {
                    qe.setTimeout(sparqlQueryTimeout);
                    result = ResultSetFactory.copyResults(qe.execSelect());
                }
//                }
            } else {
//            Model mm = ModelFactory.createRDFSModel(dataset.getDefaultModel());
//            Model modelToQuery = ModelFactory.createRDFSModel(dataset.getUnionModel());
//                Model modelToQuery = dataset.getUnionModel();
//                Model model = ModelFactory.createDefaultModel().read(in, "");
                Model m = dataset.getNamedModel(graphUri);

                //TODO TEST INFERENCE FROM OTHER MODELS
//                m.add(getAllDefModels());
//                m = ModelFactory.createRDFSModel(m);

                try (QueryExecution qe = QueryExecutionFactory.create(query, m)) {
//                    qe.setTimeout(sparqlQueryTimeout);
                    long in = System.currentTimeMillis();
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
//                    log.debug("finished printing in " + ( System.currentTimeMillis() - in ) + "ms");
                    //                    //TODO end commenting

//                    QuerySolution next = resultSet.next();
//                    next.varNames();
                    log.debug("Before copy results");
                    result = ResultSetFactory.copyResults(resultSet);
                    log.debug("After copy results");

                }
            }
//        }
        } finally {
            dataset.end();
        }
        return result;
    }

    public ResultSet executeQueryOnDataset(Query query, SecurityRequest securityRequest, boolean useSecureGraph) {
        dataset.begin(ReadWrite.READ);
        log.debug("Executing query on dataset");
        ResultSet result = null;
        try {
            if (useSecureGraph) {
                log.debug("Using secured graph");
                //TODO check for querying named graphs
                setSecurityRequest(securityRequest);
                try (QueryExecution qe = QueryExecutionFactory.create(query, securedModel)) {
                    qe.setTimeout(sparqlQueryTimeout);
                    log.debug("Executing on secured graph");
                    result = ResultSetFactory.copyResults(qe.execSelect());
                }
            } else {
                log.debug("Using nonsecured graph");
                try (QueryExecution qe = QueryExecutionFactory.create(query, dataset)) {
                    qe.setTimeout(sparqlQueryTimeout);
                    long in = System.currentTimeMillis();
                    log.debug("Executing on notsecured");
                    ResultSet resultSet = qe.execSelect();
                    log.debug("Before copy results");
                    result = ResultSetFactory.copyResults(resultSet);
                    log.debug("After copy results");
                }
            }
        } finally {
            dataset.end();
        }
        log.debug("Finished executing query on dataset");
        return result;
    }

    /**
     * Helper method for properly setting security request in both cases: when security is on and off
     *
     * @param securityRequest
     */
    private void setSecurityRequest(SecurityRequest securityRequest) {
        if (evaluator != null) {
            evaluator.setSecurityRequest(securityRequest);
        }
    }

    public Model getDefaultGraph() {
        dataset.begin(ReadWrite.READ);
        Model result = dataset.getDefaultModel();
        dataset.end();
        return result;
    }

    public OntModel getNamedOntModel(String uri, boolean includeImport, boolean useInference) {
        OntModel modelToReturn = null;

        long timers_begin = System.currentTimeMillis();
        dataset.begin(ReadWrite.READ);
        long timers_afterbeingread = System.currentTimeMillis();
        Model namedModel = dataset.getNamedModel(uri);
        long timers_aftergetNameModel = System.currentTimeMillis();
        try {
            modelToReturn = ModelHelper.asOntModel(namedModel, includeImport, useInference);
            long timers_afterAsOntModell = System.currentTimeMillis();
            log.debug("[Timers getNamedOntModel] waitingBegin " + (timers_afterbeingread - timers_begin) + " | gettingNamedModel " + (timers_aftergetNameModel - timers_afterbeingread )
                    + " | afterAsOntModel " + ( timers_afterAsOntModell - timers_aftergetNameModel));
        } catch (Exception e) {
            log.error("Error occurred when asOntModel: " + e.getMessage());
        }

        dataset.end();
        return modelToReturn;
    }

    public void printDataset() {
        if (SHOULD_PRINT_DATASET) {
            dataset.begin(ReadWrite.READ);
            Model result = dataset.getDefaultModel();
            result.write(System.out, "TURTLE");
            dataset.end();
        }
    }

    public void registerInformationModel(InformationModel informationModel) {
        log.info("Registering information model metadata " + informationModel.getUri());
        insertGraph(TripleStore.DEFAULT_GRAPH, getInformationModelMetadata(informationModel), RDFFormat.Turtle);
        log.info("Registering information model rdf " + informationModel.getUri());
        insertModelGraph(informationModel.getUri(), informationModel.getRdf(), informationModel.getRdfFormat());
        log.info("Finished registering model");
    }

    private String getInformationModelMetadata(InformationModel informationModel) {
        String entityUri = ModelHelper.getInformationModelURI(informationModel.getId());
        String rdf = "<" + entityUri + "> <" + RDF.type + "> <" + MIM.InformationModel + "> . " +
                "<" + entityUri + "> <" + CIM.id + "> \"" + informationModel.getId() + "\" . " +
                "<" + entityUri + "> <" + CIM.name + "> \"" + informationModel.getName() + "\" . " +
                "<" + entityUri + "> <" + MIM.hasDefinition + "> <" + informationModel.getUri() + "> . ";

        log.debug("Adding following information model metadata: " + rdf);
        return rdf;
    }

    public Model getAllDefModels() {
        Model allModels = ModelFactory.createDefaultModel();
        allModels.add(dataset.getNamedModel(QU.getURI()));
        allModels.add(dataset.getNamedModel(BIM_QU_ALIGN.getURI()));
        allModels.add(dataset.getNamedModel(BIM.getURI()));
        allModels.add(dataset.getNamedModel(BIM_PROPERTY.getURI()));

        return allModels;
    }

    //Specific delete operations checking if specific entity with id exists before hand


    public boolean executeDeleteResources(List<String> resourceIds) {
        dataset.begin(ReadWrite.WRITE);

        long in = System.currentTimeMillis();
        //Check first
        for (String resourceId : resourceIds) {
            try (QueryExecution qExec = QueryExecutionFactory.create(
                    "PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> SELECT (count(*) AS ?count) { ?s cim:id \"" + resourceId + "\"}",
                    dataset.getNamedModel(TripleStore.DEFAULT_GRAPH))) {
                ResultSet rs = qExec.execSelect();

                QuerySolution solution1 = rs.next();

                log.debug("Result of select raw: " + solution1.toString());

                int val = 0;
                try {
                    val = Integer.valueOf(StringUtils.substringBefore(solution1.get("count").toString(), "^^"));
                } catch (NumberFormatException e) {
                    log.debug("Nfe: " + StringUtils.substringBefore(solution1.get("count").toString(), "^^"));
                }
                log.debug("temporary printing select results: " + val);
                if (val == 0) {
                    log.debug("During checking for resource delete operation resource with id: " + resourceId
                            + " could not be found, check finished in " + (System.currentTimeMillis() - in) + " ms");
                    //resource does not exist, abort
                    dataset.abort();
                    return false;
                }


//                if (!rs.hasNext()) {
//                    log.debug("During checking for resource delete operation resource with id: " + resourceId + " could not be found");
//                    //resource does not exist, abort
//                    dataset.abort();
//                    return false;
//                }
            }
        }

        log.debug("Existing check done in " + (System.currentTimeMillis() - in) + " ms");

        for (String resourceId : resourceIds) {
            UpdateRequest updateRequest = new DeleteResourceRequestGenerator(resourceId).generateRequest();
            UpdateAction.execute(updateRequest, dataset);
        }
        dataset.commit();
        return true;
    }

}

