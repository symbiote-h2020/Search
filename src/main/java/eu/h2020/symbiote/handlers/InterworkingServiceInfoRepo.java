package eu.h2020.symbiote.handlers;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Created by Szymon Mueller on 23/06/2018.
 */
@Repository
public interface InterworkingServiceInfoRepo extends MongoRepository<InterworkingServiceInfo, String> {

    public Optional<InterworkingServiceInfo> findByInterworkingServiceURL( String interworkingServiceURL );

    public void deleteByPlatformId( String platformId );

}
