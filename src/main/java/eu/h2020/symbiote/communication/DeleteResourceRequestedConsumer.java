package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.handlers.ResourceDeleteHandler;
import eu.h2020.symbiote.handlers.SearchHandler;
import eu.h2020.symbiote.query.SearchRequest;
import eu.h2020.symbiote.query.SearchResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Consumer of the search requested event. Translates the message as list of query parameters, translates them into
 * SPARQL and queries the repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class DeleteResourceRequestedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(DeleteResourceRequestedConsumer.class);

    private final ResourceDeleteHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public DeleteResourceRequestedConsumer(Channel channel, ResourceDeleteHandler handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "DeleteResource requested message: " + msg );

        //Try to parse the message

        handler.deleteResource(msg);
        //Send the response back to the client
        //TODO

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}
