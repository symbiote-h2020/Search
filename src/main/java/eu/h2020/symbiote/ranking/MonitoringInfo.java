package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by Szymon Mueller on 06/11/2017.
 */
@Document(collection="monitoringInfo")
public class MonitoringInfo {

    @Id
    String id;

    CloudMonitoringDevice monitoringDeviceInfo;

    public MonitoringInfo(String id, CloudMonitoringDevice monitoringDeviceInfo) {
        this.id = id;
        this.monitoringDeviceInfo = monitoringDeviceInfo;
    }

    public String getId() {
        return id;
    }

    public CloudMonitoringDevice getMonitoringDeviceInfo() {
        return monitoringDeviceInfo;
    }

}
