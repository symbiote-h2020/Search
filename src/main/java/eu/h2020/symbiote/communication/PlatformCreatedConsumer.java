package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

import java.io.IOException;

/**
 * Consumer of the platform created event. Handler creates RDF representation of provided platform and adds it into
 * repository.
 *
 * Created by Mael on 13/01/2017.
 */
public class PlatformCreatedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(PlatformCreatedConsumer.class);

    private final PlatformHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     */
    public PlatformCreatedConsumer(Channel channel, PlatformHandler handler ) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume platform created message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            Platform platform = mapper.readValue(msg, Platform.class);

            boolean success = handler.registerPlatform(platform);
            log.debug(success?
                    "Registration of the platform in RDF is success"
                    :"Registration of the platform in RDF failed");

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Platform object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Platform object" , e);
        }



        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
