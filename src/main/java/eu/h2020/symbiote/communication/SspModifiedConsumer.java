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
import eu.h2020.symbiote.model.mim.SmartSpace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Consumer of the ssp modified event. Handler creates SPARQL Update Delete/Insert representation of modifed
 * ssp and executes it.
 *
 * Created by Szymon Mueller on 25/05/2018.
 */
public class SspModifiedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SspModifiedConsumer.class);

    private final PlatformHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     * @param handler handler to be used by the consumer.
     */
    public SspModifiedConsumer(Channel channel, PlatformHandler handler, ThreadPoolExecutor writerExecutorService ) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume ssp modified message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            SmartSpace ssp = mapper.readValue(msg, SmartSpace.class);

            Callable<Boolean> callable = () -> {
                boolean success = handler.updateSsp(ssp);
                log.debug(success ?
                        "Ssp " + ssp.getId() + " updated successfully"
                        : "Ssp " + ssp.getId() + " is reported to not be updated");
                return Boolean.TRUE;
            };
            writerExecutorService.submit(callable);

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Ssp object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Ssp object" , e);
        }



        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
