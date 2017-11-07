package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.core.internal.popularity.PopularityUpdate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Created by Szymon Mueller on 01/08/2017.
 */
@Repository
public interface PopularityRepository extends MongoRepository<PopularityUpdate, String> {

    /**
     * Returns latest Popularity Update stored for a resource with specified id.
     *
     * @param id Id of the resource
     * @return Popularity of the resource.
     */
    public Optional<PopularityUpdate> findById(String id);

}
