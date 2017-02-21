package eu.h2020.symbiote.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

import java.util.ArrayList;
import java.util.List;

/**
 * Class representing resource object in the system events.
 *
 * Created by Mael on 16/01/2017.
 */
public class Resource {
    private String id = null;
    private String name = null;
    private String owner = null;
    private String description = null;
    private List<String> observedProperties = new ArrayList<String>();
    private String resourceURL = null;
    private Location location = null;
    private String featureOfInterest = null;
    private String platformId = null;

    public Resource() {
    }

    public Resource(String id, String name, String owner, String description, List<String> observedProperties, String resourceURL, Location location, String featureOfInterest, String platformId ) {
        this.id = id;
        this.name = name;
        this.owner = owner;
        this.description = description;
        this.observedProperties = observedProperties;
        this.resourceURL = resourceURL;
        this.location = location;
        this.featureOfInterest = featureOfInterest;
        this.platformId = platformId;
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

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getObservedProperties() {
        return observedProperties;
    }

    public void setObservedProperties(List<String> observedProperties) {
        this.observedProperties = observedProperties;
    }

    public String getResourceURL() {
        return resourceURL;
    }

    public void setResourceURL(String resourceURL) {
        this.resourceURL = resourceURL;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getFeatureOfInterest() {
        return featureOfInterest;
    }

    public void setFeatureOfInterest(String featureOfInterest) {
        this.featureOfInterest = featureOfInterest;
    }

    public String getPlatformId() {
        return platformId;
    }

    public void setPlatformId(String platformId) {
        this.platformId = platformId;
    }
}
