package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.*;
import eu.h2020.symbiote.mappings.MappingManager;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.SmartSpace;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.ranking.AvailabilityManager;
import eu.h2020.symbiote.ranking.PopularityManager;
import eu.h2020.symbiote.ranking.RankingHandler;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;


/**
 * Created by mateuszl on 22.09.2016.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SearchApplication {


    @Value("${rabbit.host}")
    private String rabbitHost;

    @Value("${rabbit.username}")
    private String rabbitUsername;

    @Value("${rabbit.password}")
    private String rabbitPassword;

    public static final String DIRECTORY = System.getProperty("user.home") +File.separator+ "coreSearchTriplestore";
//    public static final String DIRECTORY = "F:\\stressTest\\2";

	private static Log log = LogFactory.getLog(SearchApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SearchApplication.class, args);
    }

    @Component
    public static class CLR implements CommandLineRunner {

        private final RabbitManager manager;

        private final PopularityManager popularityManager;
        private final AvailabilityManager availabilityManager;
        private final AccessPolicyRepo accessPolicyRepo;
        private final InterworkingServiceInfoRepo interworkingServiceInfoRepo;
        private final SecurityManager securityManager;
        private final MappingManager mappingManager;
        private final RankingHandler rankingHandler;
        private final boolean searchMultithread;
        private final boolean securityEnabled;
        private final boolean rankingEnabled;
        private final int coreThreads;
        private final int maxThreads;
        private final int threadsKeepAlive;

        private final int writerExecutorCoreThreads;
        private final int writerExecutorMaxThreads;
        private final int writerExecutorKeepAliveInMinutes;

        private final ThreadPoolExecutor writerExecutorService;

        @Autowired
        public CLR(RabbitTemplate rabbitTemplate, RabbitManager manager, PopularityManager popularityManager, AvailabilityManager availabilityManager, AccessPolicyRepo accessPolicyRepo, InterworkingServiceInfoRepo interworkingServiceInfoRepo, SecurityManager securityManager, RankingHandler rankingHandler,
                   MappingManager mappingManager,
                   @Value("${search.multithreading}") boolean searchMultithread, @Value("${search.security.enabled}") boolean securityEnabled, @Value("${search.ranking.enabled}") boolean rankingEnabled,
                   @Value("${search.multithreading.coreThreads}") int coreThreads, @Value("${search.multithreading.maxThreads}") int maxThreads, @Value("${search.multithreading.keepAliveInMinutes}") int threadsKeepAlive,
                   @Value("${search.multithreading.writer.coreThreads}") int writerExecutorCoreThreads, @Value("${search.multithreading.writer.maxThreads}") int writerExecutorMaxThreads, @Value("${search.multithreading.writer.keepAliveInMinutes}") int writerExecutorKeepAliveInMinutes) {
            this.manager = manager;
            this.popularityManager = popularityManager;
            this.availabilityManager = availabilityManager;
            this.accessPolicyRepo = accessPolicyRepo;
            this.interworkingServiceInfoRepo = interworkingServiceInfoRepo;
            this.securityManager = securityManager;
            this.mappingManager = mappingManager;
            this.rankingHandler = rankingHandler;
            this.securityEnabled = securityEnabled;
            this.rankingEnabled = rankingEnabled;
            this.searchMultithread = searchMultithread;
            this.coreThreads=coreThreads;
            this.maxThreads = maxThreads;
            this.threadsKeepAlive = threadsKeepAlive;

            this.writerExecutorCoreThreads = writerExecutorCoreThreads;
            this.writerExecutorMaxThreads = writerExecutorMaxThreads;
            this.writerExecutorKeepAliveInMinutes = writerExecutorKeepAliveInMinutes;

            this.writerExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(writerExecutorCoreThreads);
            writerExecutorService.setMaximumPoolSize(writerExecutorMaxThreads);
            writerExecutorService.setKeepAliveTime(writerExecutorKeepAliveInMinutes, TimeUnit.MINUTES);
        }

        @Override
        public void run(String... args) throws Exception {
            SearchStorage searchStorage = getDefaultStorage(securityManager,securityEnabled);

            PlatformHandler platformHandler = new PlatformHandler( searchStorage, interworkingServiceInfoRepo );
            manager.registerPlatformCreatedConsumer(platformHandler, writerExecutorService);

            manager.registerPlatformDeletedConsumer(platformHandler, writerExecutorService);

            manager.registerPlatformUpdatedConsumer(platformHandler,writerExecutorService);

            ResourceHandler resourceHandler = new ResourceHandler(searchStorage, this.accessPolicyRepo, interworkingServiceInfoRepo);
            manager.registerResourceCreatedConsumer(resourceHandler, writerExecutorService);

            manager.registerResourceDeletedConsumer(resourceHandler, writerExecutorService);

            manager.registerResourceUpdatedConsumer(resourceHandler, writerExecutorService);

            ISearchEvents searchHandler;
            if( searchMultithread ) {
                searchHandler = new MultiSearchHandler(searchStorage.getTripleStore(), securityEnabled, securityManager,
                        rankingHandler, rankingEnabled, coreThreads, maxThreads, threadsKeepAlive );
                manager.registerResourceSearchConsumer(searchHandler);
            } else {
                searchHandler = new SearchHandler(searchStorage.getTripleStore(), securityEnabled, securityManager, rankingHandler, rankingEnabled );
                manager.registerSingleThreadResourceSearchConsumer(searchHandler);
            }


            manager.registerResourceSparqlSearchConsumer(searchHandler);

            manager.registerPopularityUpdateConsumer(popularityManager);
            manager.registerAvailabilityUpdateConsumer(availabilityManager);

            //SSP
            manager.registerSspCreatedConsumer(platformHandler, writerExecutorService);
            manager.registerSspDeletedConsumer(platformHandler, writerExecutorService);
            manager.registerSspUpdatedConsumer(platformHandler, writerExecutorService);

            manager.registerSdevCreatedConsumer(platformHandler, writerExecutorService);
            manager.registerSdevDeletedConsumer(platformHandler, writerExecutorService);
            manager.registerSdevUpdatedConsumer(platformHandler, writerExecutorService);

            manager.registerSspResourceCreatedConsumer(resourceHandler, writerExecutorService);
            manager.registerSspResourceDeletedConsumer(resourceHandler, writerExecutorService);
            manager.registerSspResourceUpdatedConsumer(resourceHandler, writerExecutorService);

            //Mappings handling crud
            manager.registerMappingCreateConsumer(mappingManager);
            manager.registerMappingDeleteConsumer(mappingManager);
            manager.registerMappingGetAllConsumer(mappingManager);
            manager.registerMappingGetSingleConsumer(mappingManager);

            //Information model handling crud
            manager.registerModelCreateConsumer(platformHandler);
            manager.registerModelDeleteConsumer(platformHandler);
            manager.registerModelUpdateConsumer(platformHandler);

            //TODO
            //loading interworking services on startup
//            platformHandler.loadAndSaveInterworkingServicesFromTriplestore();

//            platformHandler.deleteSsp("5b921e51ef6ecf58cca87812");
//            platformHandler.deleteSdev("5b922212ef6ecf58cca87813");
//
//            platformHandler.deleteSsp("SSP_Navigo");
//
//            Thread.sleep(2000);
//            SmartSpace smartSpace = new SmartSpace();
//            smartSpace.setName("SSP_Navigo");
//            smartSpace.setDescription(Arrays.asList("Smart Space at Navigo"));
//            smartSpace.setId("SSP_Navigo");
//            InterworkingService interworkingService = new InterworkingService();
//            interworkingService.setUrl("https://smartspace.navigotoscana.it");
//            interworkingService.setInformationModelId("BIM");
//            smartSpace.setInterworkingServices( Arrays.asList(interworkingService));
//            platformHandler.registerSsp(smartSpace);



//        deleteNavigoSSP(sspName,"5b921e51ef6ecf58cca87812",sspUrl);
//        Thread.sleep(5000);
//        deleteNavigoSSP(sspName,"5b921e51ef6ecf58cca87813",sspUrl);

            startBlankCleanupScheduler(resourceHandler);


            //TODO moveDefaultGraph
//            moveDefaultGraph(searchStorage.getTripleStore());
        }
    }

    public static SearchStorage getDefaultStorage(SecurityManager securityManager, boolean securityEnabled) {
        return SearchStorage.getInstance(DIRECTORY,securityManager,securityEnabled);
    }

    public static void startBlankCleanupScheduler( ResourceHandler resourceHandler) {
        Runnable callable = () -> resourceHandler.cleanupBlankOrphans();

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        Calendar nowCal = Calendar.getInstance();
        long epochSecondsTomorrow = LocalDate.now().plusDays(1).atTime(2,0).toEpochSecond(ZoneOffset.UTC);
        long epochSecondsNow = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        long l = epochSecondsTomorrow - epochSecondsNow;

        scheduledExecutorService.scheduleAtFixedRate(callable, l ,86400,TimeUnit.SECONDS);

    }

    public static void moveDefaultGraph(TripleStore tripleStore) {
        System.out.println(">>>>>>>>>> MOVING TO NAMED GRAPH <<<<<<<<<");

        UpdateRequest req = UpdateFactory.create();
        req.add("MOVE DEFAULT TO <http://www.symbiote-h2020.eu/defaultGraph>");
        tripleStore.executeUpdate(req);
    }

    @Bean
    Jackson2JsonMessageConverter jackson2JsonMessageConverter() {

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        /**
         * It is necessary to register the GeoJsonModule, otherwise the GeoJsonPoint cannot
         * be deserialized by Jackson2JsonMessageConverter.
         */
        // ObjectMapper mapper = new ObjectMapper();
        // mapper.registerModule(new GeoJsonModule());
        // converter.setJsonObjectMapper(mapper);
        return converter;
    }

    @Bean
    public ConnectionFactory connectionFactory() throws Exception {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(rabbitHost);
        // connectionFactory.setPublisherConfirms(true);
        // connectionFactory.setPublisherReturns(true);
        connectionFactory.setUsername(rabbitUsername);
        connectionFactory.setPassword(rabbitPassword);
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter jackson2JsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

//    @CrossOrigin
//    @RestController
//    class RegistrationController {
//
//        public RegistrationController() {
//        }
//
//        @RequestMapping(value = "/search/sparql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//        public
//        @ResponseBody
//        HttpEntity<String> searchResourcesByParams(@RequestBody QueryRequest query){
//
//            //Do sparql query
//            SearchStorage storage = getDefaultStorage(securityManager, securityEnabled);
//            List<String> listResponse = storage.query(query.getGraphUri(),query.getSparql());
//            System.out.println( "Got response: " + listResponse);
//            ObjectMapper mapper = new ObjectMapper();
//            String response = listResponse.toString();
//            try {
//                response = mapper.writeValueAsString(listResponse);
//            } catch (JsonProcessingException e) {
//                e.printStackTrace();
//            }
//
//            return new ResponseEntity<String>(response, HttpStatus.OK);
//        }
//
//    }

}
