package eu.h2020.symbiote.query;

import java.util.List;

/**
 * Class representing search query request.
 *
 * Created by Mael on 24/01/2017.
 */
public class SearchRequest {

    private String platform_id;
    private String platform_name;
    private String owner;
    private String name;
    private String id;
    private String description;
    private String location_name;
    private Double location_lat;
    private Double location_long;
    private Integer max_distance;
    private List<String> observed_property;

    public SearchRequest() {
    }

    public SearchRequest(String platform_id, String platform_name, String owner, String name, String id, String description, String location_name, Double location_lat, Double location_long, Integer max_distance, List<String> observed_property) {
        this.platform_id = platform_id;
        this.platform_name = platform_name;
        this.owner = owner;
        this.name = name;
        this.id = id;
        this.description = description;
        this.location_name = location_name;
        this.location_lat = location_lat;
        this.location_long = location_long;
        this.max_distance = max_distance;
        this.observed_property = observed_property;
    }

    public String getPlatform_id() {
        return platform_id;
    }

    public void setPlatform_id(String platform_id) {
        this.platform_id = platform_id;
    }

    public String getPlatform_name() {
        return platform_name;
    }

    public void setPlatform_name(String platform_name) {
        this.platform_name = platform_name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation_name() {
        return location_name;
    }

    public void setLocation_name(String location_name) {
        this.location_name = location_name;
    }

    public Double getLocation_lat() {
        return location_lat;
    }

    public void setLocation_lat(Double location_lat) {
        this.location_lat = location_lat;
    }

    public Double getLocation_long() {
        return location_long;
    }

    public void setLocation_long(Double location_long) {
        this.location_long = location_long;
    }

    public Integer getMax_distance() {
        return max_distance;
    }

    public void setMax_distance(Integer max_distance) {
        this.max_distance = max_distance;
    }

    public List<String> getObserved_property() {
        return observed_property;
    }

    public void setObserved_property(List<String> observed_property) {
        this.observed_property = observed_property;
    }
}