package eu.h2020.symbiote.communication;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Bean used to manage internal communication using RabbitMQ.
 * It is responsible for declaring exchanges and using routing keys from centralized config server.
 */
@Component
public class RabbitManager {

    private static Log log = LogFactory.getLog(RabbitManager.class);

    @Value("${rabbit.host}")
    private String rabbitHost;

    @Value("${rabbit.username}")
    private String rabbitUsername;

    @Value("${rabbit.password}")
    private String rabbitPassword;

    @Value("${rabbit.exchange.platform.name}")
    private String platformExchangeName;
    @Value("${rabbit.exchange.platform.type}")
    private String platformExchangeType;
    @Value("${rabbit.exchange.platform.durable}")
    private boolean plaftormExchangeDurable;
    @Value("${rabbit.exchange.platform.autodelete}")
    private boolean platformExchangeAutodelete;
    @Value("${rabbit.exchange.platform.internal}")
    private boolean platformExchangeInternal;
    @Value("${rabbit.routingKey.platform.creationRequested}")
    private String platformCreationRequestedRoutingKey;
    @Value("${rabbit.routingKey.platform.created}")
    private String platformCreatedRoutingKey;
    @Value("${rabbit.routingKey.platform.modified}")
    private String platformModifiedRoutingKey;
    @Value("${rabbit.routingKey.platform.removed}")
    private String platformDeletedRoutingKey;

    @Value("${rabbit.exchange.resource.name}")
    private String resourceExchangeName;
    @Value("${rabbit.exchange.resource.type}")
    private String resourceExchangeType;
    @Value("${rabbit.exchange.resource.durable}")
    private boolean resourceExchangeDurable;
    @Value("${rabbit.exchange.resource.autodelete}")
    private boolean resourceExchangeAutodelete;
    @Value("${rabbit.exchange.resource.internal}")
    private boolean resourceExchangeInternal;
    @Value("${rabbit.routingKey.resource.creationRequested}")
    private String resourceCreationRequestedRoutingKey;
    @Value("${rabbit.routingKey.resource.created}")
    private String resourceCreatedRoutingKey;

    @Value("${rabbit.routingKey.resource.modified}")
    private String resourceModifiedRoutingKey;

    @Value("${rabbit.routingKey.resource.removed}")
    private String resourceDeletedRoutingKey;

    @Value("${rabbit.routingKey.resource.searchRequested}")
    private String resourceSearchRequestedRoutingKey;

    @Value("${rabbit.routingKey.resource.searchPerformed}")
    private String resourceSearchPerformedRoutingKey;

    private Connection connection;

    /**
     * Initialization method. Used to create global connection used by all communication within the component and
     * all global exchanges.
     */
    @PostConstruct
    private void init() throws InterruptedException {
        //FIXME check if there is better exception handling in @postconstruct method
        Channel channel = null;

        try {
            ConnectionFactory factory = new ConnectionFactory();
//            factory.setHost("127.0.0.1"); //todo value from properties
            factory.setHost(this.rabbitHost);
            factory.setUsername(this.rabbitUsername);
            factory.setPassword(this.rabbitPassword);

            this.connection = factory.newConnection();

            channel = this.connection.createChannel();
            channel.exchangeDeclare(this.platformExchangeName,
                    this.platformExchangeType,
                    this.plaftormExchangeDurable,
                    this.platformExchangeAutodelete,
                    this.platformExchangeInternal,
                    null);

            channel.exchangeDeclare(this.resourceExchangeName,
                    this.resourceExchangeType,
                    this.resourceExchangeDurable,
                    this.resourceExchangeAutodelete,
                    this.resourceExchangeInternal,
                    null);


            //message retrieval
            //receiveMessages();

            // message to Search Service

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        } finally {
            closeChannel(channel);
        }
    }

