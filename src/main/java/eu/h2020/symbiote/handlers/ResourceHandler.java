package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.model.internal.CoreResource;
import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.query.DeleteResourceRequestGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateRequest;
import org.springframework.util.Assert;

import java.io.StringReader;
import java.util.List;

/**
 * Created by Mael on 16/01/2017.
 */
public class ResourceHandler implements IResourceEvents {

    private static final Log log = LogFactory.getLog(ResourceHandler.class);

    private final SearchStorage storage;

    public ResourceHandler(SearchStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean registerResource(CoreResourceRegisteredOrModifiedEventPayload resources) {
        Assert.notNull(resources);
        log.debug("Resource handler is handling resources for platform: " + resources.getPlatformId());

        //Read to get platform and its information service
        String platformId = resources.getPlatformId();
//        String resourceURL = resource.getResourceURL(); //match this with
//
//        String query = "PREFIX core: <https://www.symbiote-h2020.eu/ontology/core#>\n" +
//                "PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta.owl#>" +
//                "\n" +
//                "SELECT ?service WHERE {\n" +
//                "\t?service a mim:InterworkingService;\n" +
//                "\t\t\tmim:hasURL \"" + resourceURL + "\".\n" +
//                "} ";
//
//        List<String> response = this.storage.query(Ontology.getPlatformGraphURI(platformId), query);
//        String registeredServiceURI = null;
//        if (response != null && response.size() == 1) {
//            registeredServiceURI = response.get(0).substring(response.get(0).indexOf("=") + 2);
//        } else {
//            log.error(response == null ? "Response is null" : "Response size differs, got size: " + response.size());
//        }
//
//        if (registeredServiceURI == null) {
//            log.error("Could not properly find interworking service for url " + resourceURL);
//            return false;
//        }
//        log.debug("Gonna create resource for service URI: " + registeredServiceURI);
////        Model resourceModel = HandlerUtils.generateModelFromResource(resource);
//        this.storage.registerResource(Ontology.getPlatformGraphURI(platformId), registeredServiceURI, Ontology.getResourceGraphURI(resource.getId()), resourceModel);

        for (CoreResource coreResource : resources.getResources()) {
            Model model = ModelFactory.createDefaultModel();
            System.out.println(" ----- ------ -----");
            System.out.println(coreResource.getRdf());
            System.out.println(" ----- ------ -----");

            String matchUrl = coreResource.getInterworkingServiceURL();
            if( !matchUrl.endsWith("/") ) {
                matchUrl += "/"; //match this with
            }

            log.debug( "Querying for interworking service URI... ");
            String query = "PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#>\n" +
                    "PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#>" +
                    "\n" +
                    "SELECT ?service WHERE {\n" +
                    "\t?service a mim:InterworkingService;\n" +
                    "\t\t\tmim:hasURL \"" + matchUrl + "\".\n" +
                    "\t\t?platform cim:id \"" + platformId + "\";\n" +
                    "\t\t\tmim:hasService ?service.\n" +
                    "} ";

            List<String> response = this.storage.query(Ontology.getPlatformGraphURI(platformId), query);
            String registeredServiceURI = null;
            if (response != null && response.size() == 1) {
                System.out.println("response: " + response.get(0));
                registeredServiceURI = response.get(0).substring(response.get(0).indexOf("=") + 2);
                log.debug("Found resource URL: " + registeredServiceURI);
            } else {
                log.error(response == null ? "Response is null" : "Response size differs, got size: " + response.size());
            }

            if (registeredServiceURI == null) {
                log.error("Could not properly find interworking service for url " + matchUrl);
                return false;
            }


            try (StringReader reader = new StringReader(coreResource.getRdf())) {
                model.read(reader, null, coreResource.getRdfFormat().toString());
                this.storage.registerResource(Ontology.getPlatformGraphURI(platformId), registeredServiceURI, Ontology.getResourceGraphURI(coreResource.getId()), model);
            }
        }
        storage.getTripleStore().printDataset();
        return true;
    }

    @Override
    public boolean updateResource(CoreResourceRegisteredOrModifiedEventPayload resources) {
        log.debug(">>>> Updating description of the resources for platform " + resources.getPlatformId());
        for (CoreResource coreResource : resources.getResources()) {
            boolean deleteSuccess = deleteResource(coreResource.getId());
            if (deleteSuccess) {
                log.debug("Delete of resource " + coreResource.getId() + " successful (as part of update process)");
            } else {
                log.error("Deleting of the resource failed during update execution");
                return false;
            }
        }
        log.debug(">>>> Adding updated resource descriptions");
        boolean registerSuccess = registerResource(resources);
        if (!registerSuccess) {
            log.error("Registering of the resources failed during update execution");
            return false;
        }
        storage.getTripleStore().printDataset();
        return true;
    }

    @Override
    public boolean deleteResource(String resourceId) {
        log.debug("Deleting resource " + resourceId);
        UpdateRequest updateRequest = new DeleteResourceRequestGenerator(resourceId).generateRequest();
        this.storage.getTripleStore().executeUpdate(updateRequest);
        storage.getTripleStore().printDataset();
        return true;
    }
}
