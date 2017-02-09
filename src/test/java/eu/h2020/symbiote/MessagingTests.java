package eu.h2020.symbiote;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import eu.h2020.symbiote.communication.RabbitManager;

import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.query.SearchRequest;
import eu.h2020.symbiote.query.SearchResponse;
import eu.h2020.symbiote.query.SearchResponseResource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static eu.h2020.symbiote.TestSetupConfig.*;
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

        ReflectionTestUtils.setField(rabbitManager, "resourceSearchRequestedRoutingKey", SEARCH_REQUESTED);
        ReflectionTestUtils.setField(rabbitManager, "resourceSearchPerformedRoutingKey", SEARCH_PERFORMED);

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
            verify(mockHandler).registerPlatform(isA(Platform.class));

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
            Resource resource = generateResource();
            String jsonResource = mapper.writeValueAsString(resource);
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
            Resource resource = generateResource();
            String jsonResource = mapper.writeValueAsString(resource);
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
            Resource resource = generateResource();
            String jsonResource = mapper.writeValueAsString(resource);
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
            SearchResponse response = mock(SearchResponse.class);
            when(response.getResourceList()).thenReturn(new ArrayList<SearchResponseResource>());
            when(mockHandler.search(isA(SearchRequest.class))).thenReturn(response);

            ObjectMapper mapper = new ObjectMapper();
            SearchRequest searchRequest = new SearchRequest();
            String jsonRequest = mapper.writeValueAsString(searchRequest);
            AMQP.BasicProperties props = new AMQP.BasicProperties()
                    .builder()
                    .correlationId("corrId")
                    .replyTo("replyq")
                    .build();

            sendMessage(RESOURCE_EXCHANGE_NAME, SEARCH_REQUESTED, props, jsonRequest);
            Thread.sleep(1000);
            verify(mockHandler).search(isA(SearchRequest.class));

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












