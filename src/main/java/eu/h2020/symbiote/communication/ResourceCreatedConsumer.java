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
import org.apache.commons.lang.StringUtils;
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
public class ResourceCreatedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ResourceCreatedConsumer.class);

    private static Log timerLog = LogFactory.getLog("TimerLog");

    private final ResourceHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public ResourceCreatedConsumer(Channel channel, ResourceHandler handler, ThreadPoolExecutor writerExecutorService) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume resource created message: " + msg );

        //Try to parse the message

        try {

            long before = System.currentTimeMillis();

            ObjectMapper mapper = new ObjectMapper();
            Callable<Boolean> callable = () -> {

                CoreResourceRegisteredOrModifiedEventPayload coreRes = null;
                CoreSspResourceRegisteredOrModifiedEventPayload sspRes = null;

                try {
                    coreRes = mapper.readValue(msg, CoreResourceRegisteredOrModifiedEventPayload.class);
                } catch(Exception e ) {
                    log.debug("This is not a core resource");
                }
                try {
                    sspRes = mapper.readValue(msg, CoreSspResourceRegisteredOrModifiedEventPayload.class);
                    if(StringUtils.isEmpty(sspRes.getSdevId())) {
                        sspRes= null;
                    }
                } catch(Exception e ) {
                    log.debug("This is not a ssp resource");
                }

                boolean success = false;
                if( sspRes != null ) {
                    success = handler.registerResource(sspRes);
                    log.debug("Registration is running for ssp!");
                    if( success ) {
                        handler.addSdevResourceServiceLink(sspRes);
                    }
                } else if ( coreRes != null ) {
                    success = handler.registerResource(coreRes);
                    log.debug("Registration is running for core res");
                }

                long after = System.currentTimeMillis();
                log.debug((success ?
                        "Registration of the resource in RDF is success"
                        : "Registration of the resource in RDF failed") + " in time " + (after - before) + " ms");
                return Boolean.TRUE;
            };
            writerExecutorService.submit(callable);

//        } catch( JsonParseException | JsonMappingException e ) {
//            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
//        } catch( IOException e ) {
//            log.error("I/O Exception occurred when parsing Resource object" , e);
        } catch( Exception e ) {
            log.error("Generic occurred when handling rdf resource registration: " + msg, e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }
}
