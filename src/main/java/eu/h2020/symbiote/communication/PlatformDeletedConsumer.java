package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.mim.Platform;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Consumer of the platform deleted event. Handler removes RDF representation of specified platform.
 *
 * Created by Mael on 13/01/2017.
 */
public class PlatformDeletedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(PlatformDeletedConsumer.class);

    private final PlatformHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     * @param handler handler to be used by the consumer.
     */
    public PlatformDeletedConsumer(Channel channel, PlatformHandler handler, ThreadPoolExecutor writerExecutorService ) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume platform deleted message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            Platform platform = mapper.readValue(msg, Platform.class);

            Callable<Boolean> callable = () -> {
                boolean success = handler.deletePlatform(platform.getId());
                log.debug(success ?
                        "Platform " + platform.getId() + " deleted successfully"
                        : "Platform " + platform.getId() + " is reported to not be deleted");
                return Boolean.TRUE;
            };

            writerExecutorService.submit(callable);


        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Platform object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Platform object" , e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
