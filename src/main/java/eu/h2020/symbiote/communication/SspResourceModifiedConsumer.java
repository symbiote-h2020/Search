package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.CoreSspResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.handlers.ResourceHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Consumer of the resource modified event. Handler removes existing resource and creates RDF representation of
 * the new resource.
 *
 * Created by Mael on 17/01/2017.
 */
public class SspResourceModifiedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SspResourceModifiedConsumer.class);

    private final ResourceHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public SspResourceModifiedConsumer(Channel channel, ResourceHandler handler, ThreadPoolExecutor writerExecutorService) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume resource update message: " + msg );

        //Try to parse the message

        long before = System.currentTimeMillis();

        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreSspResourceRegisteredOrModifiedEventPayload resource = mapper.readValue(msg, CoreSspResourceRegisteredOrModifiedEventPayload.class);

            Callable<Boolean> callable = () -> {
                boolean success = handler.updateResource(resource);
                if( success ) {
                    handler.addSdevResourceServiceLink(resource);
                }

                long after = System.currentTimeMillis();
                log.debug((success ?
                        "Update of the resource in RDF is success"
                        : "Update of the resource in RDF failed") + " in time " + (after - before) + " ms");
                return Boolean.TRUE;
            };
            writerExecutorService.submit(callable);

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}
