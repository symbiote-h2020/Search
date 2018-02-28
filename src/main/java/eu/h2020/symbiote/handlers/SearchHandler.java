package eu.h2020.symbiote.handlers;

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
    public QueryResponse search(CoreQueryRequest request) {
        QueryResponse response = new QueryResponse();
        try {
            long beforeSparql = System.currentTimeMillis();
            Map<SecurityCredentials, ValidationStatus> validatedCredentials = new HashMap<>();
            QueryGenerator q = HandlerUtils.generateQueryFromSearchRequest(request);

            ResultSet results = this.triplestore.executeQuery(q.toString(),request.getSecurityRequest(),false);

            response = HandlerUtils.generateSearchResponseFromResultSet(results);

            if (q.isMultivaluequery()) {
                    searchForPropertiesOfResources(response.getBody(), request.getSecurityRequest());
            }
            long afterSparql = System.currentTimeMillis();

            //Filtering of the results
            log.debug("Initially found " + response.getBody().size() + " resources, performing filtering" );

            long beforeCheckPolicy = System.currentTimeMillis();

            List<QueryResourceResult> filteredResults = response.getBody().stream().filter(res -> {
//                    log.debug("Checking policies for for res: " + res.getId() );
                    return securityManager.checkPolicyByResourceId(res.getId(), request.getSecurityRequest(),validatedCredentials);
            }).collect(Collectors.toList());

            long afterCheckPolicy = System.currentTimeMillis();

            log.debug("After filtering got " + filteredResults.size() + " results");
            log.debug("[Timers] Sparql: " + (afterSparql - beforeSparql ) + " ms | Checking policy: " + (afterCheckPolicy - beforeCheckPolicy ) + " ms." );

            response.setBody(filteredResults);
            response.setStatus(HttpStatus.SC_OK);
            response.setMessage(SUCCESS_MESSAGE);

            if( shouldRank ) {
                log.debug("Generating ranking for response...");
                RankingQuery rankingQuery = new RankingQuery(response);
                rankingQuery.setIncludeDistance(HandlerUtils.isDistanceQuery(request));
                response = rankingHandler.generateRanking(rankingQuery);
            }

        } catch (Exception e) {
            log.error("Error occurred during search: " + e.getMessage());
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("Internal server error occurred during search : " + e.getMessage());
        }
        try {
            if( securityEnabled ) {
                response.setServiceResponse(securityEnabled?securityManager.generateSecurityResponse():"");
            }
        } catch (SecurityHandlerException e) {
            log.error("Error occurred when generating security response. Setting response to empty string. Message of error: " + e.getMessage(), e);
            response.setServiceResponse("");
        }
        return response;
    }

    @Override
    public SparqlQueryResponse sparqlSearch(CoreSparqlQueryRequest request) {
        String resultOfSearch = "";
        SparqlQueryResponse response = new SparqlQueryResponse();

        ResultSet resultSet = this.triplestore.executeQuery(request.getBody(),request.getSecurityRequest(),true);


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

        response.setBody(resultOfSearch);
        response.setStatus(HttpStatus.SC_OK);
        response.setMessage(SUCCESS_MESSAGE);

        try {
            response.setServiceResponse(securityManager.generateSecurityResponse());
        } catch (SecurityHandlerException e) {
            log.error("Error occurred when generating security response. Setting response to empty string. Message of error: " + e.getMessage(), e);
            response.setServiceResponse("");
            response.setMessage("Security response could not be correctly generated");
        }
        return response;
    }

    private void searchForPropertiesOfResources(List<QueryResourceResult> resources, SecurityRequest request) {

        List<String> resourceIds = resources.stream().map(q -> q.getId()).collect(Collectors.toList());

        ResourceAndObservedPropertyQueryGenerator q = new ResourceAndObservedPropertyQueryGenerator(resourceIds);
        ResultSet resultSet = this.triplestore.executeQuery(q.toString(),request,true);

        Map<String,List<Property>> resourcesPropertiesMap = new HashMap<>();

        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            String resourceId = qs.get(QueryVarName.VALUE).toString();
            if( !resourcesPropertiesMap.containsKey(resourceId) ) {
                resourcesPropertiesMap.put(resourceId,new ArrayList<>());
            }
            log.debug("Adding property " + qs.get(QueryVarName.PROPERTY_NAME).toString() + " for res " + resourceId );
            List<Property> propertiesList = resourcesPropertiesMap.get(resourceId);
            Property prop = new Property(qs.get(QueryVarName.PROPERTY_NAME).toString(),qs.get(QueryVarName.PROPERTY_IRI).toString(), Arrays.asList(qs.get(QueryVarName.PROPERTY_DESC).toString()));
            propertiesList.add(prop);
        }

        //Setting properties
        resources.stream().filter( rs -> resourcesPropertiesMap.containsKey(rs.getId())).forEach(rs -> rs.setObservedProperties(resourcesPropertiesMap.get(rs.getId())));
    }

}
