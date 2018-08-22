package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.ci.QueryResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

import static org.apache.jena.vocabulary.VOID.properties;

/**
 * Created by Szymon Mueller on 10/07/2018.
 */
public class SearchCommunicationHandler {

    private final String reqId;
    private final Channel channel;
    private final String consumerTag;
    private final Envelope envelope;
    private final AMQP.BasicProperties properties;
    private Log log = LogFactory.getLog(SearchCommunicationHandler.class);

    public SearchCommunicationHandler(String reqId, Channel channel, String consumerTag, Envelope envelope, AMQP.BasicProperties properties) {
        this.reqId = reqId;
        this.channel = channel;
        this.consumerTag = consumerTag;
        this.envelope = envelope;
        this.properties = properties;
    }

    public String getReqId() {
        return reqId;
    }

    public void sendResponse(QueryResponse response ) {
        log.debug( "Calculated response, sending back to the sender, status: " + response.getStatus());
        ObjectMapper mapper = new ObjectMapper();
        long in = System.currentTimeMillis();
        try {
            byte[] responseBytes = mapper.writeValueAsBytes(response!=null?response:"[]");


            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(properties.getCorrelationId())
                    .contentType("application/json")
                    .build();
            this.channel.basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
            log.debug("["+reqId+"-> Message sent back in total time " + (System.currentTimeMillis() - in) + " ms");

            this.channel.basicAck(envelope.getDeliveryTag(), false);
        } catch (JsonProcessingException e) {
            log.error("Error occurred when parsing Resource object JSON: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        }

    }

}
