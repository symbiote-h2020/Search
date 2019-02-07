package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.cloud.model.ssp.SspRegInfo;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.model.mim.SmartSpace;
import eu.h2020.symbiote.query.DeletePlatformRequestGenerator;
import eu.h2020.symbiote.query.DeleteSdevRequestGenerator;
import eu.h2020.symbiote.query.DeleteSspRequestGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import eu.h2020.symbiote.semantics.ModelHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.*;
import org.apache.jena.update.UpdateRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the handler for the platform related events.
 * <p>
 * Created by Mael on 11/01/2017.
 */
public class PlatformHandler implements IPlatformEvents, ISspEvents, IModelEvents {

    private static final Log log = LogFactory.getLog(PlatformHandler.class);

    private final SearchStorage storage;

    private final InterworkingServiceInfoRepo interworkingServiceInfoRepo;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param storage Storage on which the events should be executed.
     */
    public PlatformHandler(SearchStorage storage, InterworkingServiceInfoRepo interworkingServiceInfoRepo) {
        this.storage = storage;
        this.interworkingServiceInfoRepo = interworkingServiceInfoRepo;
    }

    @Override
    public boolean registerPlatform(Platform platform) {

        Model platformModel = HandlerUtils.generateModelFromPlatform(platform);
        log.debug("Handler is registering following platform model: ");

        // list the statements in the Model
        StmtIterator iter = platformModel.listStatements();

// print out the predicate, subject and object of each statement
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();  // get next statement
            Resource subject = stmt.getSubject();     // get the subject
            Property predicate = stmt.getPredicate();   // get the predicate
            RDFNode object = stmt.getObject();      // get the object

            log.debug(subject.toString());
            log.debug(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                log.debug(object.toString());
            } else {
                // object is a literal
                log.debug(" \"" + object.toString() + "\"");
            }

            log.debug(" .");
        }

        log.debug("End of model");
        storage.registerPlatform(platform.getId(), platformModel);
        storage.getTripleStore().printDataset();

        log.debug("Saving interworking services for platform " + platform.getId() + " | services: " + (platform.getInterworkingServices()==null?
        "services are null":"size is " +platform.getInterworkingServices().size()));
        //Save interworking services in the repo when registering new platform
        saveInterworkingServicesInfoForPlatform(platform.getId(),
                ModelHelper.getPlatformURI(platform.getId()), platform.getInterworkingServices());

