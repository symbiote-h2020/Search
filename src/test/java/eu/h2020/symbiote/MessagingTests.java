package eu.h2020.symbiote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.cloud.monitoring.model.Metric;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.ranking.AvailabilityManager;
import eu.h2020.symbiote.ranking.PopularityManager;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created by Mael on 06/02/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagingTests {

    public static final String PLATFORM_EXCHANGE_NAME = "symbiote.platform";
    public static final String PLATFORM_CREATED = "platform.created";
    public static final String PLATFORM_MODIFIED = "platform.modified";
    public static final String PLATFORM_DELETED = "platform.removed";
    public static final String RESOURCE_EXCHANGE_NAME = "symbiote.resource";
    public static final String RESOURCE_CREATED = "resource.created";
    public static final String RESOURCE_MODIFIED = "resource.modified";
    public static final String RESOURCE_DELETED = "resource.removed";
    public static final String SEARCH_REQUESTED = "resource.searchRequested";
    public static final String SEARCH_PERFORMED = "resource.searchPerformed";
    public static final String SPARQL_REQUESTED = "resource.sparqlRequested";
    public static final String SPARQL_PERFORMED = "resource.sparqlPerformed";
    public static final String EXCHANGE_SEARCH = "symbiote.search";
    public static final String POPULARITY_RK = "symbiote.popularity.rk";

    @InjectMocks
    private RabbitManager rabbitManager;


    @Before
    public void setup() {
        ReflectionTestUtils.setField(rabbitManager, "rabbitHost", "localhost");
        ReflectionTestUtils.setField(rabbitManager, "rabbitUsername", "guest");
        ReflectionTestUtils.setField(rabbitManager, "rabbitPassword", "guest");

        ReflectionTestUtils.setField(rabbitManager, "platformExchangeName", PLATFORM_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "plaftormExchangeDurable", true);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeAutodelete", false);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeInternal", false);

        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeName", RESOURCE_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeDurable", true);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeAutodelete", false);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeInternal", false);

        ReflectionTestUtils.setField(rabbitManager, "platformCreatedRoutingKey", PLATFORM_CREATED);
        ReflectionTestUtils.setField(rabbitManager, "platformModifiedRoutingKey", PLATFORM_MODIFIED);
        ReflectionTestUtils.setField(rabbitManager, "platformDeletedRoutingKey", PLATFORM_DELETED);
        ReflectionTestUtils.setField(rabbitManager, "resourceCreatedRoutingKey", RESOURCE_CREATED);
        ReflectionTestUtils.setField(rabbitManager, "resourceModifiedRoutingKey", RESOURCE_MODIFIED);
        ReflectionTestUtils.setField(rabbitManager, "resourceDeletedRoutingKey", RESOURCE_DELETED);

        ReflectionTestUtils.setField(rabbitManager, "exchangeSearchName", EXCHANGE_SEARCH);
        ReflectionTestUtils.setField(rabbitManager, "exchangeSearchType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "exchangeSearchDurable", true );
        ReflectionTestUtils.setField(rabbitManager, "exchangeSearchAutodelete", false );
        ReflectionTestUtils.setField(rabbitManager, "exchangeSearchInternal", false );
        ReflectionTestUtils.setField(rabbitManager, "popularityUpdatesRoutingKey", POPULARITY_RK);

        ReflectionTestUtils.setField(rabbitManager, "resourceSearchRequestedRoutingKey", SEARCH_REQUESTED);
        ReflectionTestUtils.setField(rabbitManager, "resourceSearchPerformedRoutingKey", SEARCH_PERFORMED);
        ReflectionTestUtils.setField(rabbitManager, "resourceSparqlSearchRequestedRoutingKey", SPARQL_REQUESTED);
        ReflectionTestUtils.setField(rabbitManager, "resourceSparqlSearchPerformedRoutingKey", SPARQL_PERFORMED);


        ReflectionTestUtils.invokeMethod(rabbitManager, "init");
    }

    @After
    public void teardown() {
        ReflectionTestUtils.invokeMethod(rabbitManager, "cleanup");
    }

    @Test
    public void testPlatformConsumerForCreateCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerPlatformCreatedConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
            Platform platform = generatePlatformA();
            String jsonPlatform = mapper.writeValueAsString(platform);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_CREATED, null, jsonPlatform);
            Thread.sleep(1000);
            ArgumentCaptor<Platform> platformCaptor = ArgumentCaptor.forClass(Platform.class);
            verify(mockHandler,times(1)).registerPlatform(platformCaptor.capture());
            assertEquals("ID of the platforms must be the same",platform.getId(),platformCaptor.getValue().getId());
            assertEquals("Name of the platforms must be the same",platform.getName(),platformCaptor.getValue().getName());
            assertEquals("Description of the platforms must be the same",platform.getDescription(),platformCaptor.getValue().getDescription());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPlatformConsumerForDeleteCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerPlatformDeletedConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
            Platform platform = generatePlatformA();
            String jsonPlatform = mapper.writeValueAsString(platform);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_DELETED, null, jsonPlatform);
            Thread.sleep(1000);
            ArgumentCaptor<String> platformIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockHandler,times(1)).deletePlatform(platformIdCaptor.capture());
            assertEquals("ID to delete must be the same", platform.getId(),platformIdCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPlatformConsumerForModifyCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerPlatformUpdatedConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
            Platform platform = generatePlatformA();
            String jsonPlatform = mapper.writeValueAsString(platform);
            sendMessage(PLATFORM_EXCHANGE_NAME, PLATFORM_MODIFIED, null, jsonPlatform);
            Thread.sleep(1000);
            ArgumentCaptor<Platform> platformUpdateCaptor = ArgumentCaptor.forClass(Platform.class);
            verify(mockHandler,times(1)).updatePlatform(platformUpdateCaptor.capture());

            assertEquals("ID of the platforms must be the same",platform.getId(),platformUpdateCaptor.getValue().getId());
            assertEquals("Name of the platforms must be the same",platform.getName(),platformUpdateCaptor.getValue().getName());
            assertEquals("Description of the platforms must be the same",platform.getDescription(),platformUpdateCaptor.getValue().getDescription());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testResourceConsumerForCreateCalled() {
        ResourceHandler mockHandler = mock(ResourceHandler.class);
        try {
            rabbitManager.registerResourceCreatedConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
            CoreResourceRegisteredOrModifiedEventPayload payload = new CoreResourceRegisteredOrModifiedEventPayload();
            CoreResource resource = generateResource();
            payload.setResources(Arrays.asList(resource));
            payload.setPlatformId(PLATFORM_A_ID);
            String jsonResource = mapper.writeValueAsString(payload);
            sendMessage(RESOURCE_EXCHANGE_NAME, RESOURCE_CREATED, null, jsonResource);
            Thread.sleep(1000);
            verify(mockHandler).registerResource(any());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testResourceConsumerForModifyCalled() {
        ResourceHandler mockHandler = mock(ResourceHandler.class);
        try {
            rabbitManager.registerResourceUpdatedConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
            CoreResourceRegisteredOrModifiedEventPayload payload = new CoreResourceRegisteredOrModifiedEventPayload();
            CoreResource resource = generateResource();
            payload.setResources(Arrays.asList(resource));
            payload.setPlatformId(PLATFORM_A_ID);
            String jsonResource = mapper.writeValueAsString(payload);
            sendMessage(RESOURCE_EXCHANGE_NAME, RESOURCE_MODIFIED, null, jsonResource);
            Thread.sleep(1000);
            verify(mockHandler).updateResource(any());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testResourceConsumerForDeleteCalled() {
        ResourceHandler mockHandler = mock(ResourceHandler.class);
        try {
            rabbitManager.registerResourceDeletedConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
//            CoreResource resource = generateResource();
            List<String> resourcesToDel = Arrays.asList(RESOURCE_101_ID);
            String jsonResource = mapper.writeValueAsString(resourcesToDel);
            sendMessage(RESOURCE_EXCHANGE_NAME, RESOURCE_DELETED, null, jsonResource);
            Thread.sleep(1000);
            verify(mockHandler).deleteResource(isA(String.class));

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSearchRequestCalled() {
        SearchHandler mockHandler = mock(SearchHandler.class);
        try {
            rabbitManager.registerResourceSearchConsumer(mockHandler);
            QueryResponse response = mock(QueryResponse.class);
            when(response.getResources()).thenReturn(new ArrayList<QueryResourceResult>());
            when(mockHandler.search(isA(CoreQueryRequest.class))).thenReturn(response);

            ObjectMapper mapper = new ObjectMapper();
            CoreQueryRequest searchRequest = new CoreQueryRequest();
            String jsonRequest = mapper.writeValueAsString(searchRequest);
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId("corrId")
                    .replyTo("replyq")
                    .build();

            sendMessage(RESOURCE_EXCHANGE_NAME, SEARCH_REQUESTED, props, jsonRequest);
            Thread.sleep(1000);
            verify(mockHandler,times(1)).search(isA(CoreQueryRequest.class));

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSPARQLRequestCalled() {
        String rdf = "This_is_RDF";
        SearchHandler mockHandler = mock(SearchHandler.class);
        try {
            rabbitManager.registerResourceSparqlSearchConsumer(mockHandler);
            SparqlQueryResponse response = mock(SparqlQueryResponse.class);
            when(response.getBody()).thenReturn(rdf);
            when(mockHandler.sparqlSearch(isA(CoreSparqlQueryRequest.class))).thenReturn(response);

            ObjectMapper mapper = new ObjectMapper();
            CoreSparqlQueryRequest searchRequest = new CoreSparqlQueryRequest();
            String jsonRequest = mapper.writeValueAsString(searchRequest);
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId("corrId")
                    .replyTo("replyq")
                    .build();

            sendMessage(RESOURCE_EXCHANGE_NAME, SPARQL_REQUESTED, props, jsonRequest);
            Thread.sleep(1000);
            verify(mockHandler,times(1)).sparqlSearch(isA(CoreSparqlQueryRequest.class));

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testAvailabilityRequestCalled() {
        AvailabilityManager mockHandler = mock(AvailabilityManager.class);
        try {
            rabbitManager.registerAvailabilityUpdateConsumer(mockHandler);

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
            SecurityRequest request = new SecurityRequest("test1");
            CloudMonitoringPlatformRequest searchRequest = new CloudMonitoringPlatformRequest(request,monit);
            String jsonRequest = mapper.writeValueAsString(searchRequest);
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId("corrId")
                    .replyTo("replyq")
                    .build();

            sendMessage(RabbitManager.MONITORING_EXCHANGE, RabbitManager.MONITORING_ROUTING_KEY, props, jsonRequest);
            Thread.sleep(1000);
            verify(mockHandler,times(1)).saveAvailabilityMessage(isA(CloudMonitoringPlatformRequest.class));

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPopularityRequestCalled() {
        PopularityManager mockHandler = mock(PopularityManager.class);
        try {
            rabbitManager.registerPopularityUpdateConsumer(mockHandler);

            ObjectMapper mapper = new ObjectMapper();
            PopularityUpdatesMessage searchRequest = new PopularityUpdatesMessage();
            String jsonRequest = mapper.writeValueAsString(searchRequest);
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId("corrId")
                    .replyTo("replyq")
                    .build();

            sendMessage(EXCHANGE_SEARCH, POPULARITY_RK, props, jsonRequest);
            Thread.sleep(1000);
            verify(mockHandler,times(1)).savePopularityMessage(isA(PopularityUpdatesMessage.class));

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String exchange, String routingKey, AMQP.BasicProperties properties, String message) {
        Channel channel = null;
        try {
            channel = rabbitManager.getConnection().createChannel();
            channel.basicPublish(exchange, routingKey, properties, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}













