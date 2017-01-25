package eu.h2020.symbiote.query;

import java.util.List;

/**
 * A response to the query containing a list of resources fulfilling the requirements.
 *
 * Created by Mael on 24/01/2017.
 */
public class QueryResponse {

    private List<QueryResponseResource> resourceList;

    public QueryResponse() {
    }

    public QueryResponse(List<QueryResponseResource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<QueryResponseResource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<QueryResponseResource> resourceList) {
        this.resourceList = resourceList;
    }
}
