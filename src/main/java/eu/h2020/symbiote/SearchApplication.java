package eu.h2020.symbiote;

import eu.h2020.symbiote.communication.RabbitManager;
import eu.h2020.symbiote.handlers.PlatformHandler;
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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by mateuszl on 22.09.2016.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SearchApplication {

    public static final String DIRECTORY = "/coreSearchTriplestore";

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
            SearchStorage searchStorage = SearchStorage.getInstance();

            PlatformHandler platformHandler = new PlatformHandler( searchStorage );
            manager.registerPlatformCreatedConsumer(platformHandler);
        }
    }

    //TODO whats this
    @Bean
    public AlwaysSampler defaultSampler() {
        return new AlwaysSampler();
    }

    @RestController
    class RegistrationController {


        public RegistrationController() {
        }
    }

}
