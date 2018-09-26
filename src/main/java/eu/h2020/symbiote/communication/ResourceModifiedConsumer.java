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
public class ResourceModifiedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ResourceModifiedConsumer.class);

    private final ResourceHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public ResourceModifiedConsumer(Channel channel, ResourceHandler handler, ThreadPoolExecutor writerExecutorService) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume resource update message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreResourceRegisteredOrModifiedEventPayload resource = mapper.readValue(msg, CoreResourceRegisteredOrModifiedEventPayload.class);

            long before = System.currentTimeMillis();

            Callable<Boolean> callable = () -> {
                boolean success = handler.updateResource(resource);

                try {
                    CoreSspResourceRegisteredOrModifiedEventPayload sspRes = mapper.readValue(msg, CoreSspResourceRegisteredOrModifiedEventPayload.class);
                    if (success) {
                        handler.addSdevResourceServiceLink(sspRes);
                    }
                } catch ( Exception e ) {
                    //Ignore error
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
