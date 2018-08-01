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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Consumer of the ssp sdev created event. Handler creates RDF representation of provided ssp and adds it into
 * repository.
 * <p>
 * Created by Szymon Mueller on 25/05/2018.
 */
public class SdevCreatedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SdevCreatedConsumer.class);

    private final PlatformHandler handler;

    private final ThreadPoolExecutor writerExecutorService;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached.
     * @param handler handler to be used by the consumer.
     */
    public SdevCreatedConsumer(Channel channel, PlatformHandler handler, ThreadPoolExecutor writerExecutorService) {
        super(channel);
        this.handler = handler;
        this.writerExecutorService = writerExecutorService;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug("Consume sdev created message: " + msg);

        //Try to parse the message

        try {
            ObjectMapper mapper = new ObjectMapper();
            SspRegInfo sdev = mapper.readValue(msg, SspRegInfo.class);

            Callable<Boolean> callable = () -> {
                boolean success = handler.registerSdev(sdev);
                log.debug(success ?
                        "Registration of the sdev in RDF is success"
                        : "Registration of the sdev in RDF failed");
                return Boolean.TRUE;
            };

            writerExecutorService.submit(callable);

//            handler.printStorage();

        } catch (JsonParseException | JsonMappingException e) {
            log.error("Error occurred when parsing Sdev object JSON: " + msg, e);
        } catch (IOException e) {
            log.error("I/O Exception occurred when parsing Sdev object", e);
        }


        getChannel().basicAck(envelope.getDeliveryTag(), false);
    }

}
