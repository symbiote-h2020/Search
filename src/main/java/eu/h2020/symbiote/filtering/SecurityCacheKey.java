package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;

/**
 * Created by Szymon Mueller on 03/11/2017.
 */
public class SecurityCacheKey {

    private final SecurityRequest request;
    private final String resourceIri;

    public SecurityCacheKey(SecurityRequest request, String resourceIri) {
        this.request = request;
        this.resourceIri = resourceIri;
    }

    public SecurityRequest getRequest() {
        return request;
    }

    public String getResourceIri() {
        return resourceIri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SecurityCacheKey that = (SecurityCacheKey) o;

        if (!resourceIri.equals(that.resourceIri)) return false;
        return request.equals(that.request);
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + ((request != null?request.hashCode():0));
        result = 31 * result + ((resourceIri!=null?resourceIri.hashCode():0));
        return result;
    }
}