    /**
     * Cleanup method - removes the global connection disconnecting all channels/queues.
     */
    @PreDestroy
    private void cleanup() {
        //FIXME check if there is better exception handling in @predestroy method
        try {
            if (this.connection != null && this.connection.isOpen())
                this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Closes specified channel.
     *
     * @param channel Channel to be closed.
     */
    private void closeChannel(Channel channel) {
        try {
            if (channel != null && channel.isOpen())
                channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    /**
     * Registers consumer for event platform.created. Event will trigger translation of the platform into RDF
     * and writing it into JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when platform.created event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerPlatformCreatedConsumer( PlatformHandler platformHandler ) throws IOException {

        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, platformExchangeName, platformCreatedRoutingKey);
        PlatformCreatedConsumer consumer = new PlatformCreatedConsumer(channel, platformHandler );

        log.debug("Creating platform consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer platform created!!!" );
    }

    /**
     * Registers consumer for event platform.deleted. Event will trigger translation of the request into SPARQL UPDATE
     * and executing it in JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when platform.deleted event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerPlatformDeletedConsumer( PlatformHandler platformHandler ) throws IOException {

        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, platformExchangeName, platformDeletedRoutingKey);
        PlatformDeletedConsumer consumer = new PlatformDeletedConsumer(channel,platformHandler );

        log.debug("Delete platform consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer delete platform created!!!" );
    }

    /**
     * Registers consumer for event platform.updated. Event will trigger translation of the resource into RDF
     * and writing it into JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when platform.updated event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerPlatformUpdatedConsumer(PlatformHandler platformHandler) throws IOException {

        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, platformExchangeName, platformModifiedRoutingKey);
        PlatformModifiedConsumer consumer = new PlatformModifiedConsumer(channel, platformHandler );

        log.debug("Creating platform modified consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer platform modified created!!!" );
    }

    /**
     * Registers consumer for event resource.created. Event will trigger translation of the resource into RDF
     * and writing it into JENA repository.
     *
     * @param resourceHandler Event handler which will be triggered when resource.created event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerResourceCreatedConsumer( ResourceHandler resourceHandler) throws IOException {

        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, resourceExchangeName, resourceCreatedRoutingKey);
        ResourceCreatedConsumer consumer = new ResourceCreatedConsumer(channel, resourceHandler );

        log.debug("Creating resource consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer resource created!!!" );
    }

    /**
     * Registers consumer for event resource.deleted. Event will trigger translation of the request into SPARQL UPDATE
     * and executing it in JENA repository.
     *
     * @param resourceDeleteHandler Event handler which will be triggered when resource.deleted event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerResourceDeletedConsumer( ResourceHandler resourceDeleteHandler ) throws IOException {

        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, resourceExchangeName, resourceDeletedRoutingKey);
        ResourceDeletedConsumer consumer = new ResourceDeletedConsumer(channel,resourceDeleteHandler );

        log.debug("Delete resource consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer delete resource created!!!" );
    }

    /**
     * Registers consumer for event resource.created. Event will trigger translation of the resource into RDF
     * and writing it into JENA repository.
     *
     * @param resourceHandler Event handler which will be triggered when resource.created event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerResourceUpdatedConsumer(ResourceHandler resourceHandler) throws IOException {

        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, resourceExchangeName, resourceModifiedRoutingKey);
        ResourceModifiedConsumer consumer = new ResourceModifiedConsumer(channel, resourceHandler );

        log.debug("Creating resource modified consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer resource modified created!!!" );
    }


    /**
     * Registers consumer for event resource.searchRequested. Event will trigger translation of the request into SPARQL
     * and executing it in JENA repository.
     *
     * @param searchHandler Event handler which will be triggered when resource.searchRequested event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerResourceSearchConsumer( SearchHandler searchHandler ) throws IOException {
        Channel channel = connection.createChannel();
        String queueName = channel.queueDeclare().getQueue();
        channel.queueBind(queueName, resourceExchangeName, resourceSearchRequestedRoutingKey );

        SearchRequestedConsumer consumer = new SearchRequestedConsumer(channel, searchHandler );

        log.debug("Creating search consumer");
        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer search created!!!" );
    }
}
