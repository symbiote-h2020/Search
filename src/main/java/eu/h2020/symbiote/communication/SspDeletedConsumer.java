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

/**
 * Consumer of the ssp deleted event. Handler removes RDF representation of specified ssp.
 *
 * Created by Szymon Mueller on 25/05/2018.
 */
public class SspDeletedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SspDeletedConsumer.class);

    private final PlatformHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     * @param handler handler to be used by the consumer.
     */
    public SspDeletedConsumer(Channel channel, PlatformHandler handler ) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume ssp deleted message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            SmartSpace ssp = mapper.readValue(msg, SmartSpace.class);

            boolean success = handler.deleteSsp(ssp.getId());
            log.debug(success?
                    "Ssp " + ssp.getId() + " deleted successfully"
                    :"Ssp " + ssp.getId() + " is reported to not be deleted");

            handler.printStorage();

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Ssp object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Ssp object" , e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
