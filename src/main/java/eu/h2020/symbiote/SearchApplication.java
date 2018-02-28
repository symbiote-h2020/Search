package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.ranking.AvailabilityManager;
import eu.h2020.symbiote.ranking.PopularityManager;
import eu.h2020.symbiote.ranking.RankingHandler;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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


/**
 * Created by mateuszl on 22.09.2016.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SearchApplication {

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
        private final SecurityManager securityManager;
        private final RankingHandler rankingHandler;
        private final boolean securityEnabled;
        private final boolean rankingEnabled;

        @Autowired
        public CLR(RabbitManager manager, PopularityManager popularityManager, AvailabilityManager availabilityManager, AccessPolicyRepo accessPolicyRepo, SecurityManager securityManager, RankingHandler rankingHandler,
                   @Value("${search.security.enabled}") boolean securityEnabled, @Value("${search.ranking.enabled}") boolean rankingEnabled) {
            this.manager = manager;
            this.popularityManager = popularityManager;
            this.availabilityManager = availabilityManager;
            this.accessPolicyRepo = accessPolicyRepo;
            this.securityManager = securityManager;
            this.rankingHandler = rankingHandler;
            this.securityEnabled = securityEnabled;
            this.rankingEnabled = rankingEnabled;
        }

        @Override
        public void run(String... args) throws Exception {
            SearchStorage searchStorage = getDefaultStorage(securityManager,securityEnabled);

            PlatformHandler platformHandler = new PlatformHandler( searchStorage );
            manager.registerPlatformCreatedConsumer(platformHandler);

            manager.registerPlatformDeletedConsumer(platformHandler);

            manager.registerPlatformUpdatedConsumer(platformHandler);

            ResourceHandler resourceHandler = new ResourceHandler(searchStorage, this.accessPolicyRepo);
            manager.registerResourceCreatedConsumer(resourceHandler);

            manager.registerResourceDeletedConsumer(resourceHandler);

            manager.registerResourceUpdatedConsumer(resourceHandler);

            SearchHandler searchHandler = new SearchHandler(searchStorage.getTripleStore(), securityEnabled, securityManager, rankingHandler, rankingEnabled );
            manager.registerResourceSearchConsumer(searchHandler);
            manager.registerResourceSparqlSearchConsumer(searchHandler);

            manager.registerPopularityUpdateConsumer(popularityManager);
            manager.registerAvailabilityUpdateConsumer(availabilityManager);

        }
    }

    public static SearchStorage getDefaultStorage(SecurityManager securityManager, boolean securityEnabled) {
        return SearchStorage.getInstance(DIRECTORY,securityManager,securityEnabled);
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
