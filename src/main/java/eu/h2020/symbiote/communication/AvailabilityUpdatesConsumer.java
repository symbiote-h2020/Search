package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.cloud.monitoring.model.CloudMonitoringPlatformRequest;
import eu.h2020.symbiote.core.internal.popularity.PopularityUpdatesMessage;
import eu.h2020.symbiote.ranking.AvailabilityManager;
import eu.h2020.symbiote.ranking.PopularityManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Consumer of the popularity updates.
 *
 * Created by Szymon Mueller on 24/08/2017.
 */
public class AvailabilityUpdatesConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(AvailabilityUpdatesConsumer.class);
    private final AvailabilityManager manager;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     */
    public AvailabilityUpdatesConsumer(Channel channel, AvailabilityManager manager) {
        super(channel);
        this.manager =manager;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consuming popularity : " + msg );

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            CloudMonitoringPlatformRequest availabilityUpdate = mapper.readValue(msg, CloudMonitoringPlatformRequest.class);

            manager.saveAvailabilityMessage(availabilityUpdate);

//            boolean success = handler.registerPlatform(platform);
//            log.debug(success?
//                    "Registration of the platform in RDF is success"
//                    :"Registration of the platform in RDF failed");

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Monitoring object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Monitoring object" , e);
        }



        getChannel().basicAck(envelope.getDeliveryTag(),false);
    }

}
