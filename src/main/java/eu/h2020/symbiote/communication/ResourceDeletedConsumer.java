package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.handlers.ResourceHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;

/**
 * Consumer of the search requested event. Translates the message as list of query parameters, translates them into
 * SPARQL and queries the repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class ResourceDeletedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ResourceDeletedConsumer.class);

    private final ResourceHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public ResourceDeletedConsumer(Channel channel, ResourceHandler handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "DeleteResource requested message: " + msg );

        //Try to parse the message
        ObjectMapper mapper = new ObjectMapper();
        List<String> toDelete = mapper.readValue(msg, new TypeReference<List<String>>() {
        });

        for( String delId: toDelete ) {
            log.debug( "Deleting resource " + delId );
            handler.deleteResource(delId);
            //Send the response back to the client
            //TODO

        }
        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}

