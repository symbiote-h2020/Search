package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.CoreSspResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.AccessPolicy;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.CleanupBlankOrphansRequestGenerator;
import eu.h2020.symbiote.query.DeleteResourceRequestGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import eu.h2020.symbiote.security.accesspolicies.IAccessPolicy;
import eu.h2020.symbiote.security.accesspolicies.common.AccessPolicyFactory;
import eu.h2020.symbiote.security.commons.exceptions.custom.InvalidArgumentsException;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.BIM;
import eu.h2020.symbiote.semantics.ontology.CIM;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.commons.cli.Option;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.update.UpdateRequest;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Mael on 16/01/2017.
 */
public class ResourceHandler implements IResourceEvents {

    private static final String TAG_RESOURCE_URI = "?RESOURCE_URI";

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

            log.debug("Querying for interworking service URI... resUrl: " + resourceURL + " platformId: " + platformId);
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

            Optional<InterworkingServiceInfo> registeredService = findService(resourceURL, platformId);
            if (!registeredService.isPresent()) {
                //If still couldnt find
                log.debug("Couldnt find interworking service URL, returning false");
                return false;
            }

            Map<String, Model> resourcesInRdf = findResourcesInRdf(coreResource.getRdf(), coreResource.getRdfFormat(), registeredService.get());

            if (resourcesInRdf.size() != 1) {
                try (StringReader reader = new StringReader(coreResource.getRdf())) {
                    model.read(reader,null,coreResource.getRdfFormat().toString());
                    resourcesInRdf.put(ModelHelper.getResourceURI(coreResource.getId()),model);
                }
            }

            String resourceUri = resourcesInRdf.keySet().iterator().next();
            Model modelToSave = resourcesInRdf.get(resourceUri);

