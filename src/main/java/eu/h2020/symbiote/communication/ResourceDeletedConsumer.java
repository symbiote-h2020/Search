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
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Consumer of the resource delete event.
 *
 * Created by Mael on 17/01/2017.
 */
public class ResourceDeletedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ResourceDeletedConsumer.class);

    private final ResourceHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public ResourceDeletedConsumer(Channel channel, ResourceHandler handler, ThreadPoolExecutor writerExecutorService) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "DeleteResource requested message: " + msg );

        long before = System.currentTimeMillis();

        //Try to parse the message
        ObjectMapper mapper = new ObjectMapper();
        List<String> toDelete = mapper.readValue(msg, new TypeReference<List<String>>() {
        });

        Callable<Boolean> callable = () -> {
            for (String delId : toDelete) {
                log.debug("Deleting resource " + delId);
                handler.deleteResource(delId);
                //Send the response back to the client
                //TODO
                long after = System.currentTimeMillis();

                log.debug("Total delete operation finished and took: " + (after - before ) + " ms");
            }
            return Boolean.TRUE;
        };

        writerExecutorService.submit(callable);

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}

