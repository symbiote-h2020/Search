package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.CoreSspResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.filtering.AccessPolicy;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.query.CleanupBlankOrphansRequestGenerator;
import eu.h2020.symbiote.query.DeleteResourceRequestGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.AccessPolicyFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.semantics.ModelHelper;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.update.UpdateRequest;
import org.springframework.util.Assert;

import java.io.StringReader;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by Mael on 16/01/2017.
 */
public class ResourceHandler implements IResourceEvents {

    private static final Log log = LogFactory.getLog(ResourceHandler.class);

    private final SearchStorage storage;
    private final AccessPolicyRepo accessPolicyRepo;
    private final InterworkingServiceInfoRepo interworkingServiceInfoRepo;

    public ResourceHandler(SearchStorage storage, AccessPolicyRepo accessPolicyRepo, InterworkingServiceInfoRepo interworkingServiceInfoRepo) {
        this.storage = storage;
        this.accessPolicyRepo = accessPolicyRepo;
        this.interworkingServiceInfoRepo = interworkingServiceInfoRepo;
    }

    @Override
    public boolean registerResource(CoreResourceRegisteredOrModifiedEventPayload resources) {
        Assert.notNull(resources, "Could not register null resources");
        log.debug("Resource handler is handling resources for platform: " + resources.getPlatformId());

        //Read to get platform and its information service
        String platformId = resources.getPlatformId();

        for (CoreResource coreResource : resources.getResources()) {
            Model model = ModelFactory.createDefaultModel();
            log.debug(" ----- ------ -----");
            log.debug(coreResource.getRdf());
            log.debug(" ----- ------ -----");

            String resourceURL = coreResource.getInterworkingServiceURL(); //match this with

            log.debug( "Querying for interworking service URI... resUrl: " + resourceURL + " platformId: " + platformId);
//            String registeredServiceURI = findServiceURI(resourceURL,platformId);
//            if( registeredServiceURI == null ) {
//                //Try with slash in the end - most common mistake from platforms
//                if( !resourceURL.endsWith("/") ) {
//                    log.debug("Couldnt find interworking service URI, trying with URL ending with a slash");
//                    registeredServiceURI = findServiceURI(resourceURL + "/", platformId);
//                } else if (resourceURL.endsWith("/") ) {
//                    log.debug("Couldnt find interworking service URI, trying with URL without trailing slash");
//                    registeredServiceURI = findServiceURI(resourceURL.substring(0,resourceURL.length()-1), platformId);
//                }
//            }

            Optional<String> registeredServiceURI = findServiceURI(resourceURL, platformId);
            if( !registeredServiceURI.isPresent()) {
                //If still couldnt find
                log.debug("Couldnt find interworking service URL, returning false");
                return false;
            }


            try (StringReader reader = new StringReader(coreResource.getRdf())) {
                model.read(reader, null, coreResource.getRdfFormat().toString());
                this.storage.registerResource(ModelHelper.getPlatformURI(platformId), registeredServiceURI.get(), ModelHelper.getResourceURI(coreResource.getId()), model);
                if( coreResource.getPolicySpecifier() != null ) {
                    try {
                        IAccessPolicy singleTokenAccessPolicy = AccessPolicyFactory.getAccessPolicy(coreResource.getPolicySpecifier());
                        AccessPolicy policy = new AccessPolicy(coreResource.getId(), ModelHelper.getResourceURI(coreResource.getId()), singleTokenAccessPolicy);
                        this.accessPolicyRepo.save(policy);
                    } catch (InvalidArgumentsException e) {
                        log.error("[POLICY NOT SAVED] Error when parsing filtering policy: " + e.getMessage(), e);
                    }
                }
            }
        }
        storage.getTripleStore().printDataset();
        return true;
    }

    public void addSdevResourceServiceLink( CoreSspResourceRegisteredOrModifiedEventPayload resources ) {
        log.debug("Adding sdev resources service link");

        String sdevURI = ModelHelper.getSdevURI(resources.getSdevId());

        String sdevServiceUri = HandlerUtils.generateInterworkingServiceUriForSdev(sdevURI);

        for (CoreResource coreResource : resources.getResources()) {
            storage.registerSdevResourceLinkToSdevService(sdevServiceUri, ModelHelper.getResourceURI(coreResource.getId()));
        }
    }

