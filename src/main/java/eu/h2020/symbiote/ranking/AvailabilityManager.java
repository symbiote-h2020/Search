package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Szymon Mueller on 27/07/2017.
 */
@Component
public class AvailabilityManager {

    private AvailabilityRepository availabilityRepository;

    private Log log = LogFactory.getLog(AvailabilityManager.class);

    @Autowired
    public AvailabilityManager(AvailabilityRepository availabilityRepository) {
        this.availabilityRepository = availabilityRepository;
    }

    public float getAvailabilityForResource(String resourceId ) {
        float result = 0.0f;
        CloudMonitoringDevice monitoringInfo = availabilityRepository.findOne(resourceId);
        if( monitoringInfo == null ) {
            log.debug("Could not find availability for resource " + resourceId + ", setting " + result);
        } else {
            if( monitoringInfo.getAvailability() == 1 ) {
                log.debug("Resource " + resourceId + " available");
                result = 1.0f;
            } else {
                log.warn("Resource " + resourceId + " has different availability value: " + monitoringInfo.getAvailability() + ". Returning availability rank as a " + result);
            }
        }

        return result;
    }

}
