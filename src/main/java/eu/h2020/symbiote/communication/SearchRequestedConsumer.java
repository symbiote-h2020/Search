package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.ci.QueryResponse;
import eu.h2020.symbiote.core.internal.CoreQueryRequest;
import eu.h2020.symbiote.handlers.SearchHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Consumer of the search requested event. Translates the message as list of query parameters, translates them into
 * SPARQL and queries the repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class SearchRequestedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SearchRequestedConsumer.class);

    private final SearchHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public SearchRequestedConsumer(Channel channel, SearchHandler handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume search requested message: " + msg );

        //Try to parse the message
        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreQueryRequest searchRequest = mapper.readValue(msg, CoreQueryRequest.class);

            

            QueryResponse response = handler.search(searchRequest);
            //Send the response back to the client
            String responseMessage = "msg";
            if( response != null && response.getResources() != null ) {
                responseMessage = "size is " + response.getResources().size();
            } else {
                responseMessage = "Response is null or empty";
            }


            log.debug( "Calculated response, sending back to the sender: " + responseMessage);

            byte[] responseBytes = mapper.writeValueAsBytes(response!=null?response.getResources():"[]");

            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .build();
            this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
            System.out.println("-> Message sent back");

            this.getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        }
    }
}
