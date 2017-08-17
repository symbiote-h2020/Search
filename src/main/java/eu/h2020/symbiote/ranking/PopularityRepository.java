package eu.h2020.symbiote.ranking;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Szymon Mueller on 01/08/2017.
 */
@Repository
public interface PopularityRepository extends MongoRepository<PopularityObject, String> {

}
