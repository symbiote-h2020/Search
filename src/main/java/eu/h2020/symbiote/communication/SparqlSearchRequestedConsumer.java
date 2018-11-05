package eu.h2020.symbiote.communication;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import eu.h2020.symbiote.core.ci.SparqlQueryResponse;
import eu.h2020.symbiote.core.internal.CoreSparqlQueryRequest;
import eu.h2020.symbiote.handlers.ISearchEvents;
import eu.h2020.symbiote.handlers.SearchHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;

import java.io.IOException;
import java.util.UUID;

/**
 * Consumer of the search requested event. Translates the message as list of query parameters, translates them into
 * SPARQL and queries the repository.
 *
 * Created by Mael on 17/01/2017.
 */
public class SparqlSearchRequestedConsumer extends DefaultConsumer {

    private static int MAX_SPARQL_SIZE = 30000000;

    private static Log log = LogFactory.getLog(SparqlSearchRequestedConsumer.class);

    private final ISearchEvents handler;

    /**
     * Constructs a new instance and records its association to the passed-in channel.
     *
     * @param channel the channel to which this consumer is attached
     * @param handler handler to be used by the consumer.
     *
     */
    public SparqlSearchRequestedConsumer(Channel channel, ISearchEvents handler) {
        super(channel);
        this.handler = handler;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        String msg = new String(body);
        String reqId = UUID.randomUUID().toString();
        log.debug( "["+reqId+"] Consume sparql search requested message: " + msg );

        SearchCommunicationHandler comm = new SearchCommunicationHandler(reqId, this.getChannel(), consumerTag,envelope,properties);

        //Try to parse the message
        try {
            ObjectMapper mapper = new ObjectMapper();
            CoreSparqlQueryRequest searchRequest = mapper.readValue(msg, CoreSparqlQueryRequest.class);


            SparqlQueryResponse response = null;
             try {
                 response = handler.sparqlSearch(comm, searchRequest);
             } catch( Exception e ) {
                 log.error("Error occurred when performing sparql search: " + e.getMessage(), e);
                 response = new SparqlQueryResponse(HttpStatus.SC_INTERNAL_SERVER_ERROR,"Error occurred during sparql search: " + e.getMessage(),"");
             }

//            //TODO for brievety
////            log.debug( "Got Sparql response : " + StringUtils.substring(response.getBody(), 0,400 ));
//            log.debug( "Got Sparql response : " + response.getBody());
//
//            if( response.getBody() != null && response.getBody().length() > MAX_SPARQL_SIZE ) {
//                log.debug( "Size is too big: " + response.getBody().length() );
//                response.setMessage("Size of the response is too big for communication");
//                response.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
//                response.setBody("");
//            } else {
//                log.debug(response.getBody() != null?"Size of the response ok: " + response.getBody().length():"Got null response");
//            }
//
//            byte[] responseBytes = mapper.writeValueAsBytes(response!=null?response:"[]");
//
//            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
//                    .Builder()
//                    .correlationId(properties.getCorrelationId())
//                    .build();
//            this.getChannel().basicPublish("", properties.getReplyTo(), replyProps, responseBytes);
//            log.debug("-> Message sent back");
//
//            this.getChannel().basicAck(envelope.getDeliveryTag(), false);

        } catch( JsonParseException | JsonMappingException e ) {
            log.error("Error occurred when parsing Resource object JSON: " + msg, e);
        } catch( IOException e ) {
            log.error("I/O Exception occurred when parsing Resource object" , e);
        } catch( Exception e ) {
            log.error("Generic exception occurred when executing sparql search", e);
        }
    }
}
