package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.query.QueryVarName;
import eu.h2020.symbiote.query.ResourceAndObservedPropertyQueryGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.sparql.ARQException;
import org.apache.jena.sparql.resultset.ResultsFormat;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the handler for the platform related events.
 * <p>
 * Created by Mael on 11/01/2017.
 */
public class SearchHandler implements ISearchEvents {

    private static final Log log = LogFactory.getLog(SearchHandler.class);

    private final TripleStore triplestore;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param triplestore Triplestore on which the events should be executed.
     */
    public SearchHandler(TripleStore triplestore) {
        this.triplestore = triplestore;
    }


    @Override
    public QueryResponse search(CoreQueryRequest request) {
        QueryResponse response = new QueryResponse();
        try {

            QueryGenerator q = HandlerUtils.generateQueryFromSearchRequest(request);

            ResultSet results = this.triplestore.executeQuery(q.toString());

            response = HandlerUtils.generateSearchResponseFromResultSet(results);

            if (q.isMultivaluequery()) {
                response.getResources().forEach(this::searchForPropertiesOfResource);
            }

        } catch (Exception e) {
            log.error("Error occurred during search: " + e.getMessage());
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String sparqlSearch(CoreSparqlQueryRequest request) {
        String resultOfSearch = "";
        ResultSet resultSet = this.triplestore.executeQuery(request.getBody());


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
            e.printStackTrace();
        }
        return resultOfSearch;
    }

    private void searchForPropertiesOfResource(QueryResourceResult resource) {
        ResourceAndObservedPropertyQueryGenerator q = new ResourceAndObservedPropertyQueryGenerator(resource.getId());
        ResultSet resultSet = this.triplestore.executeQuery(q.toString());

        List<String> allProperties = new ArrayList<>();
        while (resultSet.hasNext()) {
            QuerySolution qs = resultSet.next();
            allProperties.add(qs.get(QueryVarName.PROPERTY_NAME).toString());
        }
        resource.setObservedProperties(allProperties);
    }

}
