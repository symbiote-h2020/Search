package eu.h2020.symbiote.query;

import java.util.ArrayList;
import java.util.List;

/**
 * A response to the query containing a list of resources fulfilling the requirements.
 *
 * Created by Mael on 24/01/2017.
 */
public class SearchResponse {

    private List<SearchResponseResource> resourceList = new ArrayList<>();

    public SearchResponse() {
    }

    public SearchResponse(List<SearchResponseResource> resourceList) {
        this.resourceList = resourceList;
    }

    public List<SearchResponseResource> getResourceList() {
        return resourceList;
    }

    public void setResourceList(List<SearchResponseResource> resourceList) {
        this.resourceList = resourceList;
    }
}
