package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.cloud.model.ssp.SspRegInfo;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.mim.SmartSpace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Consumer of the sdev deleted event. Handler removes RDF representation of specified sdev.
 *
 * Created by Szymon Mueller on 25/05/2018.
 */
public class SdevDeletedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SdevDeletedConsumer.class);

    private final PlatformHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     * @param handler handler to be used by the consumer.
     */
    public SdevDeletedConsumer(Channel channel, PlatformHandler handler ) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume sdev deleted message: " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            SspRegInfo sdev = mapper.readValue(msg, SspRegInfo.class);

            boolean success = handler.deleteSdev(sdev.getSymId());
            log.debug(success?
                    "Sdev " + sdev.getSymId() + " deleted successfully"
                    :"Sdev " + sdev.getSymId() + " is reported to not be deleted");

            handler.printStorage();

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Sdev object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Sdev object" , e);
        }

        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
