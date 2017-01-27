package eu.h2020.symbiote.query;

import java.util.List;

/**
 * Single response for a query.
 *
 * Created by Mael on 24/01/2017.
 */
public class SearchResponseResource {

    private String id;
    private String name;
    private String description;
    private String platformId;
    private String platformName;
    private String locationName;
    private Double locationLatitude;
    private Double locationLongitude;
    private Double locationAltitude;
    private List<String> observedProperties;

    public SearchResponseResource() {
    }

    public SearchResponseResource(String id, String name, String description, String platformId, String platformName, String locationName, Double locationLatitude, Double locationLongitude, Double locationAltitude, List<String> observedProperties) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.platformId = platformId;
        this.platformName = platformName;
        this.locationName = locationName;
        this.locationLatitude = locationLatitude;
        this.locationLongitude = locationLongitude;
        this.locationAltitude = locationAltitude;
        this.observedProperties = observedProperties;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public Double getLocationLatitude() {
        return locationLatitude;
    }

    public void setLocationLatitude(Double locationLatitude) {
        this.locationLatitude = locationLatitude;
    }

    public Double getLocationLongitude() {
        return locationLongitude;
    }

    public void setLocationLongitude(Double locationLongitude) {
        this.locationLongitude = locationLongitude;
    }

    public Double getLocationAltitude() {
        return locationAltitude;
    }

    public void setLocationAltitude(Double locationAltitude) {
        this.locationAltitude = locationAltitude;
    }

    public List<String> getObservedProperties() {
        return observedProperties;
    }

    public void setObservedProperties(List<String> observedProperties) {
        this.observedProperties = observedProperties;
    }
}
