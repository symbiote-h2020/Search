package eu.h2020.symbiote.mappings;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Szymon Mueller on 01/08/2017.
 */
@Repository
public interface MappingRepository extends MongoRepository<OntologyMappingInternal, String> {

    /**
     * Returns ontology mapping with specified id.
     *
     * @param id Id of the mapping
     * @return Mapping with specified id.
     */
    public Optional<OntologyMappingInternal> findById(String id);

    @Override
    public List<OntologyMappingInternal> findAll();

    public List<OntologyMappingInternal> findByOntologyMappingSourceModelId( String sourceModelId );
}
