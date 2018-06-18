package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.core.internal.popularity.PopularityUpdate;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Popularity manager responsible for handling popularity related tasks. Allows storing new popularity update
 * messages in the related repository. Also allows returning of the
 *
 * Created by Szymon Mueller on 27/07/2017.
 */
@Component
public class PopularityManager {

    private Log log = LogFactory.getLog(PopularityManager.class);

    private PopularityRepository popularityRepository;

    @Autowired
    public PopularityManager(PopularityRepository repository) {
        this.popularityRepository = repository;
    }

    public void savePopularityMessage( PopularityUpdatesMessage message ) {
        if( message != null && message.getPopularityUpdateList() != null ) {
//            log.debug("Saving " + message.getPopularityUpdateList().size() + " popularity updates" );
//            int i = 0;
            for( PopularityUpdate update: message.getPopularityUpdateList() ) {
//                log.debug("["+i+++"] " + update.getId()  + " : " + update.getViewsInDefinedInterval() );
//                PopularityObject popularityObject = new PopularityObject();
//                popularityObject.setUpdate(update);
                popularityRepository.save( update );
            }
        }
    }

    public Integer getPopularityForResource(String resourceId ) {
//        log.debug("Popularity requested for " + resourceId);

        Integer result = Integer.valueOf(0);

        Optional<PopularityUpdate> popularity = popularityRepository.findById(resourceId);

        if( popularity.isPresent()) {
            result = popularity.get().getViewsInDefinedInterval();
        } else {
//            log.warn("Null popularities found for " + resourceId );
        }

        return result;
    }

}
