package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.mim.InformationModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * RabbitMQ Consumer implementation used for model deleted
 * <p>
 * Created by Szymon Mueller
 */
public class ModelDeletedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(ModelDeletedConsumer.class);


    private final PlatformHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     * Managers beans passed as parameters because of lack of possibility to inject it to consumer.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler
     */
    public ModelDeletedConsumer(Channel channel,
                                PlatformHandler handler) {
        super(channel);
        this.handler = handler;
    }

    /**
     * Called when a <code><b>basic.deliver</b></code> is received for this consumer.
     *
     * @param consumerTag the <i>consumer tag</i> associated with the consumer
     * @param envelope    packaging data for the message
     * @param properties  content header data for the message
     * @param body        the message body (opaque, client-specific byte array)
     * @throws IOException if the consumer encounters an I/O error while processing the message
     * @see Envelope
     */
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body)
            throws IOException {
        String msg = new String(body);
        log.debug("Consume delete PIM meta model message: " + msg);

        //Try to parse the message
        try {
            ObjectMapper mapper = new ObjectMapper();
            InformationModel model = mapper.readValue(msg, InformationModel.class);

            this.handler.deleteInformationModel(model);

            log.debug("Model deleted: " + model.getUri());

            getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch (JsonParseException | JsonMappingException e) {
            log.error("Error occurred when registering new PIM meta model: " + msg, e);
        } catch (IOException e) {
            log.error("I/O Exception occurred when parsing PIM meta model object", e);
        } catch (Exception e) {
            log.error("Generic error ocurred when handling delivery", e);
        }
    }
}
