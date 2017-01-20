package eu.h2020.symbiote.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Class representing WGS84 geo location in the system events.
 *
 * Created by Mael on 16/01/2017.
 */
public class Location {
    private String id = null;
    private String name = null;
    private String description = null;
    private Double latitude = null;
    private Double longitude = null;
    private Double altitude = null;

    public Location() {

    }

    public Location(String id, String name, String description, Double latitude, Double longitude, Double altitude) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(Double altitude) {
        this.altitude = altitude;
    }
}
