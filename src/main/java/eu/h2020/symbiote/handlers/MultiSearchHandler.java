package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.mappings.MappingManager;
import eu.h2020.symbiote.mappings.MappingRepository;
import eu.h2020.symbiote.mappings.OntologyMappingInternal;
import eu.h2020.symbiote.model.cim.Property;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.query.QueryVarName;
import eu.h2020.symbiote.query.ResourceAndObservedPropertyQueryGenerator;
import eu.h2020.symbiote.ranking.RankingHandler;
import eu.h2020.symbiote.ranking.RankingQuery;
import eu.h2020.symbiote.security.commons.enums.ValidationStatus;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
import eu.h2020.symbiote.security.communication.payloads.SecurityCredentials;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import eu.h2020.symbiote.semantics.mapping.model.Mapping;
import eu.h2020.symbiote.semantics.mapping.model.MappingConfig;
import eu.h2020.symbiote.semantics.mapping.model.UnsupportedMappingException;
import eu.h2020.symbiote.semantics.mapping.parser.ParseException;
import eu.h2020.symbiote.semantics.mapping.sparql.SparqlMapper;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.jena.iri.IRI;
import org.apache.jena.iri.IRIFactory;
import org.apache.jena.ontology.Individual;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Implementation of the handler for the platform related events.
 * <p>
 * Created by Mael on 11/01/2017.
 */
public class MultiSearchHandler implements ISearchEvents {

    public static final String SUCCESS_MESSAGE = "Search performed correctly";

    private static final Log log = LogFactory.getLog(MultiSearchHandler.class);

    private final TripleStore triplestore;

    private final SecurityManager securityManager;
    private final RankingHandler rankingHandler;

    private final boolean shouldRank;
    private final boolean securityEnabled;

    private final int executorCoreThreads;
    private final int executorMaxThreads;
    private final int executorKeepAliveInMinutes;

    private final ThreadPoolExecutor executorService;
    private final MappingManager mappingManager;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param triplestore Triplestore on which the events should be executed.
     */
    public MultiSearchHandler(TripleStore triplestore, boolean securityEnabled, SecurityManager securityManager, RankingHandler rankingHandler,
                              MappingManager mappingManager, boolean shouldRank,
                              int executorCoreThreads, int executorMaxThreads, int executorKeepAliveInMinutes) {
        this.triplestore = triplestore;
        this.securityEnabled = securityEnabled;
        this.securityManager = securityManager;
        this.rankingHandler = rankingHandler;
        this.mappingManager = mappingManager;
        this.shouldRank = shouldRank;

        this.executorCoreThreads = executorCoreThreads;
        this.executorMaxThreads = executorMaxThreads;
        this.executorKeepAliveInMinutes = executorKeepAliveInMinutes;

        this.executorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(executorCoreThreads);
        executorService.setMaximumPoolSize(executorMaxThreads);
        executorService.setKeepAliveTime(executorKeepAliveInMinutes, TimeUnit.MINUTES);
        executorService.setRejectedExecutionHandler(new RejectedHandler());
    }


    @Override
    public QueryResponse search(SearchCommunicationHandler comm, CoreQueryRequest request) {
        log.debug("["+ comm.getReqId() +"] Scheduling search req" );
        executorService.submit(new CallableSearch(comm, request, triplestore));
        return new QueryResponse();
    }

    @Override
    public SparqlQueryResponse sparqlSearch(SearchCommunicationHandler comm, CoreSparqlQueryRequest request) {
        log.debug("["+ comm.getReqId() +"] Scheduling sparql search req" );
        executorService.submit(new CallableSparqlSearch(comm, request, triplestore));
        return new SparqlQueryResponse();
    }

    private void searchForPropertiesOfResources(List<QueryResourceResult> resources, SecurityRequest request) {

        List<String> resourceIds = resources.stream().map(q -> q.getId()).collect(Collectors.toList());

        ResourceAndObservedPropertyQueryGenerator q = new ResourceAndObservedPropertyQueryGenerator(resourceIds);

//        ResultSet resultSet = this.triplestore.executeQueryOnUnionGraph(q.toString(), request, false);
        ResultSet resultSet = this.triplestore.executeQueryOnGraph(q.toString(), TripleStore.UNION_GRAPH, request, false);

        Map<String, List<Property>> resourcesPropertiesMap = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String resourceId = qs.get(QueryVarName.VALUE).toString();
            if (!resourcesPropertiesMap.containsKey(resourceId)) {
                resourcesPropertiesMap.put(resourceId, new ArrayList<>());
            }
//            log.debug("Adding property " + qs.get(QueryVarName.PROPERTY_NAME).toString() + " for res " + resourceId );
            List<Property> propertiesList = resourcesPropertiesMap.get(resourceId);
            String propName = qs.get(QueryVarName.PROPERTY_NAME)!=null?qs.get(QueryVarName.PROPERTY_NAME).toString():"N/A";
            String propIri = qs.get(QueryVarName.PROPERTY_IRI)!=null?qs.get(QueryVarName.PROPERTY_IRI).toString():"N/A";
            String propDesc = qs.get(QueryVarName.PROPERTY_DESC)!=null?qs.get(QueryVarName.PROPERTY_DESC).toString():"N/A";
            Property prop = new Property(propName,propIri, Arrays.asList(propDesc));
            propertiesList.add(prop);
        }

