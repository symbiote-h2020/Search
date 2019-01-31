package eu.h2020.symbiote.communication;

import com.rabbitmq.client.Channel;
import eu.h2020.symbiote.handlers.ISearchEvents;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.mappings.MappingManager;
import eu.h2020.symbiote.ranking.AvailabilityManager;
import eu.h2020.symbiote.ranking.PopularityManager;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

/**
 * Bean used to manage internal communication using RabbitMQ.
 * It is responsible for declaring exchanges and using routing keys from centralized config server.
 */
@Component
public class RabbitManager {

    public static final String MONITORING_EXCHANGE = "symbIoTe.CoreResourceMonitor.exchange.out";
    public static final String MONITORING_ROUTING_KEY = "monitoring";
    private static Log log = LogFactory.getLog(RabbitManager.class);

    private Map<String,Object> queueArgs;

    @Value("${rabbit.host}")
    private String rabbitHost;

    @Value("${rabbit.username}")
    private String rabbitUsername;

    @Value("${rabbit.password}")
    private String rabbitPassword;

    @Value("${search.spring.rabbitmq.template.reply-timeout}")
    private Integer rabbitMessageTimeout;

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
    @Value("${rabbit.routingKey.resource.sparqlSearchRequested}")
    private String resourceSparqlSearchRequestedRoutingKey;
    @Value("${rabbit.routingKey.resource.sparqlSearchPerformed}")
    private String resourceSparqlSearchPerformedRoutingKey;


    //Popularity keys
    @Value("${rabbit.exchange.search.name}")
    private String exchangeSearchName;
    @Value("${rabbit.exchange.search.type}")
    private String exchangeSearchType;
    @Value("${rabbit.exchange.search.durable}")
    private boolean exchangeSearchDurable;
    @Value("${rabbit.exchange.search.autodelete}")
    private boolean exchangeSearchAutodelete;
    @Value("${rabbit.exchange.search.internal}")
    private boolean exchangeSearchInternal;
    @Value("${rabbit.routingKey.search.popularityUpdates}")
    private String popularityUpdatesRoutingKey;

    /* Information Model messages Params */
    @Value("${rabbit.routingKey.platform.model.created}")
    private String informationModelCreatedRoutingKey;
    @Value("${rabbit.routingKey.platform.model.removed}")
    private String informationModelRemovedRoutingKey;
    @Value("${rabbit.routingKey.platform.model.modified}")
    private String informationModelModifiedRoutingKey;

    //Ssp exchange
    @Value("${rabbit.exchange.ssp.name}")
    private String sspExchangeName;
    @Value("${rabbit.exchange.ssp.type}")
    private String sspExchangeType;
    @Value("${rabbit.exchange.ssp.durable}")
    private boolean sspExchangeDurable;
    @Value("${rabbit.exchange.ssp.autodelete}")
    private boolean sspExchangeAutodelete;
    @Value("${rabbit.exchange.ssp.internal}")
    private boolean sspExchangeInternal;