            this.storage.registerResource(ModelHelper.getPlatformURI(platformId), registeredService.get().getInterworkingServiceIRI(), resourceUri , modelToSave);
            if (coreResource.getPolicySpecifier() != null) {
                try {
                    IAccessPolicy singleTokenAccessPolicy = AccessPolicyFactory.getAccessPolicy(coreResource.getPolicySpecifier());
                    AccessPolicy policy = new AccessPolicy(coreResource.getId(), ModelHelper.getResourceURI(coreResource.getId()), singleTokenAccessPolicy);
                    this.accessPolicyRepo.save(policy);
                } catch (InvalidArgumentsException e) {
                    log.error("[POLICY NOT SAVED] Error when parsing filtering policy: " + e.getMessage(), e);
                }
            }
//            }
        }
        storage.getTripleStore().printDataset();
        return true;
    }

    public Map<String,Model> findResourcesInRdf(String rdf, RDFFormat rdfFormat, InterworkingServiceInfo informationModeIri) {
        log.debug("Searching for resource in rdf");
        long in = System.currentTimeMillis();
        Map<String,Model> result = new HashMap<>();
        try {
            long time1 = System.currentTimeMillis();
            OntModel ontModel = ModelHelper.readModel(rdf, rdfFormat , false, false);
            long timer_after_read_model = System.currentTimeMillis();
//            if( ontModel.getDocumentManager().getProcessImports() ) {
//                log.debug("Turning on manual process imports");
//                ontModel.getDocumentManager().setProcessImports(true);
//            } else {
//                log.debug("Process imports are on");
//            }
            ontModel = ModelHelper.withInf(ontModel);
            long timer_after_with_inf = System.currentTimeMillis();

            //New add pim

//                Model pimModel = this.storage.getTripleStore().getNamedModel(registeredService.get().getInformationModeIri());
//
//                OntModel pim = null;
//                try {
//                    pim = ModelHelper.asOntModel(pimModel, true, true);
//                } catch( Exception e ) {
//                    log.error("Error occurred when asOntModel: " + e.getMessage());
//                }

            //Get information model definition for information model entity
            OntModel loadedDefaultModel = storage.getNamedGraphAsOntModel(TripleStore.DEFAULT_GRAPH,false,false);
            Set<Individual> infoModels = loadedDefaultModel.listIndividuals(MIM.InformationModel).toSet();
            long timer_after_storage_getnamed = System.currentTimeMillis();
            List<Individual> rightInfoModels = infoModels.stream().filter(infoModelIndividual -> infoModelIndividual.getURI().equals(informationModeIri.getInformationModeIri())).collect(Collectors.toList());

            //TODO just for test

            long timer_after_filtering_byuri = System.currentTimeMillis();

            log.debug("Checking graph containing information model, found: " + rightInfoModels.size() + " for InfoModelIRI: " + informationModeIri.getInformationModeIri());
            log.debug("[Timers] readModel " + (timer_after_read_model - time1) + " | withInf " + (timer_after_with_inf - timer_after_read_model )
                    + " | storageGetNamed " + (timer_after_storage_getnamed - timer_after_with_inf ) + " | filteringByUri " + (timer_after_filtering_byuri - timer_after_storage_getnamed));

            String pimGraphUri = BIM.getURI();

            if( rightInfoModels. size() == 1 ) {
                Individual pimIndividual = rightInfoModels.get(0);
                log.debug("Trying to read definition graph uri");
                Resource modelGraph = loadedDefaultModel.getResource(pimIndividual.getURI()).getProperty(MIM.hasDefinition).getResource();
                log.debug("Found graphUri: " + modelGraph.getURI());
                pimGraphUri = modelGraph.getURI();
            }

//            System.out.println("Is " + pimGraphUri + " any of this serviceIRI: " + informationModeIri.getInterworkingServiceIRI() + " | serviceURL: " + informationModeIri.getInterworkingServiceURL()
//                            + " | platformId: " + informationModeIri.getPlatformId() + " | informationModelIRI " + informationModeIri.getInformationModeIri());

            OntModel pim = storage.getNamedGraphAsOntModel(pimGraphUri,true,true);
//            OntModel pim = storage.getNamedGraphAsOntModel("http://iosb.fraunhofer.de/ilt/ontologies/educampus");
//            printModel("pimModel",pim);

            if( pim != null ) {
                ontModel.addSubModel( pim);

                //Set<Individual> individuals = ontModel.listIndividuals(CIM.Resource).toSet();

                Set<Individual> resourcesDefinedInPIM = ModelHelper.withInf(pim).listIndividuals(CIM.Resource).toSet();
                log.debug("After reading pim defined resources");
                Set<Individual> resourceIndividuals = ontModel.listIndividuals(CIM.Resource).toSet();
//                    Set<Individual> resourceIndividuals = ontModel.listIndividuals(CIM.Service).toSet();
                log.debug("After reading all resources");
                resourceIndividuals.removeAll(resourcesDefinedInPIM);
                ontModel.removeSubModel(pim);
                if (resourceIndividuals.size() == 1) {
                    Individual resourceIndv = resourceIndividuals.iterator().next();

                    String resourceUri = resourceIndv.getURI();
                    log.debug("Found following URI of the resource " + resourceUri);

                    //TODO

                    GET_RESOURCE_CLOSURE.setIri(TAG_RESOURCE_URI, resourceUri);
                    try (QueryExecution qexec = QueryExecutionFactory.create(GET_RESOURCE_CLOSURE.asQuery(), ontModel.getRawModel())) {
                        Model model = qexec.execConstruct();
                        result.put(resourceUri,model);
                    }

                } else {
                    //What to do here?
                    log.error("Wrong number of resources in registration! size " + resourceIndividuals.size());
                }
            } else {
                log.error("Could not load submodel: " + informationModeIri);
            }
        } catch (IOException e) {
            log.error(e);
        }
        log.debug("Returning resources in rdf: " + result.size() + " total check time: " + ( System.currentTimeMillis() - in ) + " ms");
        return result;

    }

    private void printModel(String modelName, OntModel resourceModel) {
        log.debug("Printing model " + modelName);
        log.debug("========================================================");
        StmtIterator stmtIterator = resourceModel.listStatements();
        while( stmtIterator.hasNext() ) {
            Statement next = stmtIterator.next();
            log.debug( " " + next.getSubject().toString() + "  |  " + next.getPredicate().toString() + "  |  " + next.getObject().toString() );
        }
        log.debug("========================================================");
    }

    public void addSdevResourceServiceLink(CoreSspResourceRegisteredOrModifiedEventPayload resources) {
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

    private String getSearchInterworkingServiceSPARQL(String resourceURL, String platformId) {
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

    private Optional<InterworkingServiceInfo> findService(String resourceURL, String platformId) {
        Optional<InterworkingServiceInfo> result;
        if (resourceURL != null && !resourceURL.isEmpty()) {
            List<InterworkingServiceInfo> ii = this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL);
            if (ii.size() == 0) {
                if (resourceURL.endsWith("/")) {
                    ii = this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL.substring(0, resourceURL.length() - 1));
                } else {
                    ii = this.interworkingServiceInfoRepo.findByInterworkingServiceURL(resourceURL + "/");
                }
            }

            log.debug("Found " + ii.size() + " interworking services for url " + resourceURL + " for platform " + platformId);
            if (ii.size() > 0) {
                log.debug("The platform id of the first element of interworking service list: " + ii.get(0).getPlatformId());
                log.debug("The iri of the first element of interworking service list: " + ii.get(0).getInterworkingServiceIRI());
                result = Optional.of(ii.get(0));
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
//
//    private String findServiceURISPARQL(String resourceURL, String platformId) {
//        String registeredServiceURI = null;
//        String query = getSearchInterworkingServiceSPARQL(resourceURL, platformId);
//
//        List<String> response = this.storage.query(ModelHelper.getPlatformURI(platformId), query);
//
//        if (response != null && response.size() == 1) {
//            log.debug("response: " + response.get(0));
//            registeredServiceURI = response.get(0).substring(response.get(0).indexOf("=") + 2);
//            log.debug("Found resource URL: " + registeredServiceURI);
//        } else {
//            log.error(response == null ? "Response is null" : "Response size differs, got size: " + response.size());
//        }
//
//        if (registeredServiceURI == null) {
//            log.error("Could not properly find interworking service for url " + resourceURL);
//        }
//        return registeredServiceURI;
//    }

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

        //Mod to check if resource exists
        this.storage.getTripleStore().executeUpdate(updateRequest);

        return true;
    }

    public boolean deleteResources( List<String> resourceIds ) {
        boolean result = this.storage.getTripleStore().executeDeleteResources(resourceIds);
        log.debug("Delete resources ended with result: " + result);
        return result;
    }

    @Override
    public void cleanupBlankOrphans() {
        log.debug("Deleting orphans ...");
        CleanupBlankOrphansRequestGenerator generator = new CleanupBlankOrphansRequestGenerator();
        UpdateRequest orphanClears = generator.generateRequest();

        this.storage.getTripleStore().executeUpdate(orphanClears);
    }


    private static final ParameterizedSparqlString GET_RESOURCE_CLOSURE = new ParameterizedSparqlString(
            "CONSTRUCT {\n"
                    + "	?s ?p ?o.\n"
                    + "}\n"
                    + "{\n"
                    + "	SELECT DISTINCT ?s ?p ?o\n"
                    + "	{\n"
                    + "		{\n"
                    + "			SELECT *\n"
                    + "			{\n"
                    + "				" + TAG_RESOURCE_URI + " ?p ?o.\n"
                    + "				BIND( " + TAG_RESOURCE_URI + " as ?s)\n"
                    + "			}\n"
                    + "		}\n"
                    + "		UNION\n"
                    + "		{\n"
                    + "			SELECT *\n"
                    + "			WHERE {\n"
                    + "				" + TAG_RESOURCE_URI + " (a|!a)+ ?s . \n"
                    + "				?s ?p ?o.\n"
                    + "			}\n"
                    + "		}\n"
                    + "	}\n"
                    + "}");
}
