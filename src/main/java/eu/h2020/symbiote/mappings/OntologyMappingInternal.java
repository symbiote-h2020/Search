package eu.h2020.symbiote.mappings;

import eu.h2020.symbiote.model.mim.OntologyMapping;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Szymon Mueller on 24/09/2018.
 */
@Document
public class OntologyMappingInternal {

    @Id
    private String id;

    private OntologyMapping ontologyMapping;

    public OntologyMappingInternal() {
    }

    public OntologyMappingInternal(String id, OntologyMapping ontologyMapping) {
        this.id = id;
        this.ontologyMapping = ontologyMapping;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public OntologyMapping getOntologyMapping() {
        return ontologyMapping;
    }

    public void setOntologyMapping(OntologyMapping ontologyMapping) {
        this.ontologyMapping = ontologyMapping;
    }
}
