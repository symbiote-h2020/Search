package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
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
public class SparqlSearchRequestedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SparqlSearchRequestedConsumer.class);

    private final SearchHandler handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public SparqlSearchRequestedConsumer(Channel channel, SearchHandler handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        log.debug( "Consume sparql search requested message: " + msg );

        //Try to parse the message
        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreSparqlQueryRequest searchRequest = mapper.readValue(msg, CoreSparqlQueryRequest.class);

            String response = null;
             try {
                 response = handler.sparqlSearch(searchRequest);
             } catch( Exception e ) {
                 log.error("Error occurred when performing sparql search: " + e.getMessage(), e);
             }
            //Send the response back to the client
//            String responseMessage = "msg";
//            if( response != null && response.getResourceList() != null ) {
//                responseMessage = "size is " + response.getResourceList().size();
//            } else {
//                responseMessage = "Response is null or empty";
//            }

            log.debug( "Got Sparql response : " + response );

            byte[] responseBytes = mapper.writeValueAsBytes(response!=null?response:"[]");

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
        } catch( Exception e ) {

        }
    }
}
