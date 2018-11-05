package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Interface for handling search events.
 *
 * Created by Mael on 25/01/2017.
 */
public interface ISearchEvents {

    /**
     * Performs search in the module.
     *
     * @param comm Handler which dispatches response to the client
     * @param request Request containing parameters of the search
     * @return Response containing list of found resources.
     */
    QueryResponse search(SearchCommunicationHandler comm, CoreQueryRequest request );

    /**
     * Performs sparql search in the module.
     *
     * @param comm Handler which dispatches response to the client
     * @param request Request containing SPARQL.
     * @return Response in format specified in the request.
     */
    SparqlQueryResponse sparqlSearch(SearchCommunicationHandler comm,CoreSparqlQueryRequest request );


}
