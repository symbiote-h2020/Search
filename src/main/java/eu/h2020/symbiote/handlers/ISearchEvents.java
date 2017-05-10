package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;

/**
 * Interface for handling search events.
 *
 * Created by Mael on 25/01/2017.
 */
public interface ISearchEvents {

    /**
     * Performs search in the module.
     *
     * @param request Request containing parameters of the search
     * @return Response containing list of found resources.
     */
    QueryResponse search(CoreQueryRequest request );

    /**
     * Performs sparql search in the module.
     *
     * @param request Request containing SPARQL.
     * @return Response in format specified in the request.
     */
    String sparqlSearch(CoreSparqlQueryRequest request );

}
