package eu.h2020.symbiote.ranking;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Szymon Mueller on 27/07/2017.
 */
@Component
public class PopularityManager {

    private PopularityRepository repository;

    @Autowired
    public PopularityManager(PopularityRepository repository) {
        this.repository = repository;
    }

    public float getPopularityForResource(String resourceId ) {


        //TODO
        return 1.0f;
    }

}
