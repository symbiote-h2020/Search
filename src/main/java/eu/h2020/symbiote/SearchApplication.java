package eu.h2020.symbiote;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.model.QueryRequest;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.sleuth.sampler.AlwaysSampler;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;


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

        @Autowired
        public CLR( RabbitManager manager ) {
            this.manager = manager;
        }

        @Override
        public void run(String... args) throws Exception {
            SearchStorage searchStorage = getDefaultStorage();

            PlatformHandler platformHandler = new PlatformHandler( searchStorage );
            manager.registerPlatformCreatedConsumer(platformHandler);

            manager.registerPlatformDeletedConsumer(platformHandler);

            manager.registerPlatformUpdatedConsumer(platformHandler);

            ResourceHandler resourceHandler = new ResourceHandler(searchStorage);
            manager.registerResourceCreatedConsumer(resourceHandler);

            manager.registerResourceDeletedConsumer(resourceHandler);

            manager.registerResourceUpdatedConsumer(resourceHandler);

            SearchHandler searchHandler = new SearchHandler(searchStorage.getTripleStore() );
            manager.registerResourceSearchConsumer(searchHandler);
            manager.registerResourceSparqlSearchConsumer(searchHandler);

        }
    }

    public static SearchStorage getDefaultStorage() {
        return SearchStorage.getInstance(DIRECTORY);
    }

    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

    @CrossOrigin
    @RestController
    class RegistrationController {

        public RegistrationController() {
        }

        @RequestMapping(value = "/search/sparql", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
        public
        @ResponseBody
        HttpEntity<String> searchResourcesByParams(@RequestBody QueryRequest query){

            //Do sparql query
            SearchStorage storage = getDefaultStorage();
            List<String> listResponse = storage.query(query.getGraphUri(),query.getSparql());
            System.out.println( "Got response: " + listResponse);
            ObjectMapper mapper = new ObjectMapper();
            String response = listResponse.toString();
            try {
                response = mapper.writeValueAsString(listResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return new ResponseEntity<String>(response, HttpStatus.OK);
        }

    }



}
