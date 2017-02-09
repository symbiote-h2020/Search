package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.model.Resource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Consumer of the resource created event. Handler creates RDF representation of provided resource and adds it into
 * repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class ResourceCreatedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ResourceCreatedConsumer.class);

    private final ResourceHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public ResourceCreatedConsumer(Channel channel, ResourceHandler handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume resource created message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            Resource resource = mapper.readValue(msg, Resource.class);

            boolean success = handler.registerResource(resource);
            log.debug(success?
                    "Registration of the resource in RDF is success"
                    :"Registration of the resource in RDF failed");

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}