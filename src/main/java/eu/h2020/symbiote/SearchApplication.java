package eu.h2020.symbiote;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.communication.SearchCommunicationHandler;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.*;
import eu.h2020.symbiote.mappings.MappingManager;
import eu.h2020.symbiote.model.mim.InformationModel;
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
import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;


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

        WaitForPort.waitForServices(WaitForPort.findProperty("SPRING_BOOT_WAIT_FOR_SERVICES"));
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
                        rankingHandler, mappingManager, rankingEnabled, coreThreads, maxThreads, threadsKeepAlive );
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
            manager.registerMappingCreateConsumer(mappingManager,searchStorage);
            manager.registerMappingDeleteConsumer(mappingManager,searchStorage);
            manager.registerMappingGetAllConsumer(mappingManager);
            manager.registerMappingGetSingleConsumer(mappingManager);

            //Information model handling crud
            manager.registerModelCreateConsumer(platformHandler);
            manager.registerModelDeleteConsumer(platformHandler);
            manager.registerModelUpdateConsumer(platformHandler);



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


//            resourceHandler.deleteResources(Arrays.asList("5c2f517d4f5ab037cf045a72","5c2f517d4f5ab037cf045a74", "5c2f517d4f5ab037cf045a76", "5c2f52f54f5ab037cf045a79", "5c2f52f54f5ab037cf045a7b"));


            //TODO 1. moveDefaultGraph
//            moveDefaultGraph(searchStorage.getTripleStore());
            //TODO 2. moveDefaultGraph
//            searchStorage.getTripleStore().loadModelsToNamedGraphs();
            //TODO 3. send model registrations - from Registry

            //TODO
            //loading interworking services on startup
//            platformHandler.loadAndSaveInterworkingServicesFromTriplestore();



            //TODO testquery
//            testQuery(searchHandler);
            //TODO check model helper
//            log.debug("Loading CIM model test");
//            ModelHelper.readModel(CIM.getURI());

//            deleteResourcesFromGraphs(searchStorage.getTripleStore());

            //TODO add BIM as PIM
//            searchStorage.getTripleStore().addBIMasPIM();

//            registerResourceM1(resourceHandler);
//            registerResourceM2(resourceHandler);



//            registerMappingTestResource(resourceHandler);
//            searchStorage.getTripleStore().loadBaseModel(CIM.getURI());
//            searchStorage.getTripleStore().loadBaseModel(MIM.getURI());
        }

        private void deleteResourcesFromGraphs(TripleStore tripleStore) {
            UpdateRequest req = UpdateFactory.create();
            req.add(generateResourceRemoval("5bc9ab084f5ab05629acc62a","http://www.symbiote-h2020.eu/defaultGraph"));
            req.add(generateResourceRemoval("5bc9ab074f5ab05629acc628","http://www.symbiote-h2020.eu/defaultGraph"));
            req.add(generateResourceRemoval("5bc9ab084f5ab05629acc62c","http://www.symbiote-h2020.eu/defaultGraph"));
            req.add(generateResourceRemoval("5bc9aadb4f5ab05629acc624","http://www.symbiote-h2020.eu/defaultGraph"));
            req.add(generateResourceRemoval("5bc9ab064f5ab05629acc626","http://www.symbiote-h2020.eu/defaultGraph"));
            tripleStore.executeUpdate(req);
        }

