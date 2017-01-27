package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.query.SearchRequest;
import eu.h2020.symbiote.query.SearchResponse;
import org.apache.jena.query.ResultSet;

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
    SearchResponse search(SearchRequest request );

}
