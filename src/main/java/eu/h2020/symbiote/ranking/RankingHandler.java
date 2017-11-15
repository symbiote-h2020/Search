package eu.h2020.symbiote.ranking;

import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Ranking helper class, used to generate rankings for the search results.
 * <p>
 * Created by Szymon Mueller on 15/07/2017.
 */
@Component
public class RankingHandler {

    private Log log = LogFactory.getLog(RankingHandler.class);

    private AvailabilityManager availabilityManager;

    private PopularityManager popularityManager;

    @Value("${ranking.popularity.weight}")
    private Float popularityWeight;

    @Value("${ranking.availability.weight}")
    private Float availabilityWeight;

    @Value("${ranking.distance.weight}")
    private Float distanceWeight;

    @Autowired
    public RankingHandler(AvailabilityManager availabilityManager, PopularityManager popularityManager) {
        this.availabilityManager = availabilityManager;
        this.popularityManager = popularityManager;
    }

    public Float getPopularityWeight() {
        return popularityWeight;
    }

    public Float getAvailabilityWeight() {
        return availabilityWeight;
    }

    public Float getDistanceWeight() {
        return distanceWeight;
    }

    /**
     * Orders all results from the query using the ranking algorithm. Results are stored in
     * {@link RankingQuery#getResourcesMap()}.
     *
     * @param query Query with all the parameters for which to add
     */
    public QueryResponse generateRanking(RankingQuery query) {
        Map<String, Float> normalisedDistanceMap = new HashMap<>();

        if (query.isIncludeDistance()) {
            Map<String, Float> distanceMap = new HashMap<>();
//            Map<String, Double> resourceDistanceMap = new HashMap<>();
            for(QueryResourceResult resource: query.getResourcesMap().values() ) {
                if( resource.getLocationLatitude() != null && resource.getLocationLongitude() != null ) {
                    distanceMap.put(resource.getId(), Float.valueOf((float)distance(query.getLatitude(), resource.getLocationLatitude(),
                            query.getLongitude(), resource.getLocationLongitude(), 0.0d, 0.0d)));
                } else {
                    distanceMap.put(resource.getId(),Float.valueOf(-1.0f));
                }
            }
            normalisedDistanceMap = normaliseFloatMap(distanceMap);
        } else {
            //TODO setup normalised distance map properly
            for(QueryResourceResult resource: query.getResourcesMap().values() ) {
                normalisedDistanceMap.put(resource.getId(),Float.valueOf(0.0f));
            }
        }

        Map<String,Integer> popularityMap = new HashMap<>();
        query.getResourcesMap().values().stream().forEach(queryResourceResult -> popularityMap.put(queryResourceResult.getId(),popularityManager.getPopularityForResource(queryResourceResult.getId())));
        Map<String,Float> normalisedPopularity = normaliseIntegerMap(popularityMap);

        float[] perfect = generatePerfectPoint(3);
        float[] worst = generateWorstPoint(3);
        float[] weights = generateWeights();
        float minWeightednDistance = nWeightedDistance(perfect,perfect,weights);
        float maxWeightednDistance = nWeightedDistance(perfect,worst,weights);
        for( QueryResourceResult resource: query.getResourcesMap().values() ) {


            float[] resValues = generateValueArrayForSensor(normalisedPopularity.get(resource.getId()),
                    availabilityManager.getAvailabilityForResource(resource.getId()),
                    normalisedDistanceMap.get(resource.getId()));


            float weightednDistance = nWeightedDistance(perfect, resValues, weights);

//            log.debug("Res " + resource.getQueryResult().getId() + " calculated weighted distance: " + weightednDistance);
//            if( first ) {
//                minWeightednDistance = weightednDistance;
//                maxWeightednDistance = weightednDistance;
//                first = false;
//            } else {
//                if (weightednDistance > maxWeightednDistance) {
//                    maxWeightednDistance = weightednDistance;
//                }
//                if (minWeightednDistance > weightednDistance) {
//                    minWeightednDistance = weightednDistance;
//                }
//            }
            resource.setRanking( weightednDistance );
        }

        normaliseAndReverseRanking( minWeightednDistance, maxWeightednDistance, query.getResourcesMap() );
        return query.toResponse();
    }

