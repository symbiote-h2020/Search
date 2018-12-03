package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.internal.GetSingleMapping;
import eu.h2020.symbiote.core.internal.MappingListResponse;
import eu.h2020.symbiote.mappings.MappingManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by Szymon Mueller on 20/09/2018.
 */
public class MappingFindOneConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(MappingFindOneConsumer.class);
    private MappingManager mappingManager;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     * Managers beans passed as parameters because of lack of possibility to inject it to consumer.
     *
     * @param channel        the channel to which this consumer is attached
     * @param mappingManager mapping manager
     */
    public MappingFindOneConsumer(Channel channel,
                                  MappingManager mappingManager) {
        super(channel);
        this.mappingManager = mappingManager;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug("Find one mapping: " + msg);

        ObjectMapper mapper = new ObjectMapper();

        MappingListResponse response = null;
        //Try to parse the message
        try {

            GetSingleMapping getSingleMapping = mapper.readValue(msg, GetSingleMapping.class);
            response = mappingManager.findSingleMapping(getSingleMapping);
        } catch (Exception e) {
            log.error("Error occurred when find one mapping info " + e);
        }

        try {
            byte[] responseBytes = mapper.writeValueAsBytes(response != null ? response : "[]");

            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();
            this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
            log.debug("-> Find one mapping message was sent back");
        } catch (Exception e) {
            log.error("Message error occurred when sending response to find one mapping: " + e.getMessage(), e);
        } finally {
            this.getChannel().basicAck(envelope.getDeliveryTag(), false);
        }

    }
}