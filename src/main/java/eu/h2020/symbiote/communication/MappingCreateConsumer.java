package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.cci.InfoModelMappingRequest;
import eu.h2020.symbiote.core.cci.InfoModelMappingResponse;
import eu.h2020.symbiote.mappings.MappingManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by Szymon Mueller on 20/09/2018.
 */
public class MappingCreateConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(MappingCreateConsumer.class);
    private MappingManager mappingManager;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     * Managers beans passed as parameters because of lack of possibility to inject it to consumer.
     *
     * @param channel         the channel to which this consumer is attached
     * @param mappingManager    mapping manager
     */
    public MappingCreateConsumer(Channel channel,
                                 MappingManager mappingManager) {
        super(channel);
        this.mappingManager = mappingManager;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Creating mapping: " + msg );

        ObjectMapper mapper = new ObjectMapper();

        InfoModelMappingResponse response = null;
        //Try to parse the message
        try {
            InfoModelMappingRequest infoModelMappingRequest = mapper.readValue(msg, InfoModelMappingRequest.class);
            mappingManager.deleteMapping(infoModelMappingRequest);
        } catch( Exception e ) {
            log.error( "Error occurred when registering info " + e );
        }


        byte[] responseBytes = mapper.writeValueAsBytes(response != null ? response : "[]");

        AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                .Builder()
                .correlationId(properties.getCorrelationId())
                .build();
        this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
        log.debug("-> Mapping created message was sent back");

        this.getChannel().basicAck(envelope.getDeliveryTag(), false);

    }
}