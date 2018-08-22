package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdate;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    public void saveAvailabilityMessage( CloudMonitoringPlatformRequest message ) {
        if( message != null && message.getBody() != null ) {
            if( message.getBody().getMetrics() !=null ) {
                log.debug("Saving availability update, size " + message.getBody().getMetrics().size());
                for( CloudMonitoringDevice device: message.getBody().getMetrics() ) {
                    this.availabilityRepository.save(new MonitoringInfo(device.getId(),device));
                }
            }
        } else {
            log.debug("Could not save availability message, cause " + (message==null?"message is null":"message has null body"));
        }
    }

    public float getAvailabilityForResource(String resourceId ) {
        float result = 0.0f;
        Optional<MonitoringInfo> monitoringInfo = availabilityRepository.findById(resourceId);
        if( !monitoringInfo.isPresent() ) {
//            log.debug("Could not find availability for resource " + resourceId + ", setting " + result);
        } else {
            //TODO to be changed when new metrics system arrives
//            log.warn("TODO to be changed when new metrics system arrives");
//            if( monitoringInfo.get().getMonitoringDeviceInfo().getMetrics()== 1 ) {
//                log.debug("Resource " + resourceId + " available");
//                result = 1.0f;
//            } else {
//                log.warn("Resource " + resourceId + " has different availability value: " + monitoringInfo.get().getMonitoringDeviceInfo().getAvailability()  + ". Returning availability rank as a " + result);
//            }
        }

        return result;
    }

}
