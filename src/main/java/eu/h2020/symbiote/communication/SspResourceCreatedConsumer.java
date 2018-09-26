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
 * Consumer of the resource created event. Handler creates RDF representation of provided resource and adds it into
 * repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class SspResourceCreatedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SspResourceCreatedConsumer.class);

    private final ResourceHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public SspResourceCreatedConsumer(Channel channel, ResourceHandler handler, ThreadPoolExecutor writerExecutorService) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume resource created message: " + msg );

        //Try to parse the message

        long before = System.currentTimeMillis();

        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreSspResourceRegisteredOrModifiedEventPayload resources = mapper.readValue(msg, CoreSspResourceRegisteredOrModifiedEventPayload.class);

            Callable<Boolean> callable = () -> {
                boolean success = handler.registerResource(resources);

                if( success ) {
                    //Additionally add entrances
                    handler.addSdevResourceServiceLink(resources);
                }
                long after = System.currentTimeMillis();
                log.debug((success ?
                        "Registration of the resource in RDF is success"
                        : "Registration of the resource in RDF failed") + " in time " + (after - before) + " ms");
                return Boolean.TRUE;
            };
            writerExecutorService.submit(callable);


        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        } catch( Exception e ) {
            log.error("Generic occurred when handling rdf resource registration: " + msg, e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}
