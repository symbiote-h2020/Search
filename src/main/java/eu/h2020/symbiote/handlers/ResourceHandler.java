package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.objectweb.asm.Handle;
import org.springframework.util.Assert;

import java.util.List;

/**
 * Created by Mael on 16/01/2017.
 */
public class ResourceHandler implements IResourceEvents {

    private static final Log log = LogFactory.getLog(ResourceHandler.class);

    private final SearchStorage storage;

    public ResourceHandler( SearchStorage storage ) {
        this.storage = storage;
    }

    @Override
    public boolean registerResource(Resource resource) {
        Assert.notNull(resource);
        log.debug( "Resource handler is handling resource: " + resource.getId());

        //Read to get platform and its information service
        String platformId = resource.getPlatformId();
        String resourceURL = resource.getResourceURL(); //match this with

        String query = "PREFIX core: <https://www.symbiote-h2020.eu/ontology/core#>\n" +
                "PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta.owl#>" +
                "\n" +
                "SELECT ?service WHERE {\n" +
                "\t?service a mim:InterworkingService;\n" +
                "\t\t\tmim:hasURL \"" + resourceURL +"\".\n" +
                "} ";

        List<String> response = this.storage.query(Ontology.getPlatformGraphURI(platformId), query);
        log.debug(" RESPONSE FOR SEARCHING SERVICE URL: " + resourceURL );
        log.debug(response);
        log.debug(" xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx ");
        String registeredServiceURI = null;
        if( response != null && response.size() == 1) {
            registeredServiceURI = response.get(0).substring(response.get(0).indexOf("=")+2);
        } else {
            log.error(response==null?"Response is null":"Response size differs, got size: " + response.size());
        }

        if( registeredServiceURI == null ) {
            log.error("Could not properly find interworking service for url " + resourceURL);
            return false;
        }
        log.debug("Gonna create resource for service URI: " + registeredServiceURI);
        Model resourceModel = HandlerUtils.generateModelFromResource(resource);
        this.storage.registerResource(Ontology.getPlatformGraphURI(platformId),registeredServiceURI,Ontology.getResourceGraphURI(resource.getId()),resourceModel);
        return true;
    }

    @Override
    public boolean updateResource(Resource resource) {
        return false;
    }

    @Override
    public boolean deleteResource(Resource resource) {
        return false;
    }

    @Override
    public boolean deleteResource(String resourceId) {
        return false;
    }
}