    //TODO write performance improvement
    // II -> persistable class of URL and IRI of the II
    // load Map<String,List<II>> -> keys are platformIds
    // update map on platform crud

    private String getSearchInterworkingServiceSPARQL( String resourceURL, String platformId ) {
        return "PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#>\n" +
                "PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#>" +
                "\n" +
                "SELECT ?service WHERE {\n" +
                "\t?service a mim:InterworkingService;\n" +
                "\t\t\tmim:url \"" + resourceURL + "\".\n" +
                "\t\t?platform cim:id \"" + platformId + "\";\n" +
                "\t\t\tmim:hasService ?service.\n" +
                "} ";
    }

    private Optional<String> findServiceURI(String resourceURL, String platformId ) {
        Optional<String> result;
        if( resourceURL != null && !resourceURL.isEmpty() ) {
            List<InterworkingServiceInfo> ii = this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL);
            if( ii.size() == 0 ) {
                if( resourceURL.endsWith("/") ) {
                    ii = this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL.substring(0, resourceURL.length() - 1));
                } else {
                    ii = this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL+"/");
                }
            }

            log.debug("Found " + ii.size() + " interworking services for url " + resourceURL + " for platform " + platformId);
            if( ii.size()>0 ) {
                log.debug("The platform id of the first element of interworking service list: " + ii.get(0).getPlatformId());
                result = Optional.of(ii.get(0).getInterworkingServiceIRI());
            } else {
                result = Optional.empty();
            }

//            List<InterworkingServiceInfo> filterByPlatformId = ii.stream().filter(isi -> StringUtils.equals(platformId, isi.getPlatformId())).collect(Collectors.toList());
//            log.debug("After filtering by platformId " + filterByPlatformId.size() );
//            return filterByPlatformId.size()>0?Optional.of(filterByPlatformId.get(0).getInterworkingServiceIRI()): Optional.empty();
            return result;

        }
        return Optional.empty();
//            orElse( resourceURL.endsWith("/")?
//                    this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL.substring(0,resourceURL.length()-1)):
//                    this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL+"/"));
        }

//        String registeredServiceURI = findServiceURI(resourceURL,platformId);
//        if( registeredServiceURI == null ) {
//            //Try with slash in the end - most common mistake from platforms
//            if( !resourceURL.endsWith("/") ) {
//                log.debug("Couldnt find interworking service URI, trying with URL ending with a slash");
//                registeredServiceURI = findServiceURI(resourceURL + "/", platformId);
//            } else if (resourceURL.endsWith("/") ) {
//                log.debug("Couldnt find interworking service URI, trying with URL without trailing slash");
//                registeredServiceURI = findServiceURI(resourceURL.substring(0,resourceURL.length()-1), platformId);
//            }
//        }
//        if( registeredServiceURI == null ) {
//            //If still couldnt find
//            log.debug("Couldnt find interworking service URL, returning false");
//            return false;
//        }
//
//        return foundUri
//    }

    private String findServiceURISPARQL( String resourceURL, String platformId ) {
        String registeredServiceURI = null;
        String query = getSearchInterworkingServiceSPARQL(resourceURL, platformId);

        List<String> response = this.storage.query(ModelHelper.getPlatformURI(platformId), query);

        if (response != null && response.size() == 1) {
            log.debug("response: " + response.get(0));
            registeredServiceURI = response.get(0).substring(response.get(0).indexOf("=") + 2);
            log.debug("Found resource URL: " + registeredServiceURI);
        } else {
            log.error(response == null ? "Response is null" : "Response size differs, got size: " + response.size());
        }

        if (registeredServiceURI == null) {
            log.error("Could not properly find interworking service for url " + resourceURL);
        }
        return registeredServiceURI;
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

        this.storage.getTripleStore().printDataset();
        this.accessPolicyRepo.delete(resourceId);
        return true;
    }

    @Override
    public void cleanupBlankOrphans() {
        log.debug("Deleting orphans ...");
        CleanupBlankOrphansRequestGenerator generator = new CleanupBlankOrphansRequestGenerator();
        UpdateRequest orphanClears = generator.generateRequest();

        this.storage.getTripleStore().executeUpdate(orphanClears);
    }

}
