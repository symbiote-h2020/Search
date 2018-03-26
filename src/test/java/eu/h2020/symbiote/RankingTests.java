package eu.h2020.symbiote;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.cloud.monitoring.model.Metric;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdate;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import eu.h2020.symbiote.model.cim.Property;
import eu.h2020.symbiote.ranking.*;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Szymon Mueller on 31/07/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class RankingTests {

    private static final QueryResourceResult RESOURCE1;
    private static final QueryResourceResult RESOURCE2;
    private static final QueryResourceResult RESOURCE3;

    private static final String RES1_ID = "1";
    private static final Double RES1_LAT = Double.valueOf(52.402435d);
    private static final Double RES1_LONG = Double.valueOf(16.953166d);

    private static final String RES2_ID = "2";

    public static final Double RES2_LAT = Double.valueOf(52.402802d);
    public static final Double RES2_LONG = Double.valueOf(16.969817d);

    private static final String RES3_ID = "3";
    public static final Double RES3_LAT = Double.valueOf(52.414447d);
    public static final Double RES3_LONG = Double.valueOf(16.860812d);

    public static final String TEMPERATURE_NAME = "temperature";
    public static final String TEMPERATURE_IRI = "http://purl.oclc.org/NET/ssnx/qu/quantity#temperature";
    public static final String HUMIDITY_NAME = "humidity";
    public static final String HUMIDITY_IRI = "http://purl.oclc.org/NET/ssnx/qu/quantity#humidity";

    public static final Property TEMPERATURE_PROPERTY;

    public static final Property HUMIDITY_PROPERTY;

    static {
        TEMPERATURE_PROPERTY = new Property(TEMPERATURE_NAME,TEMPERATURE_IRI,null);
        HUMIDITY_PROPERTY = new Property(HUMIDITY_NAME,HUMIDITY_IRI,null);
    }

    @Mock
    PopularityRepository popularityRepository;

    @Mock
    PopularityManager popularityManager;

    @Mock
    AvailabilityRepository availabilityRepository;

    @Mock
    AvailabilityManager availabilityManager;

    @InjectMocks
    private RankingHandler rankingHandler = new RankingHandler(availabilityManager,popularityManager);

//    PopularityRepository popularityRepo;
//    AvailabilityRepository availabilityRepo;

    static {
        RESOURCE1 = generateResult1();
        RESOURCE2 = generateResult2();
        RESOURCE3 = generateResult3();
    }

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(rankingHandler, "popularityWeight",Float.valueOf(0.5f));
        ReflectionTestUtils.setField(rankingHandler, "availabilityWeight",Float.valueOf(1.0f));
        ReflectionTestUtils.setField(rankingHandler, "distanceWeight",Float.valueOf(0.5f));
        MockitoAnnotations.initMocks(this);

    }

    @Test
    public void testAvailabilityManager() {
        AvailabilityManager realManager = new AvailabilityManager(availabilityRepository);

        ObjectMapper mapper = new ObjectMapper();
        CloudMonitoringPlatform monit = new CloudMonitoringPlatform();
//            CloudMonitoringDevice[] devices = new CloudMonitoringDevice[1];
        CloudMonitoringDevice device = new CloudMonitoringDevice();
        Metric availabilityMetric = new Metric();
        availabilityMetric.setTag("availability");
        availabilityMetric.setDate(new Date());
        availabilityMetric.setValue("1");
        device.setMetrics(Arrays.asList(availabilityMetric));
        device.setId("res1");
//            devices[0] = device;
        monit.setMetrics(Arrays.asList(device));
        SecurityRequest secRequest = new SecurityRequest("test1");
        CloudMonitoringPlatformRequest request= new CloudMonitoringPlatformRequest(secRequest,monit);

        realManager.saveAvailabilityMessage(request);

        ArgumentCaptor<MonitoringInfo> argumentCaptor = ArgumentCaptor.forClass(MonitoringInfo.class);

        verify(availabilityRepository,times(1)).save(argumentCaptor.capture());
        CloudMonitoringDevice savedDeviceInfo = argumentCaptor.getValue().getMonitoringDeviceInfo();
        assertEquals(device.getId(),savedDeviceInfo.getId());
        assertEquals(device.getMetrics(),savedDeviceInfo.getMetrics());
        assertEquals(device.getMetrics().get(0).getValue(),savedDeviceInfo.getMetrics().get(0).getValue());
        assertEquals(device.getMetrics().get(0).getTag(),savedDeviceInfo.getMetrics().get(0).getTag());
//        assertEquals(device.getId(),savedDeviceInfo.getId());
        assertEquals(device.getId(),argumentCaptor.getValue().getId());
    }

    @Test
    public void testRanking() {
//        rankingHandler = new RankingHandler(availabilityManager, popularityManager);

        when(popularityManager.getPopularityForResource(anyString())).thenReturn(Integer.valueOf(15));
        when(availabilityManager.getAvailabilityForResource(anyString())).thenReturn(Float.valueOf(1.0f));

        QueryResponse queryResponse = new QueryResponse();
        List<QueryResourceResult> queryResourceResults = new ArrayList<>();

        queryResourceResults.add(RESOURCE1);
        queryResourceResults.add(RESOURCE2);
        queryResourceResults.add(RESOURCE3);

        queryResponse.setBody(queryResourceResults);
        RankingQuery rankingQuery = new RankingQuery(queryResponse);
        rankingQuery.setIncludeAvailability(true);
        rankingQuery.setIncludePopularity(true);
        rankingQuery.setIncludeDistance(true);
        rankingQuery.setLatitude(RES1_LAT);
        rankingQuery.setLongitude(RES1_LONG);
        QueryResponse rankedQueryResponse = rankingHandler.generateRanking(rankingQuery);

        List<QueryResourceResult> rankedResources = rankedQueryResponse.getResources();
        assertEquals(3,rankedResources.size());
        assertEquals(RESOURCE1,rankedResources.get(0));
        assertEquals(RESOURCE2,rankedResources.get(1));
        assertEquals(RESOURCE3,rankedResources.get(2));

        //check middle
        rankingQuery.setLatitude(Double.valueOf(52.412685d));
        rankingQuery.setLongitude(Double.valueOf(16.889488d));
        rankedQueryResponse = rankingHandler.generateRanking(rankingQuery);
        rankedResources = rankedQueryResponse.getResources();
        assertEquals(3,rankedResources.size());
        assertEquals(RESOURCE3,rankedResources.get(0));
        assertEquals(RESOURCE1,rankedResources.get(1));
        assertEquals(RESOURCE2,rankedResources.get(2));
    }

    @Test
    public void testRankingValue() {
        when(popularityManager.getPopularityForResource(RES1_ID)).thenReturn(Integer.valueOf(3));
        when(availabilityManager.getAvailabilityForResource(RES1_ID)).thenReturn(Float.valueOf(0.0f));

        when(popularityManager.getPopularityForResource(RES2_ID)).thenReturn(Integer.valueOf(8));
        when(availabilityManager.getAvailabilityForResource(RES2_ID)).thenReturn(Float.valueOf(1.0f));

        when(popularityManager.getPopularityForResource(RES3_ID)).thenReturn(Integer.valueOf(5));
        when(availabilityManager.getAvailabilityForResource(RES3_ID)).thenReturn(Float.valueOf(1.0f));

        QueryResponse queryResponse = new QueryResponse();
        List<QueryResourceResult> queryResourceResults = new ArrayList<>();

        queryResourceResults.add(RESOURCE1);
        queryResourceResults.add(RESOURCE2);
        queryResourceResults.add(RESOURCE3);

        queryResponse.setBody(queryResourceResults);
        RankingQuery rankingQuery = new RankingQuery(queryResponse);
        rankingQuery.setIncludeAvailability(true);
        rankingQuery.setIncludePopularity(true);
        rankingQuery.setIncludeDistance(true);
        rankingQuery.setLatitude(RES1_LAT);
        rankingQuery.setLongitude(RES1_LONG);
        QueryResponse rankedQueryResponse = rankingHandler.generateRanking(rankingQuery);

        List<QueryResourceResult> rankedResources = rankedQueryResponse.getResources();
        assertEquals(3,rankedResources.size());
        float res1Ranking = rankingQuery.getResourcesMap().get(RES1_ID).getRanking();
        float res2Ranking = rankingQuery.getResourcesMap().get(RES2_ID).getRanking();
        float res3Ranking = rankingQuery.getResourcesMap().get(RES3_ID).getRanking();

        assertTrue( res2Ranking > res1Ranking );
        assertTrue( res2Ranking > res3Ranking );
        assertTrue( res3Ranking > res1Ranking );
    }

    @Test
    public void testRankingValues2() {
        when(popularityManager.getPopularityForResource(RES1_ID)).thenReturn(Integer.valueOf(0));
        when(availabilityManager.getAvailabilityForResource(RES1_ID)).thenReturn(Float.valueOf(0.0f));

        when(popularityManager.getPopularityForResource(RES2_ID)).thenReturn(Integer.valueOf(3));
        when(availabilityManager.getAvailabilityForResource(RES2_ID)).thenReturn(Float.valueOf(0.0f));

        when(popularityManager.getPopularityForResource(RES3_ID)).thenReturn(Integer.valueOf(95));
        when(availabilityManager.getAvailabilityForResource(RES3_ID)).thenReturn(Float.valueOf(0.0f));

        QueryResponse queryResponse = new QueryResponse();
        List<QueryResourceResult> queryResourceResults = new ArrayList<>();

        queryResourceResults.add(RESOURCE1);
        queryResourceResults.add(RESOURCE2);
        queryResourceResults.add(RESOURCE3);

        queryResponse.setBody(queryResourceResults);
        RankingQuery rankingQuery = new RankingQuery(queryResponse);
        rankingQuery.setIncludeAvailability(true);
        rankingQuery.setIncludePopularity(true);
        rankingQuery.setIncludeDistance(false);
        QueryResponse rankedQueryResponse = rankingHandler.generateRanking(rankingQuery);

        List<QueryResourceResult> rankedResources = rankedQueryResponse.getResources();
        assertEquals(3,rankedResources.size());
        float res1Ranking = rankingQuery.getResourcesMap().get(RES1_ID).getRanking();
        float res2Ranking = rankingQuery.getResourcesMap().get(RES2_ID).getRanking();
        float res3Ranking = rankingQuery.getResourcesMap().get(RES3_ID).getRanking();

        assertTrue( res2Ranking > res1Ranking );
        assertTrue( res3Ranking > res2Ranking );
    }

    @Test
    public void testRankingValues3() {
        when(popularityManager.getPopularityForResource(RES1_ID)).thenReturn(Integer.valueOf(0));
        when(availabilityManager.getAvailabilityForResource(RES1_ID)).thenReturn(Float.valueOf(0.0f));

        when(popularityManager.getPopularityForResource(RES2_ID)).thenReturn(Integer.valueOf(3));
        when(availabilityManager.getAvailabilityForResource(RES2_ID)).thenReturn(Float.valueOf(0.0f));

        when(popularityManager.getPopularityForResource(RES3_ID)).thenReturn(Integer.valueOf(95));
        when(availabilityManager.getAvailabilityForResource(RES3_ID)).thenReturn(Float.valueOf(0.0f));

        QueryResponse queryResponse = new QueryResponse();
        List<QueryResourceResult> queryResourceResults = new ArrayList<>();

        queryResourceResults.add(RESOURCE1);
        queryResourceResults.add(RESOURCE2);
        queryResourceResults.add(RESOURCE3);

        queryResponse.setBody(queryResourceResults);
        RankingQuery rankingQuery = new RankingQuery(queryResponse);
        rankingQuery.setIncludeAvailability(true);
        rankingQuery.setIncludePopularity(true);
        rankingQuery.setIncludeDistance(true);
        rankingQuery.setLatitude(RES3_LAT);
        rankingQuery.setLongitude(RES3_LONG);
        QueryResponse rankedQueryResponse = rankingHandler.generateRanking(rankingQuery);

        List<QueryResourceResult> rankedResources = rankedQueryResponse.getResources();
        assertEquals(3,rankedResources.size());
        float res1Ranking = rankingQuery.getResourcesMap().get(RES1_ID).getRanking();
        float res2Ranking = rankingQuery.getResourcesMap().get(RES2_ID).getRanking();
        float res3Ranking = rankingQuery.getResourcesMap().get(RES3_ID).getRanking();

        assertTrue( res3Ranking > res1Ranking );
        assertTrue( res3Ranking > res2Ranking );
        assertTrue( res1Ranking > res2Ranking );
    }


    @Test
    public void testRankingForEmpty() {
        when(popularityManager.getPopularityForResource(RES1_ID)).thenReturn(Integer.valueOf(0));
        when(availabilityManager.getAvailabilityForResource(RES1_ID)).thenReturn(Float.valueOf(0.0f).floatValue());

        QueryResponse queryResponse = new QueryResponse();
        List<QueryResourceResult> queryResourceResults = new ArrayList<>();

        queryResourceResults.add(RESOURCE1);

        queryResponse.setBody(queryResourceResults);
        RankingQuery rankingQuery = new RankingQuery(queryResponse);
        rankingQuery.setIncludeAvailability(true);
        rankingQuery.setIncludePopularity(true);
        rankingQuery.setIncludeDistance(false);
        QueryResponse rankedQueryResponse = rankingHandler.generateRanking(rankingQuery);
    }

    @Test
    public void testAvailabilityManagerFindAvailable() {
        CloudMonitoringDevice monit = new CloudMonitoringDevice();
        monit.setId(RES1_ID);
        Metric availMetric = new Metric();
        availMetric.setTag("availability");
        availMetric.setValue("1");
        availMetric.setDate(new Date());
        monit.setMetrics(Arrays.asList(availMetric));
//        monit.setAvailability(1);
//        monit.setLoad(50);
//        monit.setTimestamp(""+DateTime.now().getMillis());
        Optional<MonitoringInfo> monitoringInfo = Optional.of(new MonitoringInfo(RES1_ID, monit));
        when(availabilityRepository.findById(RES1_ID)).thenReturn(monitoringInfo);

        AvailabilityManager manager = new AvailabilityManager(availabilityRepository);
        float availabilityVal = manager.getAvailabilityForResource(RES1_ID);
        verify(availabilityRepository).findById(RES1_ID);
        assertEquals(0.0f,availabilityVal,0.0f);
    }

    @Test
    public void testAvailabilityManagerFindNotAvailable() {
        CloudMonitoringDevice monit = new CloudMonitoringDevice();
        monit.setId(RES1_ID);
        Metric availMetric = new Metric();
        availMetric.setTag("availability");
        availMetric.setValue("0");
        availMetric.setDate(new Date());
        monit.setMetrics(Arrays.asList(availMetric));
        Optional<MonitoringInfo> monitoringInfo = Optional.of(new MonitoringInfo(RES1_ID, monit));
        when(availabilityRepository.findById(RES1_ID)).thenReturn(monitoringInfo);

        AvailabilityManager manager = new AvailabilityManager(availabilityRepository);
        float availabilityVal = manager.getAvailabilityForResource(RES1_ID);
        verify(availabilityRepository).findById(RES1_ID);
        assertEquals(0.0f,availabilityVal,0.0f);
    }

    @Test
    public void testAvailabilityManagerFindWrongAvailability() {
        CloudMonitoringDevice monit = new CloudMonitoringDevice();
        monit.setId(RES1_ID);
        Metric availMetric = new Metric();
        availMetric.setTag("availability");
        availMetric.setValue("67");
        availMetric.setDate(new Date());
        monit.setMetrics(Arrays.asList(availMetric));
        Optional<MonitoringInfo> monitoringInfo = Optional.of(new MonitoringInfo(RES1_ID, monit));
        when(availabilityRepository.findById(RES1_ID)).thenReturn(monitoringInfo);

        AvailabilityManager manager = new AvailabilityManager(availabilityRepository);
        float availabilityVal = manager.getAvailabilityForResource(RES1_ID);
        verify(availabilityRepository).findById(RES1_ID);
        assertEquals(0.0f,availabilityVal,0.0f);
    }

    @Test
    public void testGetPopularityForResource() {
        PopularityManager realManager = new PopularityManager(popularityRepository);
        String res1 = "res1";
        Integer views = 15;
        PopularityUpdate popularityUpdate = new PopularityUpdate();
        popularityUpdate.setId(res1);
        popularityUpdate.setViewsInDefinedInterval(views);
        Optional<PopularityUpdate> popularity = Optional.of(popularityUpdate);
        when(popularityRepository.findById(res1)).thenReturn(popularity);

        Integer result = realManager.getPopularityForResource(res1);
        verify(popularityRepository,times(1)).findById(res1);
        assertEquals("Result of popularity query must be the same as the one from repo", views,result);

    }

    @Test
    public void testPopularityManagerSave() {
        PopularityManager realPopularityManager = new PopularityManager(popularityRepository);
        PopularityUpdatesMessage updatesMessage = new PopularityUpdatesMessage();
        PopularityUpdate singleUpdate = new PopularityUpdate();
        singleUpdate.setId("res1");
        singleUpdate.setViewsInDefinedInterval(Integer.valueOf(10));
        List<PopularityUpdate> updateList = Arrays.asList(singleUpdate);
        updatesMessage.setPopularityUpdateList(updateList);
        realPopularityManager.savePopularityMessage(updatesMessage);
        verify(popularityRepository).save(singleUpdate);
    }

    private static QueryResourceResult generateResult1() {
        QueryResourceResult resourceResult = new QueryResourceResult();
        resourceResult.setId(RES1_ID);
        resourceResult.setDescription("desc1");
        resourceResult.setName("Resource1");
        resourceResult.setOwner("owner");
        resourceResult.setPlatformId("p1");
        resourceResult.setPlatformName("Platform1");
        resourceResult.setResourceType(Arrays.asList("Stationary"));
        resourceResult.setLocationAltitude(Double.valueOf(15.0d));
        resourceResult.setLocationLatitude(RES1_LAT);
        resourceResult.setLocationLongitude(RES1_LONG);
        resourceResult.setObservedProperties(Arrays.asList(TEMPERATURE_PROPERTY));
        return resourceResult;
    }

    private static QueryResourceResult generateResult2() {
        QueryResourceResult resourceResult = new QueryResourceResult();
        resourceResult.setId(RES2_ID);
        resourceResult.setDescription("desc2");
        resourceResult.setName("Resource2");
        resourceResult.setOwner("owner");
        resourceResult.setPlatformId("p1");
        resourceResult.setPlatformName("Platform1");
        resourceResult.setResourceType(Arrays.asList("Stationary"));
        resourceResult.setLocationAltitude(Double.valueOf(15.0d));
        resourceResult.setLocationLatitude(RES2_LAT);
        resourceResult.setLocationLongitude(RES2_LONG);
        resourceResult.setObservedProperties(Arrays.asList(TEMPERATURE_PROPERTY));
        return resourceResult;
    }

    private static QueryResourceResult generateResult3() {
        QueryResourceResult resourceResult = new QueryResourceResult();
        resourceResult.setId(RES3_ID);
        resourceResult.setDescription("desc3");
        resourceResult.setName("Resource3");
        resourceResult.setOwner("owner");
        resourceResult.setPlatformId("p1");
        resourceResult.setPlatformName("Platform1");
        resourceResult.setResourceType(Arrays.asList("Mobile"));
        resourceResult.setLocationAltitude(Double.valueOf(15.0d));
        resourceResult.setLocationLatitude(RES3_LAT);
        resourceResult.setLocationLongitude(RES3_LONG);
        resourceResult.setObservedProperties(Arrays.asList(TEMPERATURE_PROPERTY,HUMIDITY_PROPERTY));
        return resourceResult;
    }

}