        return true;
    }

    @Override
    public boolean updatePlatform(Platform platform) {
        log.debug("Updating platform " + platform.getId());

        boolean success = deletePlatform(platform.getId());
        if (success) {
            log.debug("Delete step of update performed successfully ");
            registerPlatform(platform);
        } else {
            log.error("Delete step of update failed ");
        }
//        storage.getTripleStore().printDataset();
        return true;
    }

    @Override
    public boolean deletePlatform(String platformId) {
        log.debug("Deleting platform " + platformId);
        UpdateRequest updateRequest = new DeletePlatformRequestGenerator(platformId).generateRequest();
        this.storage.getTripleStore().executeUpdate(updateRequest);
        interworkingServiceInfoRepo.deleteByPlatformId(platformId);
        storage.getTripleStore().printDataset();
        return true;
    }


    @Override
    public boolean registerSsp(SmartSpace ssp) {

        Model sspModel = HandlerUtils.generateModelFromSsp(ssp);

        storage.registerSsp(ssp.getId(), sspModel);
//        storage.getTripleStore().printDataset();

        saveInterworkingServicesInfoForPlatform(ssp.getId(), ModelHelper.getSspURI(ssp.getId()), ssp.getInterworkingServices());

        return true;

    }

    @Override
    public boolean updateSsp(SmartSpace ssp) {
        log.debug("Updating ssp " + ssp.getId());

        boolean success = deleteSsp(ssp.getId());
        if (success) {
            log.debug("Delete step of update ssp performed successfully ");
            registerSsp(ssp);
        } else {
            log.error("Delete step of update ssp failed ");
        }

        return true;
    }

    @Override
    public boolean deleteSsp(String sspId) {
        log.debug("Deleting ssp " + sspId);
        UpdateRequest updateRequest = new DeleteSspRequestGenerator(sspId).generateRequest();
        this.storage.getTripleStore().executeUpdate(updateRequest);
        storage.getTripleStore().printDataset();

        interworkingServiceInfoRepo.deleteByPlatformId(sspId);

        return true;
    }

    @Override
    public boolean registerSdev(SspRegInfo sdev) {

        Model sdevModel = HandlerUtils.generateModelFromSdev(sdev);

        storage.registerSdev(sdev.getSymId(), sdevModel);
//        storage.getTripleStore().printDataset();

        return true;
    }

    @Override
    public boolean updateSdev(SspRegInfo sdev) {
        log.debug("Updating sdev " + sdev.getSymId());

        boolean success = deleteSdev(sdev.getSymId());
        if (success) {
            log.debug("Delete of update sdev performed successfully ");
            registerSdev(sdev);
        } else {
            log.error("Delete step of update sdev failed ");
        }

        return true;
    }

    @Override
    public boolean deleteSdev(String sdevId) {
        log.debug("Deleting sdev " + sdevId);
        UpdateRequest updateRequest = new DeleteSdevRequestGenerator(sdevId).generateRequest();
        this.storage.getTripleStore().executeUpdate(updateRequest);
        storage.getTripleStore().printDataset();
        return true;
    }

    public void saveInterworkingServicesInfoForPlatform(String systemId, String systemIri, List<InterworkingService> serviceList) {
        if( serviceList != null ) {
            serviceList.stream()
                    .map(is -> new InterworkingServiceInfo(HandlerUtils
                            .generateInterworkingServiceUri(systemIri, is.getUrl()),
                            is.getUrl(), systemId, ModelHelper.getInformationModelURI(is.getInformationModelId()))).forEach(isInfo -> {
                log.debug("Saving interworking service | systemId: " + isInfo.getPlatformId() + " | serviceUrl: "
                        + isInfo.getInterworkingServiceURL() + " | serviceIri" + isInfo.getInterworkingServiceIRI());
                interworkingServiceInfoRepo.save(isInfo);
            });
        }
    }

    private String getLoadAllInterworkingServicesSPQRL() {
        return "PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#>\n" +
                "PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#>" +
                "\n" +
                "SELECT ?service ?serviceURL ?platformId ?informationModel FROM <http://www.symbiote-h2020.eu/ontology/internal/meta> WHERE {\n" +
                "\t?service a mim:InterworkingService;\n" +
                "\t\t\tmim:url ?serviceURL;\n" +
                "\t\t\tmim:usesInformationModel ?informationModel.\n" +
                "\t\t?platform cim:id ?platformId;\n" +
                "\t\t\tmim:hasService ?service.\n" +
                "} ";
    }

    private static final String SOLUTION_SERVICE_IRI = "service";
    private static final String SOLUTION_SERVICE_URL = "serviceURL";
    private static final String SOLUTION_PLATFORM_ID = "platformId";
    private static final String SOLUTION_INFORMATION_MODEL = "informationModel";

    public List<InterworkingServiceInfo> readInterworkingServicesFromTriplestore() {
        List<InterworkingServiceInfo> services = new ArrayList<>();
        ResultSet resultSet = this.storage.getTripleStore().executeQuery(getLoadAllInterworkingServicesSPQRL(), null, false);
        log.debug("Adding services: ");
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String serviceIRI = solution.get(SOLUTION_SERVICE_IRI).toString();
            String serviceURL = solution.get(SOLUTION_SERVICE_URL).toString();
            String platformId = solution.get(SOLUTION_PLATFORM_ID).toString();
            String informationModelIri = solution.get(SOLUTION_INFORMATION_MODEL).toString();

            log.debug(" serviceIRI: " + serviceIRI + " | serviceURL: " + serviceURL + " | platformId: " + platformId + " | informationModelIRI " + informationModelIri );
            services.add(new InterworkingServiceInfo(serviceIRI, serviceURL, platformId, informationModelIri));
        }
        return services;
    }

    //TODO check informationmodelId
    public void loadAndSaveInterworkingServicesFromTriplestore() {
        readInterworkingServicesFromTriplestore().stream().forEach(ii -> interworkingServiceInfoRepo.save(ii));
    }

    public void printStorage() {
        this.storage.getTripleStore().printDataset();
    }

    @Override
    public void registerInformationModel(InformationModel model) {
        log.debug("Inserting information model graph " + model.getUri());
        this.storage.registerModel( model  );
    }

    @Override
    public void deleteInformationModel(InformationModel model) {
        log.debug("Removing information model graph " + model.getUri());
        this.storage.removeNamedGraph( model.getUri() );
    }

    @Override
    public void updateInformationModel(InformationModel model) {
        log.debug("Updating information model graph " + model.getUri());
        this.storage.removeNamedGraph( model.getUri() );
        this.storage.registerModel( model );
    }
}