    //Ssp keys
    @Value("${rabbit.routingKey.ssp.created}")
    private String sspCreatedRoutingKey;
    @Value("${rabbit.routingKey.ssp.removed}")
    private String sspRemovedRoutingKey;
    @Value("${rabbit.routingKey.ssp.modified}")
    private String sspModifiedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.created}")
    private String sspSdevCreatedRoutingKey;
    @Value("${rabbit.routingKey.ssp.sdev.removed}")
    private String sspSdevRemovedRoutingKey;
    @Value("${rabbit.routingKey.ssp.sdev.modified}")
    private String sspSdevModifiedRoutingKey;

    @Value("${rabbit.routingKey.ssp.sdev.resource.created}")
    private String sspSdevResourceCreatedRoutingKey;
    @Value("${rabbit.routingKey.ssp.sdev.resource.removed}")
    private String sspSdevResourceRemoveddRoutingKey;
    @Value("${rabbit.routingKey.ssp.sdev.resource.modified}")
    private String sspSdevResourceModifiedRoutingKey;

    //Mapping keys
    @Value("${rabbit.exchange.mapping.name}")
    private String mappingsExchangeName;
    @Value("${rabbit.exchange.mapping.type}")
    private String mappingsExchangeType;
    @Value("${rabbit.exchange.mapping.durable}")
    private boolean mappingsExchangeDurable;
    @Value("${rabbit.exchange.mapping.autodelete}")
    private boolean mappingsExchangeAutodelete;
    @Value("${rabbit.exchange.mapping.internal}")
    private boolean mappingsExchangeInternal;

    @Value("${rabbit.routingKey.mapping.getAllMappingsRequested}")
    private String mappingsGetAllRoutingKey;
    @Value("${rabbit.routingKey.mapping.getSingleMappingRequested}")
    private String mappingsGetSingleRoutingKey;
    @Value("${rabbit.routingKey.mapping.creationRequested}")
    private String mappingsCreationRequestedRoutingKey;
    @Value("${rabbit.routingKey.mapping.removalRequested}")
    private String mappingsRemovalRequestedRoutingKey;

    private org.springframework.amqp.rabbit.connection.Connection connection;
    private RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitManager( RabbitTemplate rabbitTemplate ) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Initialization method. Used to create global connection used by all communication within the component and
     * all global exchanges.
     */
    @PostConstruct
    private void init() throws InterruptedException {
        //FIXME check if there is better exception handling in @postconstruct method

        log.debug("rabbitMessageTimeout: " + rabbitMessageTimeout);
        Channel channel = null;

        //setting up ttl args
        queueArgs = new HashMap<>();
        queueArgs.put("x-message-ttl", rabbitMessageTimeout);

        try {
            org.springframework.amqp.rabbit.connection.ConnectionFactory factory = this.rabbitTemplate.getConnectionFactory();

            this.connection = factory.createConnection();

            channel = this.connection.createChannel(false);
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

            channel.exchangeDeclare(this.exchangeSearchName,
                    this.exchangeSearchType,
                    this.exchangeSearchDurable,
                    this.exchangeSearchAutodelete,
                    this.exchangeSearchInternal,
                    null);

            channel.exchangeDeclare(this.sspExchangeName,
                    this.sspExchangeType,
                    this.sspExchangeDurable,
                    this.sspExchangeAutodelete,
                    this.sspExchangeInternal,
                    null);


            channel.exchangeDeclare(MONITORING_EXCHANGE,
                    "direct",
                    true,
                    false,
                    false,
                    null);

            channel.exchangeDeclare(this.mappingsExchangeName,
                    this.mappingsExchangeType,
                    this.mappingsExchangeDurable,
                    this.mappingsExchangeAutodelete,
                    this.mappingsExchangeInternal,
                    null);

            //message retrieval
            //receiveMessages();

            // message to Search Service

        } catch (IOException e) {
            e.printStackTrace();
//        } catch (TimeoutException e) {
//            e.printStackTrace();
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
        } catch ( AmqpException e) {
            log.fatal("Error when cleaning up connections: " + e.getMessage(), e);
        }
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
    public void registerPlatformCreatedConsumer(PlatformHandler platformHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-platform-created";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, platformExchangeName, platformCreatedRoutingKey);
        PlatformCreatedConsumer consumer = new PlatformCreatedConsumer(channel, platformHandler, executor );

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
    public void registerPlatformDeletedConsumer( PlatformHandler platformHandler, ThreadPoolExecutor executor  ) throws IOException {
        String queueName = "symbIoTe-Search-platform-deleted";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, platformExchangeName, platformDeletedRoutingKey);
        PlatformDeletedConsumer consumer = new PlatformDeletedConsumer(channel,platformHandler, executor );

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
    public void registerPlatformUpdatedConsumer(PlatformHandler platformHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-platform-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, platformExchangeName, platformModifiedRoutingKey);
        PlatformModifiedConsumer consumer = new PlatformModifiedConsumer(channel, platformHandler, executor );

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
    public void registerResourceCreatedConsumer( ResourceHandler resourceHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-resource-created";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, resourceCreatedRoutingKey);
        ResourceCreatedConsumer consumer = new ResourceCreatedConsumer(channel, resourceHandler, executor );

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
    public void registerResourceDeletedConsumer( ResourceHandler resourceDeleteHandler, ThreadPoolExecutor executor  ) throws IOException {
        String queueName = "symbIoTe-Search-resource-deleted";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, resourceDeletedRoutingKey);
        ResourceDeletedConsumer consumer = new ResourceDeletedConsumer(channel,resourceDeleteHandler, executor );

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
    public void registerResourceUpdatedConsumer(ResourceHandler resourceHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-resource-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, resourceModifiedRoutingKey);
        ResourceModifiedConsumer consumer = new ResourceModifiedConsumer(channel, resourceHandler, executor );

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
    public void registerResourceSearchConsumer( ISearchEvents searchHandler ) throws IOException {
        String queueName = "symbIoTe-Search-search-requested";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, resourceSearchRequestedRoutingKey );

        SearchRequestedConsumer consumer = new SearchRequestedConsumer(channel, searchHandler );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer search created!!!" );
    }

    /**
     * Registers consumer for event resource.searchRequested. Event will trigger translation of the request into SPARQL
     * and executing it in JENA repository.
     *
     * @param searchHandler Event handler which will be triggered when resource.searchRequested event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSingleThreadResourceSearchConsumer( ISearchEvents searchHandler ) throws IOException {
        String queueName = "symbIoTe-Search-search-requested";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, resourceSearchRequestedRoutingKey );

        SingleThreadSearchRequestedConsumer consumer = new SingleThreadSearchRequestedConsumer(channel, searchHandler );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer search created!!!" );
    }

    /**
     * Registers consumer for event resource.sparqlSearchRequested. Event will trigger translation of the request into SPARQL
     * and executing it in JENA repository.
     *
     * @param searchHandler Event handler which will be triggered when resource.sparqlSearchRequested event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerResourceSparqlSearchConsumer( ISearchEvents searchHandler ) throws IOException {
        String queueName = "symbIoTe-Search-sparqlSearch-requested";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, resourceSparqlSearchRequestedRoutingKey );

        SparqlSearchRequestedConsumer consumer = new SparqlSearchRequestedConsumer(channel, searchHandler );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer search created!!!" );
    }

    public void registerPopularityUpdateConsumer(PopularityManager popularityManager) throws IOException {
        String queueName = "symbIoTe-Search-popularity-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, exchangeSearchName, popularityUpdatesRoutingKey );

        PopularityUpdatesConsumer consumer = new PopularityUpdatesConsumer(channel,popularityManager);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer popularity created!!!" );
    }

    public void registerAvailabilityUpdateConsumer(AvailabilityManager availabilityManager) throws IOException {
        String queueName = "symbIoTe-Search-availability-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, MONITORING_EXCHANGE, MONITORING_ROUTING_KEY);

        AvailabilityUpdatesConsumer consumer = new AvailabilityUpdatesConsumer(channel,availabilityManager);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer availability created!!!" );
    }


    //SSP section

    //Ssp CRUD
    /**
     * Registers consumer for event ssp.created. Event will trigger translation of the ssp into RDF
     * and writing it into JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when ssp.created event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSspCreatedConsumer( PlatformHandler platformHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-ssp-created";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, sspExchangeName, sspCreatedRoutingKey);
        SspCreatedConsumer consumer = new SspCreatedConsumer(channel, platformHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer ssp created!!!" );
    }

    /**
     * Registers consumer for event ssp.deleted. Event will trigger translation of the request into SPARQL UPDATE
     * and executing it in JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when ssp.deleted event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSspDeletedConsumer( PlatformHandler platformHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-ssp-deleted";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, sspExchangeName, sspRemovedRoutingKey);
        SspDeletedConsumer consumer = new SspDeletedConsumer(channel,platformHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer delete ssp created!!!" );
    }

    /**
     * Registers consumer for event ssp.updated. Event will trigger translation of the ssp into RDF
     * and writing it into JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when ssp.updated event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSspUpdatedConsumer(PlatformHandler platformHandler,ThreadPoolExecutor executor) throws IOException {
        String queueName = "symbIoTe-Search-ssp-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, sspExchangeName, sspModifiedRoutingKey);
        SspModifiedConsumer consumer = new SspModifiedConsumer(channel, platformHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer ssp modified created!!!" );
    }

    //SDEV crud
    /**
     * Registers consumer for event sdev.created. Event will trigger translation of the sdev into RDF
     * and writing it into JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when platform.created event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSdevCreatedConsumer( PlatformHandler platformHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-sdev-created";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, sspExchangeName, sspSdevCreatedRoutingKey);
        SdevCreatedConsumer consumer = new SdevCreatedConsumer(channel, platformHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer sdev created!!!" );
    }

    /**
     * Registers consumer for event sdev.deleted. Event will trigger translation of the request into SPARQL UPDATE
     * and executing it in JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when sdev.deleted event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSdevDeletedConsumer( PlatformHandler platformHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-sdev-deleted";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, sspExchangeName, sspSdevRemovedRoutingKey);
        SdevDeletedConsumer consumer = new SdevDeletedConsumer(channel,platformHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer delete sdev created!!!" );
    }

    /**
     * Registers consumer for event sdev.updated. Event will trigger translation of the sdev into RDF
     * and writing it into JENA repository.
     *
     * @param platformHandler Event handler which will be triggered when sdev.updated event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSdevUpdatedConsumer(PlatformHandler platformHandler, ThreadPoolExecutor executor) throws IOException {
        String queueName = "symbIoTe-Search-sdev-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, sspExchangeName, sspSdevModifiedRoutingKey);
        SdevModifiedConsumer consumer = new SdevModifiedConsumer(channel, platformHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer sdev modified created!!!" );
    }

    //SSP resource CRUD
    /**
     * Registers consumer for event sspresource.created. Event will trigger writing it into JENA repository.
     *
     * @param resourceHandler Event handler which will be triggered when sspresource.created event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSspResourceCreatedConsumer( ResourceHandler resourceHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-ssp-resource-created";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, sspSdevResourceCreatedRoutingKey);
        SspResourceCreatedConsumer consumer = new SspResourceCreatedConsumer(channel, resourceHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer sspResourceCreated created!!!" );
    }

    /**
     * Registers consumer for event sspResource.deleted. Event will trigger translation of the request into SPARQL UPDATE
     * and executing it in JENA repository.
     *
     * @param resourceHandler Event handler which will be triggered when sspResource.deleted event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSspResourceDeletedConsumer( ResourceHandler resourceHandler, ThreadPoolExecutor executor ) throws IOException {
        String queueName = "symbIoTe-Search-ssp-resource-deleted";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, sspSdevResourceRemoveddRoutingKey);
        ResourceDeletedConsumer consumer = new ResourceDeletedConsumer(channel,resourceHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer delete sspResource created!!!" );
    }

    /**
     * Registers consumer for event sspResource.updated. Event will trigger translation of the resource into RDF
     * and writing it into JENA repository.
     *
     * @param resourceHandler Event handler which will be triggered when sspResource.updated event is received.
     * @throws IOException In case there are problems with RabbitMQ connections.
     */
    public void registerSspResourceUpdatedConsumer(ResourceHandler resourceHandler, ThreadPoolExecutor executor) throws IOException {
        String queueName = "symbIoTe-Search-ssp-resource-updated";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, resourceExchangeName, sspSdevResourceModifiedRoutingKey);
        SspResourceModifiedConsumer consumer = new SspResourceModifiedConsumer(channel, resourceHandler, executor );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer sspResource modified created!!!" );
    }

    public void registerMappingGetSingleConsumer(MappingManager mappingManager) throws IOException {
        String queueName = "symbIoTe-Search-mapping-get-single";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, mappingsExchangeName, mappingsGetSingleRoutingKey );
        MappingFindOneConsumer consumer = new MappingFindOneConsumer(channel,mappingManager);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer mappings find one created!!!" );
    }

    public void registerMappingGetAllConsumer(MappingManager mappingManager) throws IOException {
        String queueName = "symbIoTe-Search-mapping-get-all";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, mappingsExchangeName, mappingsGetAllRoutingKey );
        MappingFindAllConsumer consumer = new MappingFindAllConsumer(channel,mappingManager);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer mappings find all created!!!" );
    }

    public void registerMappingCreateConsumer(MappingManager mappingManager, SearchStorage searchStorage) throws IOException {
        String queueName = "symbIoTe-Search-mapping-create";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, mappingsExchangeName, mappingsCreationRequestedRoutingKey);
        MappingCreateConsumer consumer = new MappingCreateConsumer(channel,mappingManager,searchStorage);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer mappings create created!!!" );
    }

    public void registerMappingDeleteConsumer(MappingManager mappingManager, SearchStorage searchStorage ) throws IOException {
        String queueName = "symbIoTe-Search-mapping-delete";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, mappingsExchangeName, mappingsRemovalRequestedRoutingKey);
        MappingDeleteConsumer consumer = new MappingDeleteConsumer(channel,mappingManager,searchStorage );

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer mappings delete created!!!" );
    }

    public void registerModelCreateConsumer(PlatformHandler platformHandler) throws IOException {
        String queueName = "symbIoTe-Search-model-create";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, platformExchangeName, informationModelCreatedRoutingKey);
        ModelCreatedConsumer consumer = new ModelCreatedConsumer(channel,platformHandler);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer model create created!!!" );
    }

    public void registerModelUpdateConsumer(PlatformHandler platformHandler) throws IOException {
        String queueName = "symbIoTe-Search-model-update";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, platformExchangeName, informationModelModifiedRoutingKey);
        ModelModifiedConsumer consumer = new ModelModifiedConsumer(channel,platformHandler);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer model modified created!!!" );
    }

    public void registerModelDeleteConsumer(PlatformHandler platformHandler) throws IOException {
        String queueName = "symbIoTe-Search-model-delete";

        Channel channel = connection.createChannel(false);
        channel.queueDeclare(queueName, false, true, true, queueArgs);
        channel.queueBind(queueName, platformExchangeName, informationModelRemovedRoutingKey);
        ModelDeletedConsumer consumer = new ModelDeletedConsumer(channel,platformHandler);

        channel.basicConsume(queueName, false, consumer);
        log.debug( "Consumer model deleted created!!!" );
    }

    public org.springframework.amqp.rabbit.connection.Connection getConnection() {
        return this.connection;
    }

}
