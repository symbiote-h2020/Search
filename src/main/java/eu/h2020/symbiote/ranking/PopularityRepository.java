package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Szymon Mueller on 01/08/2017.
 */
@Repository
public interface PopularityRepository extends MongoRepository<String,String> {

}
