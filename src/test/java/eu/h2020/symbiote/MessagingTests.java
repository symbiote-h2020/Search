package eu.h2020.symbiote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import eu.h2020.symbiote.cloud.model.ssp.SspRegInfo;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringDevice;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatform;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.cloud.monitoring.model.Metric;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.ci.QueryResourceResult;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.*;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.model.mim.SmartSpace;
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
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * Created by Mael on 06/02/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class MessagingTests {



    @InjectMocks
    private RabbitManager rabbitManager;
    private ThreadPoolExecutor writerExecutorService;


    @Before
    public void setup() {
        ReflectionTestUtils.setField(rabbitManager, "rabbitHost", "localhost");
        ReflectionTestUtils.setField(rabbitManager, "rabbitUsername", "guest");
        ReflectionTestUtils.setField(rabbitManager, "rabbitPassword", "guest");
        ReflectionTestUtils.setField(rabbitManager, "rabbitMessageTimeout", 60000);


        ReflectionTestUtils.setField(rabbitManager, "platformExchangeName", PLATFORM_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "plaftormExchangeDurable", false);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeAutodelete", true);
        ReflectionTestUtils.setField(rabbitManager, "platformExchangeInternal", false);

        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeName", RESOURCE_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeDurable", false);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeAutodelete", true);
        ReflectionTestUtils.setField(rabbitManager, "resourceExchangeInternal", false);


        ReflectionTestUtils.setField(rabbitManager, "sspExchangeName", SSP_EXCHANGE );

        ReflectionTestUtils.setField(rabbitManager, "sspExchangeType", "topic" );
        ReflectionTestUtils.setField(rabbitManager, "sspExchangeDurable", false );
        ReflectionTestUtils.setField(rabbitManager, "sspExchangeAutodelete", true );
        ReflectionTestUtils.setField(rabbitManager, "sspExchangeInternal", false );
        ReflectionTestUtils.setField(rabbitManager, "sspCreatedRoutingKey",SSP_CREATED);
        ReflectionTestUtils.setField(rabbitManager, "sspRemovedRoutingKey",SSP_DELETED);
        ReflectionTestUtils.setField(rabbitManager, "sspModifiedRoutingKey",SSP_MODIFIED);

        ReflectionTestUtils.setField(rabbitManager, "sspSdevCreatedRoutingKey",SSP_SDEV_CREATED);
        ReflectionTestUtils.setField(rabbitManager, "sspSdevRemovedRoutingKey", SSP_SDEV_DELETED);
        ReflectionTestUtils.setField(rabbitManager, "sspSdevModifiedRoutingKey", SSP_SDEV_MODIFIED);

        ReflectionTestUtils.setField(rabbitManager, "sspSdevResourceCreatedRoutingKey",SSP_RESOURCE_CREATED);
        ReflectionTestUtils.setField(rabbitManager, "sspSdevResourceRemoveddRoutingKey",SSP_RESOURCE_DELETED);
        ReflectionTestUtils.setField(rabbitManager, "sspSdevResourceModifiedRoutingKey",SSP_RESOURCE_MODIFIED);

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

        ReflectionTestUtils.setField(rabbitManager, "mappingsExchangeName", MAPPING_EXCHANGE_NAME);
        ReflectionTestUtils.setField(rabbitManager, "mappingsExchangeType", "topic");
        ReflectionTestUtils.setField(rabbitManager, "mappingsExchangeDurable", false);
        ReflectionTestUtils.setField(rabbitManager, "mappingsExchangeAutodelete", true);
        ReflectionTestUtils.setField(rabbitManager, "mappingsExchangeInternal", false);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");

        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter);
        ReflectionTestUtils.setField(rabbitManager, "rabbitTemplate", rabbitTemplate);

        ReflectionTestUtils.invokeMethod(rabbitManager, "init");

        this.writerExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(20);
        writerExecutorService.setMaximumPoolSize(30);
        writerExecutorService.setKeepAliveTime(5, TimeUnit.MINUTES);
    }

    @After
    public void teardown() {
        ReflectionTestUtils.invokeMethod(rabbitManager, "cleanup");
    }

    @Test
    public void testPlatformConsumerForCreateCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerPlatformCreatedConsumer(mockHandler, writerExecutorService);

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
            rabbitManager.registerPlatformDeletedConsumer(mockHandler,writerExecutorService);

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
            rabbitManager.registerPlatformUpdatedConsumer(mockHandler,writerExecutorService);

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
    public void testSspConsumerForCreateCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerSspCreatedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            SmartSpace ssp = generateSmartSpace(SSP_NAME,SSP_ID,PLATFORM_A_URL);
            String jsonSsp = mapper.writeValueAsString(ssp);
            sendMessage(SSP_EXCHANGE, SSP_CREATED, null, jsonSsp);
            Thread.sleep(1000);
            ArgumentCaptor<SmartSpace> sspCaptor = ArgumentCaptor.forClass(SmartSpace.class);
            verify(mockHandler,times(1)).registerSsp(sspCaptor.capture());
            assertEquals("ID of the ssps must be the same",ssp.getId(),sspCaptor.getValue().getId());
            assertEquals("Name of the ssps must be the same",ssp.getName(),sspCaptor.getValue().getName());
            assertEquals("Description of the ssps must be the same",ssp.getDescription(),sspCaptor.getValue().getDescription());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSspConsumerForDeleteCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerSspDeletedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            SmartSpace ssp = generateSmartSpace(SSP_NAME,SSP_ID,PLATFORM_A_URL);
            String jsonSsp = mapper.writeValueAsString(ssp);
            sendMessage(SSP_EXCHANGE, SSP_DELETED, null, jsonSsp);
            Thread.sleep(1000);
            ArgumentCaptor<String> sspIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockHandler,times(1)).deleteSsp(sspIdCaptor.capture());
            assertEquals("ID to delete must be the same", ssp.getId(),sspIdCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSspConsumerForModifyCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerSspUpdatedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            SmartSpace ssp = generateSmartSpace(SSP_NAME,SSP_ID,PLATFORM_A_URL);
            String jsonSsp = mapper.writeValueAsString(ssp);
            sendMessage(SSP_EXCHANGE, SSP_MODIFIED, null, jsonSsp);
            Thread.sleep(1000);
            ArgumentCaptor<SmartSpace> sspUpdateCaptor = ArgumentCaptor.forClass(SmartSpace.class);
            verify(mockHandler,times(1)).updateSsp(sspUpdateCaptor.capture());

            assertEquals("ID of the ssps must be the same",ssp.getId(),sspUpdateCaptor.getValue().getId());
            assertEquals("Name of the ssps must be the same",ssp.getName(),sspUpdateCaptor.getValue().getName());
            assertEquals("Description of the ssps must be the same",ssp.getDescription(),sspUpdateCaptor.getValue().getDescription());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSdevConsumerForCreateCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerSdevCreatedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            SspRegInfo sdev = generateSdev("id_in_ssp",SDEV_ID_1,"sspSymbioteId","https://www.example.com/ssp1/symbiote");
            String jsonSdev = mapper.writeValueAsString(sdev);
            sendMessage(SSP_EXCHANGE, SSP_SDEV_CREATED, null, jsonSdev);
            Thread.sleep(1000);
            ArgumentCaptor<SspRegInfo> sdevCaptor = ArgumentCaptor.forClass(SspRegInfo.class);
            verify(mockHandler,times(1)).registerSdev(sdevCaptor.capture());
            assertEquals("ID of the sdevs must be the same",sdev.getSymId(),sdevCaptor.getValue().getSymId());
            assertEquals("plugin id of the sdevs must be the same",sdev.getPluginId(),sdevCaptor.getValue().getPluginId());
            assertEquals("plugin url of the sdevs must be the same",sdev.getPluginURL(),sdevCaptor.getValue().getPluginURL());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSdevConsumerForDeleteCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerSdevDeletedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            SspRegInfo sdev = generateSdev("id_in_ssp",SDEV_ID_1,"sspSymbioteId","https://www.example.com/ssp1/symbiote");
            String jsonSdev = mapper.writeValueAsString(sdev);
            sendMessage(SSP_EXCHANGE, SSP_SDEV_DELETED, null, jsonSdev);
            Thread.sleep(1000);
            ArgumentCaptor<String> sdevIdCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockHandler,times(1)).deleteSdev(sdevIdCaptor.capture());
            assertEquals("ID to delete must be the same", sdev.getSymId(),sdevIdCaptor.getValue());

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSdevConsumerForModifyCalled() {
        PlatformHandler mockHandler = mock(PlatformHandler.class);
        try {
            rabbitManager.registerSdevUpdatedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            SspRegInfo sdev = generateSdev("id_in_ssp",SDEV_ID_1,"sspSymbioteId","https://www.example.com/ssp1/symbiote");
            String jsonSdev = mapper.writeValueAsString(sdev);
            sendMessage(SSP_EXCHANGE, SSP_SDEV_MODIFIED, null, jsonSdev);
            Thread.sleep(1000);
            ArgumentCaptor<SspRegInfo> sdevUpdateCaptor = ArgumentCaptor.forClass(SspRegInfo.class);
            verify(mockHandler,times(1)).updateSdev(sdevUpdateCaptor.capture());

            assertEquals("ID of the sdevs must be the same",sdev.getSymId(),sdevUpdateCaptor.getValue().getSymId());
            assertEquals("plugin id of the sdevs must be the same",sdev.getPluginId(),sdevUpdateCaptor.getValue().getPluginId());
            assertEquals("plugin url of the sdevs must be the same",sdev.getPluginURL(),sdevUpdateCaptor.getValue().getPluginURL());

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
            rabbitManager.registerResourceCreatedConsumer(mockHandler,writerExecutorService);

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
            rabbitManager.registerResourceUpdatedConsumer(mockHandler,writerExecutorService);

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
            rabbitManager.registerResourceDeletedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
//            CoreResource resource = generateResource();
            List<String> resourcesToDel = Arrays.asList(RESOURCE_101_ID);
            String jsonResource = mapper.writeValueAsString(resourcesToDel);
            sendMessage(RESOURCE_EXCHANGE_NAME, RESOURCE_DELETED, null, jsonResource);
            Thread.sleep(1000);
            verify(mockHandler).deleteResources(isA(List.class));

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSspResourceConsumerForCreateCalled() {
        ResourceHandler mockHandler = mock(ResourceHandler.class);
        try {
            rabbitManager.registerSspResourceCreatedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            CoreSspResourceRegisteredOrModifiedEventPayload payload = new CoreSspResourceRegisteredOrModifiedEventPayload();
            CoreResource resource = generateResource();
            payload.setResources(Arrays.asList(resource));
            payload.setPlatformId(SSP_ID_1);
            payload.setSdevId(SDEV_ID_1);
            String jsonResource = mapper.writeValueAsString(payload);
            sendMessage(RESOURCE_EXCHANGE_NAME, SSP_RESOURCE_CREATED, null, jsonResource);
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
    public void testSspResourceConsumerForModifyCalled() {
        ResourceHandler mockHandler = mock(ResourceHandler.class);
        try {
            rabbitManager.registerSspResourceUpdatedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
            CoreSspResourceRegisteredOrModifiedEventPayload payload = new CoreSspResourceRegisteredOrModifiedEventPayload();
            CoreResource resource = generateResource();
            payload.setResources(Arrays.asList(resource));
            payload.setPlatformId(SSP_ID_1);
            payload.setSdevId(SDEV_ID_1);
            String jsonResource = mapper.writeValueAsString(payload);
            sendMessage(RESOURCE_EXCHANGE_NAME, SSP_RESOURCE_MODIFIED, null, jsonResource);
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
    public void testSspResourceConsumerForDeleteCalled() {
        ResourceHandler mockHandler = mock(ResourceHandler.class);
        try {
            rabbitManager.registerSspResourceDeletedConsumer(mockHandler,writerExecutorService);

            ObjectMapper mapper = new ObjectMapper();
//            CoreResource resource = generateResource();
            List<String> resourcesToDel = Arrays.asList(RESOURCE_101_ID);
            String jsonResource = mapper.writeValueAsString(resourcesToDel);
            sendMessage(RESOURCE_EXCHANGE_NAME, SSP_RESOURCE_DELETED, null, jsonResource);
            Thread.sleep(1000);
            verify(mockHandler).deleteResources(isA(List.class));

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
            when(mockHandler.search(isA(SearchCommunicationHandler.class),isA(CoreQueryRequest.class))).thenReturn(response);

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
            verify(mockHandler,times(1)).search(isA(SearchCommunicationHandler.class),isA(CoreQueryRequest.class));

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
            when(mockHandler.sparqlSearch(isA(SearchCommunicationHandler.class),isA(CoreSparqlQueryRequest.class))).thenReturn(response);

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
            verify(mockHandler,times(1)).sparqlSearch(isA(SearchCommunicationHandler.class),isA(CoreSparqlQueryRequest.class));

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
            channel = rabbitManager.getConnection().createChannel(false);
            channel.basicPublish(exchange, routingKey, properties, message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}













