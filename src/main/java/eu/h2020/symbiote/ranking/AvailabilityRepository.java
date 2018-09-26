package eu.h2020.symbiote.ranking;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Szymon Mueller on 01/08/2017.
 */
@Repository
public interface AvailabilityRepository extends MongoRepository<MonitoringInfo,String> {

    /**
     * This method will find a Resource instance in the database by
     * its Id.
     *
     * @param id            the id of the resource
     * @return              the Resource instance
     */
    public Optional<MonitoringInfo> findById(String id);


}
