package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.query.QueryVarName;
import eu.h2020.symbiote.query.ResourceAndObservedPropertyQueryGenerator;
import eu.h2020.symbiote.ranking.RankingHandler;
import eu.h2020.symbiote.ranking.RankingQuery;
import eu.h2020.symbiote.security.commons.exceptions.custom.SecurityHandlerException;
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
import java.util.ArrayList;
import java.util.List;
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

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param triplestore Triplestore on which the events should be executed.
     */
    public SearchHandler(TripleStore triplestore, SecurityManager securityManager, RankingHandler rankingHandler, boolean shouldRank) {
        this.triplestore = triplestore;
        this.securityManager = securityManager;
        this.rankingHandler = rankingHandler;
        this.shouldRank = shouldRank;
    }


    @Override
    public QueryResponse search(CoreQueryRequest request) {
        QueryResponse response = new QueryResponse();
        try {

            QueryGenerator q = HandlerUtils.generateQueryFromSearchRequest(request);

            ResultSet results = this.triplestore.executeQuery(q.toString(),request.getSecurityRequest(),false);

            response = HandlerUtils.generateSearchResponseFromResultSet(results);

            if (q.isMultivaluequery()) {
                response.getBody().forEach(this::searchForPropertiesOfResource);
            }

            //Filtering of the results
            log.debug("Initially found " + response.getBody().size() + " resources, performing filtering" );



            List<QueryResourceResult> filteredResults = response.getBody().stream().filter(res -> {
                    log.debug("Checking policies for for res: " + res.getId());
                    return securityManager.checkPolicyByResourceId(res.getId(), request.getSecurityRequest());
            }).collect(Collectors.toList());

            log.debug("After filtering got " + filteredResults.size() + " results");

            response.setBody(filteredResults);
            response.setStatus(HttpStatus.SC_OK);
            response.setMessage(SUCCESS_MESSAGE);

            if( shouldRank ) {
                log.debug("Generating ranking for response...");
                response = rankingHandler.generateRanking(new RankingQuery(response));
            }

        } catch (Exception e) {
            log.error("Error occurred during search: " + e.getMessage());
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.setMessage("Internal server error occurred during search : " + e.getMessage());
        }
        try {
            response.setServiceResponse(securityManager.generateSecurityResponse());
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

    private void searchForPropertiesOfResource(QueryResourceResult resource) {
        ResourceAndObservedPropertyQueryGenerator q = new ResourceAndObservedPropertyQueryGenerator(resource.getId());
        ResultSet resultSet = this.triplestore.executeQuery(q.toString(),null,false);

        List<String> allProperties = new ArrayList<>();
        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            allProperties.add(qs.get(QueryVarName.PROPERTY_NAME).toString());
        }
        resource.setObservedProperties(allProperties);
    }

}
