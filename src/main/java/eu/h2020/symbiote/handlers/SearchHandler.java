package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.*;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the handler for the platform related events.
 *
 * Created by Mael on 11/01/2017.
 */
public class SearchHandler implements ISearchEvents{

    private static final Log log = LogFactory.getLog(SearchHandler.class);

    private final TripleStore triplestore;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param triplestore Triplestore on which the events should be executed.
     */
    public SearchHandler(TripleStore triplestore ) {
        this.triplestore = triplestore;
    }


    @Override
    public SearchResponse search(SearchRequest request) {
        SearchResponse response = null;

        QueryGenerator q = HandlerUtils.generateQueryFromSearchRequest(request);

        ResultSet results = this.triplestore.executeQuery(q.toString());

        response = HandlerUtils.generateSearchResponseFromResultSet(results);

        if( q.isMultivaluequery() ) {
            response.getResourceList().forEach(this::searchForPropertiesOfResource);
        }

        return response;
    }


    private void searchForPropertiesOfResource(SearchResponseResource resource) {
        ResourceAndObservedPropertyQueryGenerator q = new ResourceAndObservedPropertyQueryGenerator(resource.getId());
        ResultSet resultSet = this.triplestore.executeQuery(q.toString());

        List<String> allProperties = new ArrayList<>();
        while( resultSet.hasNext() ) {
            QuerySolution qs = resultSet.next();
            allProperties.add( qs.get(QueryVarName.PROPERTY_NAME).toString() );
        }
        resource.setObservedProperties( allProperties );
    }

}
