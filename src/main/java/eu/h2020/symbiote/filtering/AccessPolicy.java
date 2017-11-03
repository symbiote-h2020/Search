package eu.h2020.symbiote.filtering;

import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="policies")
public class AccessPolicy {
    @Id
    private final String id;
    private final String iri;
    private final IAccessPolicy policy;
    
    public AccessPolicy() {
        id = "";
        iri = "";
        policy = null;
    }
    
    public AccessPolicy(String resourceId, String iri, IAccessPolicy policy) {
        this.id = resourceId;
        this.iri = iri;
        this.policy = policy;
    }

    public String getResourceId() {
        return id;
    }

    public String getIri() {
        return iri;
    }

    public IAccessPolicy getPolicy() {
        return policy;
    }
}
