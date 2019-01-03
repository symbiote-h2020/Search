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
import eu.h2020.symbiote.handlers.ISearchEvents;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * Consumer of the search requested event. Translates the message as list of query parameters, translates them into
 * SPARQL and queries the repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class SingleThreadSearchRequestedConsumer extends DefaultConsumer {

    private static Log log = LogFactory.getLog(SingleThreadSearchRequestedConsumer.class);

    private final ISearchEvents handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public SingleThreadSearchRequestedConsumer(Channel channel, ISearchEvents handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        String reqId = UUID.randomUUID().toString();
        log.debug( "["+reqId+"] Consume search requested message: " + msg );
        long in = System.currentTimeMillis();


        //Try to parse the message
        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreQueryRequest searchRequest = mapper.readValue(msg, CoreQueryRequest.class);


            String responseMessage = "Something wrong happened when executing search";
            QueryResponse response = null;

            SearchCommunicationHandler comm = new SearchCommunicationHandler(reqId, this.getChannel(), consumerTag,envelope,properties);
            try {
                response = handler.search(comm,searchRequest);
                //Send the response back to the client
                if (response != null && response.getBody() != null) {
                    responseMessage = "size is " + response.getBody().size();
                } else {
                    responseMessage = "Response is null or empty";
                }
//            }catch( ExecutionException e ) {
//                log.error("Execution error occurred when performing search operation: " + e.getMessage(), e);
//                response = new QueryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Execution error occurred during search: " + e.getMessage(), new ArrayList<>());
//            }catch( TimeoutException e ) {
//                log.error("Timeout occurred when performing search operation: " + e.getMessage(), e);
//                response = new QueryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Timeout occurred when performing search: " + e.getMessage(), new ArrayList<>());
            }catch( Exception e ) {
                log.error("Error occurred when performing search operation: " + e.getMessage(), e);
                response = new QueryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error occurred during search: " + e.getMessage(), new ArrayList<>());
            }


            log.debug( "Calculated response, sending back to the sender: " + responseMessage);

            byte[] responseBytes = mapper.writeValueAsBytes(response!=null?response:"[]");

            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .contentType("application/json")
                    .build();
            this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
            log.debug("["+reqId+"-> Message sent back in total time " + (System.currentTimeMillis() - in) + " ms");

            this.getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        }
    }
}
