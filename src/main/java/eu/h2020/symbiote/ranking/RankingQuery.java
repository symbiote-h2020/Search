package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Ranking query object, used to generate ranking for results;
 *
 * Created by Szymon Mueller on 27/07/2017.
 */
public class RankingQuery {

    private boolean includePopularity = true;

    private boolean includeAvailability = true;

    private boolean includeDistance = true;

    private double longitude;

    private double latitude;

    private Map<String, QueryResourceResult> resourcesMap = new HashMap<>();

    private QueryResponse initialQuery;

    public RankingQuery( QueryResponse queryResponse ) {
        this.initialQuery = queryResponse;
        queryResponse.getBody().stream().forEach( queryResourceResult
                -> {resourcesMap.put(queryResourceResult.getId(),queryResourceResult);});
    }

    public boolean isIncludePopularity() {
        return includePopularity;
    }

    public void setIncludePopularity(boolean includePopularity) {
        this.includePopularity = includePopularity;
    }

    public boolean isIncludeAvailability() {
        return includeAvailability;
    }

    public void setIncludeAvailability(boolean includeAvailability) {
        this.includeAvailability = includeAvailability;
    }

    public boolean isIncludeDistance() {
        return includeDistance;
    }

    public void setIncludeDistance(boolean includeDistance) {
        this.includeDistance = includeDistance;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Map<String, QueryResourceResult> getResourcesMap() {
        return resourcesMap;
    }

    public void setResourcesMap(Map<String, QueryResourceResult> resourcesMap) {
        this.resourcesMap = resourcesMap;
    }

    public QueryResponse toResponse() {
        QueryResponse qr = new QueryResponse();
        qr.setMessage(initialQuery.getMessage());
        qr.setStatus(initialQuery.getStatus());
        qr.setServiceResponse(initialQuery.getServiceResponse());

        qr.setBody(this.resourcesMap.values().stream().sorted(Comparator.comparing(QueryResourceResult::getRanking).reversed())
                .collect(Collectors.toList()));

//        qr.getBody().stream().forEach(res-> System.out.println(res.getId() + " : " + this.resourcesMap.get(res.getId()).getRanking() ));
        return qr;
    }
}