        //Setting properties
        resources.stream().filter(rs -> resourcesPropertiesMap.containsKey(rs.getId())).forEach(rs -> rs.setObservedProperties(resourcesPropertiesMap.get(rs.getId())));
    }

    private class RejectedHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            log.debug("Rejected runnable r " + r.toString());
        }
    }

    private class CallableSearch implements Callable<QueryResponse> {

        private final CoreQueryRequest request;
        private final TripleStore triplestore;
        private final SearchCommunicationHandler comm;

        public CallableSearch(SearchCommunicationHandler searchCommunicationhandler, CoreQueryRequest request, TripleStore triplestore) {
            this.comm = searchCommunicationhandler;
            this.request = request;
            this.triplestore = triplestore;
        }

        @Override
        public QueryResponse call() throws Exception {
            QueryResponse response = new QueryResponse();
            try {
                long beforeSparql = System.currentTimeMillis();
                Map<SecurityCredentials, ValidationStatus> validatedCredentials = new HashMap<>();
                QueryGenerator q = HandlerUtils.generateQueryFromSearchRequest(request);

                long afterQGeneration = System.currentTimeMillis();

//                ResultSet results = this.triplestore.executeQueryOnUnionGraph(q.toString(), request.getSecurityRequest(), false);
                ResultSet results = this.triplestore.executeQueryOnGraph(q.toString(), TripleStore.UNION_GRAPH, request.getSecurityRequest(), false);

                long afterInitialQuery = System.currentTimeMillis();

                response = HandlerUtils.generateSearchResponseFromResultSet(results);

                long afterGeneratingResponse = System.currentTimeMillis();

                if (q.isMultivaluequery()) {
                    searchForPropertiesOfResources(response.getBody(), request.getSecurityRequest());
                }
                long afterSparql = System.currentTimeMillis();

                //Filtering of the results
                log.debug("["+comm.getReqId()+"] Initially found " + response.getBody().size() + " resources, performing filtering");

                long beforeCheckPolicy = System.currentTimeMillis();

                List<QueryResourceResult> resultsList = response.getBody();

                List<String> validatedIds = securityManager.checkGroupPolicies(resultsList.stream().map(QueryResourceResult::getId).collect(Collectors.toList()), request.getSecurityRequest());

                List<QueryResourceResult> filteredResults = resultsList.stream().filter(qresult -> validatedIds.contains(qresult.getId())).collect(Collectors.toList());

                log.debug("["+comm.getReqId()+"] Filtered results size: " + filteredResults.size());

                resultsList = filteredResults;

                long afterCheckPolicy = System.currentTimeMillis();

                log.debug("["+comm.getReqId()+"] After filtering got " + resultsList.size() + " results");

                response.setBody(resultsList);
                response.setStatus(HttpStatus.SC_OK);
                response.setMessage(SUCCESS_MESSAGE);

                long beforeRank = System.currentTimeMillis();

                if( shouldRank && request.getShould_rank() !=null && request.getShould_rank() ) {
                    log.debug("["+comm.getReqId()+"] Generating ranking for response...");
                    RankingQuery rankingQuery = new RankingQuery(response);
                    rankingQuery.setIncludeDistance(HandlerUtils.isDistanceQuery(request));
                    response = rankingHandler.generateRanking(rankingQuery);
                }
                long afterRank = System.currentTimeMillis();

                log.debug("["+comm.getReqId()+"] [Timers] : queryGen " + (afterQGeneration - beforeSparql) + " ms " +
                        "| InitialQ " + (afterInitialQuery - afterQGeneration) + " ms " +
                        "| generatingResponse " + (afterGeneratingResponse - afterInitialQuery) + " ms " +
                        "| propertiesQ " + (afterSparql - afterGeneratingResponse) + " ms " +
                        "| Checking policy: " + (afterCheckPolicy - beforeCheckPolicy) + " ms." +
                        "| ranking " + (afterRank - beforeRank) + " ms " +
                        "| TOTAL " + (afterRank - beforeSparql) + " ms.");

            } catch (Exception e) {
                log.error("["+comm.getReqId()+"] Error occurred during search: " + e.getMessage());
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                response.setMessage("["+comm.getReqId()+"] Internal server error occurred during search : " + e.getMessage());
            }
            try {
                if (securityEnabled) {
                    response.setServiceResponse(securityEnabled ? securityManager.generateSecurityResponse() : "");
                }
            } catch (SecurityHandlerException e) {
                log.error("["+comm.getReqId()+"] Error occurred when generating security response. Setting response to empty string. Message of error: " + e.getMessage(), e);
                response.setServiceResponse("");
            }

            //Sending response
            comm.sendResponse(response);

            return response;
        }
    }

    private class CallableSparqlSearch implements Callable<SparqlQueryResponse> {

        private final CoreSparqlQueryRequest request;
        private final TripleStore triplestore;
        private final SearchCommunicationHandler comm;

        public CallableSparqlSearch(SearchCommunicationHandler searchCommunicationhandler, CoreSparqlQueryRequest request, TripleStore triplestore) {
            this.comm = searchCommunicationhandler;
            this.request = request;
            this.triplestore = triplestore;
        }

        @Override
        public SparqlQueryResponse call() throws Exception {

            log.debug("Callable sparql search execution");
            String resultOfSearch = "";
            SparqlQueryResponse response = new SparqlQueryResponse();

            if( request.getBaseModel() == null || StringUtils.isEmpty(request.getBaseModel()) ) {
                log.debug("Executing sparql");
                resultOfSearch = runSparqlQuery( response );
            } else {
                //Execute mapping queries
                log.debug("Executing sparql rewriting query -> nyi");
                resultOfSearch = runSparqlRewrittingQuery( response );
            }

            log.debug("Got response from sparql query: " + StringUtils.substring(resultOfSearch,0,10000) + " ....");

            response.setBody(resultOfSearch);
            response.setStatus(HttpStatus.SC_OK);
            response.setMessage(SUCCESS_MESSAGE);

            if (securityEnabled) {
                try {
                    response.setServiceResponse(securityManager.generateSecurityResponse());
                } catch (SecurityHandlerException e) {
                    log.error("Error occurred when generating security response. Setting response to empty string. Message of error: " + e.getMessage(), e);
                    response.setServiceResponse("");
                    response.setMessage("Security response could not be correctly generated");
                }
            }

            comm.sendResponse(response);
            return response;
        }

        private String runSparqlQuery( SparqlQueryResponse response ) {
            String resultOfSearch = null;

            log.debug("Running sparql query");
            ResultSet resultSet = this.triplestore.executeQuery(request.getBody(), request.getSecurityRequest(), false);


            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            String formatString = request.getOutputFormat().toString();
            ResultsFormat format = ResultsFormat.lookup(formatString);
            try {
                ResultSetFormatter.output(stream, resultSet, format);
            } catch (ARQException e) {
                log.warn("Got unsupported format exception, switching to text output format... " + e.getMessage());
                //use default formatter in case of unsupported format/other errors
                ResultSetFormatter.out(stream, resultSet);
            }
            try {
                resultOfSearch = stream.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Error occurred when doing sparqlSearch formatting: " + e.getMessage(), e);
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                response.setMessage("Error occurred when generating result output");
            }


            return resultOfSearch;
        }

        private String runSparqlRewrittingQuery( SparqlQueryResponse response ) {
            String resultOfSearch = null;

            log.debug( "Running sparql query with rewritting");

            //TODO default or union

            Query initialQuery = QueryFactory.create(request.getBody(), Syntax.syntaxARQ);

            List<OntologyMappingInternal> mappingsFromSource = new ArrayList<>();

            String sourceModelId = request.getBaseModel();
//            String sourceModelId = "model1";
            if( request.getBaseModel() != null && !request.getBaseModel().startsWith("http")) {
                //Assume its not an iri of the model
                mappingsFromSource.addAll(mappingManager.findByOntologyMappingSourceModelId(sourceModelId));
            }

            if( mappingsFromSource.size() == 0 ) {
                //Could not find models by threating sourceModel as ID - trying as an IRI
                //TODO check if need import/inference
                Set<Individual> infoModels = this.triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH,false,false).listIndividuals(MIM.InformationModel).toSet();
                List<String> rightInfoModelIds = infoModels.stream().filter(infoModelIndividual -> {
//                    infoModelIndividual.getURI().equals(sourceModelId);
//                    log.debug("Before checking for info models defitnions " + infoModelIndividual.getURI() );
//                    log.debug("-res " + infoModelIndividual.getProperty(MIM.hasDefinition).getResource().toString());
//                    log.debug("-pred " + infoModelIndividual.getProperty(MIM.hasDefinition).getPredicate().toString());
//                    log.debug("-obj " + infoModelIndividual.getProperty(MIM.hasDefinition).getObject().toString());
//                    log.debug("propValue " + infoModelIndividual.getPropertyValue(MIM.hasDefinition).toString());
                    String modelIri = infoModelIndividual.getProperty(MIM.hasDefinition).getObject().asResource().toString();


//                    Resource definition = this.triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH).getResource(infoModelIndividual.getURI()).getProperty(MIM.hasDefinition).getResource();
                    log.debug("Checking if initial definition " + sourceModelId + " equals either " + infoModelIndividual.getURI() + " or " + modelIri);
                    return infoModelIndividual.getURI().equals(sourceModelId) || modelIri.equals(sourceModelId);
                }).map( infoModelIndividual -> {
                    log.debug("Before checking mapping: " +infoModelIndividual.getURI() );
                    //TODO check if need import/inference
                    Statement property = this.triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH,false,false).getResource(infoModelIndividual.getURI()).getProperty(CIM.id);
                    log.debug("Mapping: " + property.toString() );
                    try {
                        String s = property.getLiteral().toString();
                        log.debug("For " + infoModelIndividual.getURI() + " got: " + s );
                        return s;
                    } catch( Exception e ) {
                        log.error("Got error: " + e.getMessage(),e);
                        return null;
                    }
                }).collect(Collectors.toList());

                log.debug("Found " + rightInfoModelIds.size() + " infoModelIds by using " + sourceModelId + " as an IRI");

                if( rightInfoModelIds.size() == 1) {
                    log.debug("Checking db for info model with id: " + rightInfoModelIds.get(0));
                    mappingsFromSource.addAll(mappingManager.findByOntologyMappingSourceModelId(rightInfoModelIds.get(0)));
                } else {
                    log.debug("Size is wrong");
                }

//                Resource modelGraph = storage.getNamedGraphAsOntModel(TripleStore.DEFAULT_GRAPH).getResource(pimIndividual.getURI()).getProperty(MIM.hasDefinition).getResource();
//                log.debug("Found graphUri: " + modelGraph.getURI());
//                pimGraphUri = modelGraph.getURI();

            }



            if( mappingsFromSource == null ) {
                log.error( "Null mappings found for source model with id " + sourceModelId);
            }
            log.debug( "Found " + mappingsFromSource.size() + " mappings for model with id " + sourceModelId );

            List<ResultSet> results = new ArrayList<>();

            //Run original query
            results.add(this.triplestore.executeQueryOnDataset(initialQuery, request.getSecurityRequest(), false));

            for ( OntologyMappingInternal mappingInternal: mappingsFromSource ) {
                log.debug("Using mapping " + mappingInternal.getId() + " from model "+ mappingInternal.getOntologyMapping().getSourceModelId() + " to " + mappingInternal.getOntologyMapping().getDestinationModelId() );
                SparqlMapper sparqlMapper = new SparqlMapper();
                MappingConfig config = new MappingConfig.Builder().build();
                log.debug("Config for mapping built, now parsing mapping definition:");
                try {
                    log.debug(mappingInternal.getOntologyMapping().getDefinition());
                    Mapping mapping = Mapping.parse(mappingInternal.getOntologyMapping().getDefinition());

                    log.debug("Mapping parsed successfully, got " + (mapping.getMappingRules()!= null?mapping.getMappingRules().size() + " mapping rullings":"null mapping rullings"));

                    Query mappedQuery = sparqlMapper.map(initialQuery, mapping, config);

                    log.debug("Got mapped query: " + mappedQuery.toString() );

                    //Run the mapped query
                    ResultSet resultSet = this.triplestore.executeQueryOnDataset(mappedQuery, request.getSecurityRequest(), false);
                    log.debug("Mapped query ran successfully" );

                    //Add results of mapped queries to result set
                    results.add(resultSet);

                } catch (ParseException e) {
                    log.error("Could not parse mapping for mappingId : " + mappingInternal.getId() + " msg: " + e.getMessage(), e );
                } catch (UnsupportedMappingException e) {
                    log.error("Error occurred when creating mapped query : " + e.getMessage(),e);
                } catch (Exception e ) {
                    log.error("Unspecified error occurred: " + e.getMessage(), e);
                }


            }

            ResultSetMem completeResults = new ResultSetMem(results.toArray(new ResultSet[results.size()]));

//            ResultSet resultSet = this.triplestore.executeQuery(request.getBody(), request.getSecurityRequest(), false);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            String formatString = request.getOutputFormat().toString();
            ResultsFormat format = ResultsFormat.lookup(formatString);
            try {
                ResultSetFormatter.output(stream, completeResults, format);
            } catch (ARQException e) {
                log.warn("Got unsupported format exception, switching to text output format... " + e.getMessage());
                //use default formatter in case of unsupported format/other errors
                ResultSetFormatter.out(stream, completeResults);
            }
            try {
                resultOfSearch = stream.toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Error occurred when doing sparqlSearch formatting: " + e.getMessage(), e);
                response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                response.setMessage("Error occurred when generating result output");
            }


            return resultOfSearch;

        }
    }

}
