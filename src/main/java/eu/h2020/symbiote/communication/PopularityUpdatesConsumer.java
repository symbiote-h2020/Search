package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import eu.h2020.symbiote.ranking.PopularityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Consumer of the popularity updates.
 *
 * Created by Szymon Mueller on 24/08/2017.
 */
public class PopularityUpdatesConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(PopularityUpdatesConsumer.class);
    private final PopularityManager manager;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     */
    public PopularityUpdatesConsumer(Channel channel, PopularityManager manager) {
        super(channel);
        this.manager =manager;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
//        log.debug( "Consuming popularity : " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            PopularityUpdatesMessage popularityUpdate = mapper.readValue(msg, PopularityUpdatesMessage.class);

            manager.savePopularityMessage(popularityUpdate);

//            boolean success = handler.registerPlatform(platform);
//            log.debug(success?
//                    "Registration of the platform in RDF is success"
//                    :"Registration of the platform in RDF failed");

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Platform object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Platform object" , e);
        }



        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
