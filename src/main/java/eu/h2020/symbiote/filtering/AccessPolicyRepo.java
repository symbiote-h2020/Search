package eu.h2020.symbiote.filtering;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Szymon Mueller on 10/10/2017.
 */
@Repository
public interface AccessPolicyRepo extends MongoRepository<AccessPolicy,String> {

    /**
     * This method will find an AccessPolicy in the database
     * by its resource id.
     *
     * @param resourceId    the id of the resource
     * @return              the AccessPolicy instances
     */
    public Optional<AccessPolicy> findById(String resourceId);

    /**
     * This method will find an AccessPolicy in the database
     * by its resource's iri.
     *
     * @param iri    the iri of the resource
     * @return       the AccessPolicy instances
     */
    public Optional<AccessPolicy> findByIri(String iri);

}
