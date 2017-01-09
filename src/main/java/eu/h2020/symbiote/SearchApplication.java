package eu.h2020.symbiote;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.web.bind.annotation.RestController;


/**
 * Created by mateuszl on 22.09.2016.
 */
@EnableDiscoveryClient
@SpringBootApplication
public class SearchApplication {

    public static final String DIRECTORY = "/corePlatformTriplestore";

	private static Log log = LogFactory.getLog(SearchApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SearchApplication.class, args);

        try {
            // Subscribe to RabbitMQ messages
        } catch (Exception e) {
            log.error("Error occured during subscribing from Search", e);
        }
    }

//    //TODO whats this
//    @Bean
//    public AlwaysSampler defaultSampler() {
//        return new AlwaysSampler();
//    }

    @RestController
    class RegistrationController {


        public RegistrationController() {
        }
    }

}
