package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.filtering.SecurityManager;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the handler for the platform related events.
 * <p>
 * Created by Mael on 11/01/2017.
 */
public class SearchHandler implements ISearchEvents {

    public static final String SUCCESS_MESSAGE = "Search performed correctly";

    private static final Log log = LogFactory.getLog(SearchHandler.class);

    private final TripleStore triplestore;

    private final SecurityManager securityManager;
    private final RankingHandler rankingHandler;

    private final boolean shouldRank;
    private final boolean securityEnabled;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param triplestore Triplestore on which the events should be executed.
     */
    public SearchHandler(TripleStore triplestore, boolean securityEnabled, SecurityManager securityManager, RankingHandler rankingHandler, boolean shouldRank) {
        this.triplestore = triplestore;
        this.securityEnabled = securityEnabled;
        this.securityManager = securityManager;
        this.rankingHandler = rankingHandler;
        this.shouldRank = shouldRank;
    }


    @Override
    public QueryResponse search(SearchCommunicationHandler comm, CoreQueryRequest request) {
        QueryResponse response = new QueryResponse();
        try {
            long beforeSparql = System.currentTimeMillis();
            Map<SecurityCredentials, ValidationStatus> validatedCredentials = new HashMap<>();
            QueryGenerator q = HandlerUtils.generateQueryFromSearchRequest(request);

            long afterQGeneration = System.currentTimeMillis();

            ResultSet results = this.triplestore.executeQueryOnUnionGraph(q.toString(),request.getSecurityRequest(),false);

            long afterInitialQuery = System.currentTimeMillis();

            response = HandlerUtils.generateSearchResponseFromResultSet(results);

            long afterGeneratingResponse = System.currentTimeMillis();

            if (q.isMultivaluequery()) {
                log.debug("Searching for properties");
                    searchForPropertiesOfResources(response.getBody(), request.getSecurityRequest());

            } else {
                log.debug("Not multivalue query = not searching for properties");
            }
            long afterSparql = System.currentTimeMillis();

            //Filtering of the results
            log.debug("["+comm.getReqId()+"] Initially found " + response.getBody().size() + " resources, performing filtering" );

            long beforeCheckPolicy = System.currentTimeMillis();

            List<QueryResourceResult> resultsList = response.getBody();

            try {
                List<String> validatedIds = securityManager.checkGroupPolicies(resultsList.stream().map(QueryResourceResult::getId).collect(Collectors.toList()), request.getSecurityRequest());
                log.debug("["+comm.getReqId()+"] Got validated ids " + validatedIds.size() + " resources." );

                List<QueryResourceResult> filteredResults = resultsList.stream().filter(qresult -> validatedIds.contains(qresult.getId())).collect(Collectors.toList());

                log.debug("["+comm.getReqId()+"] Filtered results size: " + filteredResults.size());

                resultsList = filteredResults;
            } catch (Exception e ) {
                log.debug("Got error during filtering: " + e.getClass().getCanonicalName() + " | cause " + e.getCause() + " | " + e.getMessage() );
                response.setBody(new ArrayList<>());
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                response.setMessage("Error occurred during checking security policies : " + e.getMessage());
                return response;
            }


            //OLD format, now:
//            if( securityEnabled ) {
//                resultsList = response.getBody().stream().filter(res -> {
////                    log.debug("Checking policies for for res: " + res.getId() );
//                    return securityManager.checkPolicyByResourceId(res.getId(), request.getSecurityRequest(), validatedCredentials);
//                }).collect(Collectors.toList());
//            }

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

            log.debug("["+comm.getReqId()+"] [Timers] : queryGen " + (afterQGeneration - beforeSparql ) + " ms " +
                    "| InitialQ " + (afterInitialQuery - afterQGeneration ) + " ms " +
                    "| generatingResponse " + (afterGeneratingResponse - afterInitialQuery) + " ms " +
                    "| propertiesQ " + (afterSparql - afterGeneratingResponse ) + " ms " +
                    "| Checking policy: " + (afterCheckPolicy - beforeCheckPolicy ) + " ms." +
                    "| ranking " + (afterRank - beforeRank ) + " ms " +
                    "| TOTAL " + ( afterRank - beforeSparql) + " ms.");

        } catch (Exception e) {
            log.error("["+comm.getReqId()+"] Error occurred during search: " + e.getMessage());
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("Internal server error occurred during search : " + e.getMessage());
        }
        try {
            if( securityEnabled ) {
                response.setServiceResponse(securityEnabled?securityManager.generateSecurityResponse():"");
            }
        } catch (SecurityHandlerException e) {
            log.error("["+comm.getReqId()+"] Error occurred when generating security response. Setting response to empty string. Message of error: " + e.getMessage(), e);
            response.setServiceResponse("");
        }
        return response;
    }

    @Override
    public SparqlQueryResponse sparqlSearch(SearchCommunicationHandler comm, CoreSparqlQueryRequest request) {
        String resultOfSearch = "";
        SparqlQueryResponse response = new SparqlQueryResponse();

        ResultSet resultSet;

        //TODO change to true for deployment
        resultSet = this.triplestore.executeQuery(request.getBody(),request.getSecurityRequest(),true);

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
//            System.out.println(resultOfSearch);
        } catch (UnsupportedEncodingException e) {
            log.error("Error occurred when doing sparqlSearch formatting: " + e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("Error occurred when generating result output");
        }

        response.setBody(resultOfSearch);
        response.setStatus(HttpStatus.SC_OK);
        response.setMessage(SUCCESS_MESSAGE);

        if( securityEnabled ) {
            try {
                response.setServiceResponse(securityManager.generateSecurityResponse());
            } catch (SecurityHandlerException e) {
                log.error("Error occurred when generating security response. Setting response to empty string. Message of error: " + e.getMessage(), e);
                response.setServiceResponse("");
                response.setMessage("Security response could not be correctly generated");
            }
        }
        return response;
    }

    private void searchForPropertiesOfResources(List<QueryResourceResult> resources, SecurityRequest request) {

        List<String> resourceIds = resources.stream().map(q -> q.getId()).collect(Collectors.toList());

        ResourceAndObservedPropertyQueryGenerator q = new ResourceAndObservedPropertyQueryGenerator(resourceIds);
        String propertiesQuery = q.toString();

        log.debug(propertiesQuery);
        ResultSet resultSet = this.triplestore.executeQueryOnUnionGraph(propertiesQuery,request,false);

        Map<String,List<Property>> resourcesPropertiesMap = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String resourceId = qs.get(QueryVarName.VALUE).toString();
            if( !resourcesPropertiesMap.containsKey(resourceId) ) {
                resourcesPropertiesMap.put(resourceId,new ArrayList<>());
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
        resources.stream().filter( rs -> resourcesPropertiesMap.containsKey(rs.getId())).forEach(rs -> rs.setObservedProperties(resourcesPropertiesMap.get(rs.getId())));
    }

}