    /**
     * Normalises and reverses (so higher value = better) ranking of the resources passed as a map.
     * Normalisation done using the passed parameter maxWeightednDistance.
     *
     * @param maxWeightednDistance Number used for normalisation.
     * @param resourcesMap Map to be normalised and reversed.
     */
    private void normaliseAndReverseRanking(float minWeightednDistance, float maxWeightednDistance, Map<String, QueryResourceResult> resourcesMap) {
        if( resourcesMap != null ) {
            resourcesMap.values().stream().forEach(res->res.setRanking(1.0f - ((res.getRanking()-minWeightednDistance)/(maxWeightednDistance - minWeightednDistance))));
        }
    }

    private Map<String, Float> normaliseFloatMap( Map<String, Float> floatMap ) {
        Map<String, Float> results = new HashMap<String, Float>();
        Optional<Float> max = floatMap.values().stream().max(Comparator.naturalOrder());
        if( max.isPresent() ) {
            Float maxValue = max.get();
            if( maxValue.equals(Float.valueOf(-1.0f))) {
                Float perfectMark = Float.valueOf(1.0f);
                for( String key: floatMap.keySet()) {
                    results.put(key, perfectMark);
                }
            } else {
                for( String key: floatMap.keySet()) {
                    results.put(key, normaliseFloat(floatMap.get(key),maxValue));
                }
            }
        } else {
            //TODO cant happen?
        }
        return results;
    }

    private Map<String, Float>  normaliseIntegerMap( Map<String, Integer> integerMap ) {
        Map<String, Float> results = new HashMap<String, Float>();
        Optional<Integer> max = integerMap.values().stream().max(Comparator.naturalOrder());
        if( max.isPresent() ) {
            Integer maxValue = max.get();
            if( maxValue.equals(Integer.valueOf(-1))) {
                Float perfectMark = Float.valueOf(1.0f);
                for( String key: integerMap.keySet()) {
                    results.put(key, perfectMark);
                }
            } else {
                for( String key: integerMap.keySet()) {
                    results.put(key, normaliseInteger(integerMap.get(key),maxValue));
                }
            }
        } else {
            //TODO cant happen?
        }
        return results;
    }


    private Float normaliseFloat(Float value, Float max ) {
        return Float.valueOf(1.0f - value/max);
    }

    private Float normaliseInteger(Integer value, Integer max ) {
        if( max == Integer.valueOf(0) ) {
            return Float.valueOf(1.0f);
        } else {
            Float f = (float)value/(float)max;
            return Float.valueOf(f);
        }
    }

    private float[] generatePerfectPoint(int size) {
        if (size < 1 || size > 200) {
            throw new IllegalArgumentException("Could not generate perfect point for size of " + size);
        }
        float[] perfectPoint = new float[size];
        Arrays.fill(perfectPoint, 1f);
        return perfectPoint;
    }

    private float[] generateWorstPoint(int size) {
        if (size < 1 || size > 200) {
            throw new IllegalArgumentException("Could not generate perfect point for size of " + size);
        }
        float[] worstPoint = new float[size];
        Arrays.fill(worstPoint, 0.0f);
        return worstPoint;
    }

    private float[] generateWeights() {
        float[] weights = new float[3];
        weights[0] = 0.5f; //popularity
        weights[1] = 1.0f; //availability
        weights[2] = 0.5f; //distance
        return weights;
    }

    private float[] generateValueArrayForSensor( float popularity, float availability, float distance ) {
        float[] resValues = new float[3];
        resValues[0] = popularity;
        resValues[1] = availability;
        resValues[2] = distance;
        return resValues;
    }

    private float nWeightedDistance(float[] perfectPoint, float[] realValues, float[] weights) {
        float total = 0, diff;
        for (int i = 0; i < perfectPoint.length; i++) {
            diff = realValues[i] - perfectPoint[i];
            total += weights[i] * diff * diff;
        }
        return (float) Math.sqrt(total);
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     * <p>
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     *
     * @returns Distance in Meters
     */
    public double distance(double lat1, double lat2, double lon1,
                           double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000;

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }

}