//        private void sendModelRegistrations(PlatformHandler platformHandler) {
//
//            try (MongoClient client = new MongoClient()) {
//                MongoDatabase database = client.getDatabase("symbiote-core-registry-database");
//
////                MongoClient mongoClient = new MongoClient(connectionString);
//
//                CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
//                        org.bson.codecs.configuration.CodecRegistries.fromProviders(Pojo);
//                MongoDatabase database = mongoClient.getDatabase("testdb").withCodecRegistry(pojoCodecRegistry);
////                CodecRegistry pojoCodecRegistry = MongoClient.getDefaultCodecRegistry();
////                pojoCodecRegistry.
////                        fromProviders(PojoCodecProvider.builder().automatic(true).build()));
////
////                database.withCodecRegistry(pojoRegistry);
//
//
//                MongoCollection<InformationModel> informationModels = database.getCollection("informationModel", InformationModel.class);
//                try (MongoCursor<InformationModel> iterator = informationModels.find().iterator() ) {
//                    while( iterator.hasNext() ) {
//                        InformationModel next = iterator.next();
//                        if( !next.getId().equals("BIM") ) {
//                            System.out.print("Adding information model: " + next.getId() + " uri: " + next.getUri());
//                            platformHandler.registerInformationModel(next);
//                        } else {
//                            System.out.println("Skipping BIM");
//                        }
//                    }
//                }
//            }
//
//        }

        private void testQuery(ISearchEvents searchHandler) {
            log.debug("Testing query");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SearchCommunicationHandler comm = new SearchCommunicationHandler("1",null,null,null,null);

            CoreSparqlQueryRequest req = new CoreSparqlQueryRequest();
            req.setBody("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> SELECT * FROM <http://www.symbiote-h2020.eu/ontology/internal/models/5bab4c3b4f5ab06e05ecf0af> WHERE { ?s ?p ?o .}");
            searchHandler.sparqlSearch(comm,req);
            log.debug("Search started");
        }

//        private void registerResourceM1( ResourceHandler resourceHandler ) {
//            CoreResourceRegisteredOrModifiedEventPayload resource = new CoreResourceRegisteredOrModifiedEventPayload();
//
//            resource.setPlatformId("psnc_PlatformM1");
//
//            CoreResource coreRes = new CoreResource();
//            coreRes.setRdfFormat(RDFFormat.JSONLD);
//            try {
//                coreRes.setRdf(FileUtils.readFileToString(new File("/home/mael/resm1.json"), Charset.defaultCharset()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            coreRes.setId("psnc_p1res1");
//            coreRes.setInterworkingServiceURL("https://www.psnc.eu/platformM1");
//            coreRes.setName("psnc_p1res1");
//            coreRes.setType(CoreResourceType.STATIONARY_SENSOR);
//            resource.setResources(Arrays.asList(coreRes));
//
//            resourceHandler.registerResource(resource);
//        }
//
//        private void registerResourceM2( ResourceHandler resourceHandler ) {
//            CoreResourceRegisteredOrModifiedEventPayload resource = new CoreResourceRegisteredOrModifiedEventPayload();
//
//            resource.setPlatformId("psnc_PlatformM2");
//
//            CoreResource coreRes = new CoreResource();
//            coreRes.setRdfFormat(RDFFormat.JSONLD);
//            try {
//                coreRes.setRdf(FileUtils.readFileToString(new File("/home/mael/resm2.json"), Charset.defaultCharset()));
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            coreRes.setId("psnc_p2res2");
//            coreRes.setInterworkingServiceURL("https://www.psnc.eu/platformM2");
//            coreRes.setName("psnc_p2res2");
//            coreRes.setType(CoreResourceType.STATIONARY_SENSOR);
//            resource.setResources(Arrays.asList(coreRes));
//
//            resourceHandler.registerResource(resource);
//        }


        private String generateResourceRemoval(String resourceId, String graph) {
            StringBuilder q = new StringBuilder();
            q.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
            q.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
            q.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
            q.append("WITH <" + graph + "> ");
            q.append("DELETE { ?sensor ?p ?o . \n");
//        q.append(" \t?o ?p1 ?o1 . \n");
            q.append(" \t?service mim:hasResource ?sensor . \n");
            q.append(" \t?foi ?foip ?foio . \n");

            q.append("} WHERE {\n");
            q.append("\t?sensor ?p ?o ;\n");
            q.append("\t\tcim:id \"" + resourceId + "\" .\n");
            q.append("\t?service mim:hasResource ?sensor .\n");

            q.append("OPTIONAL {");
            q.append("\t?foi a cim:FeatureOfInterest .\n");
            q.append("\t?sensor cim:hasFeatureOfInterest ?foi .\n");
            q.append("\t?foi ?foip ?foio .\n");
            q.append("}");

            q.append("}");
            return q.toString();
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
        log.debug(">>>>>>>>>> MOVING TO NAMED GRAPH <<<<<<<<<");

        UpdateRequest req = UpdateFactory.create();
        req.add("MOVE DEFAULT TO <"+TripleStore.DEFAULT_GRAPH+">");
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
