package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.core.internal.popularity.PopularityUpdate;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by Szymon Mueller on 01/08/2017.
 */
@Repository
public interface PopularityRepository extends MongoRepository<PopularityUpdate, String> {

//    List<PopularityUpdate> findById(String id);

}
