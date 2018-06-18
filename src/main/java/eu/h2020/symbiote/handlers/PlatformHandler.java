package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.cloud.model.ssp.SspRegInfo;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.model.mim.SmartSpace;
import eu.h2020.symbiote.query.DeletePlatformRequestGenerator;
import eu.h2020.symbiote.query.DeleteSdevRequestGenerator;
import eu.h2020.symbiote.query.DeleteSspRequestGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.update.UpdateRequest;

/**
 * Implementation of the handler for the platform related events.
 * <p>
 * Created by Mael on 11/01/2017.
 */
public class PlatformHandler implements IPlatformEvents, ISspEvents {

    private static final Log log = LogFactory.getLog(PlatformHandler.class);

    private final SearchStorage storage;

    /**
     * Create a handler of the platform events for specified storage.
     *
     * @param storage Storage on which the events should be executed.
     */
    public PlatformHandler(SearchStorage storage) {
        this.storage = storage;
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

            System.out.print(subject.toString());
            System.out.print(" " + predicate.toString() + " ");
            if (object instanceof Resource) {
                System.out.print(object.toString());
            } else {
                // object is a literal
                System.out.print(" \"" + object.toString() + "\"");
            }

            log.debug(" .");
        }

        log.debug("End of model");
        storage.registerPlatform(platform.getId(), platformModel );
        storage.getTripleStore().printDataset();

        return true;
    }

    @Override
    public boolean updatePlatform(Platform platform) {
        log.debug("Updating platform " + platform.getId());

        boolean success = deletePlatform(platform.getId());
        if( success ) {
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
        storage.getTripleStore().printDataset();
        return true;
    }


    @Override
    public boolean registerSsp(SmartSpace ssp) {

        Model sspModel = HandlerUtils.generateModelFromSsp(ssp);

        storage.registerSsp(ssp.getId(), sspModel );
//        storage.getTripleStore().printDataset();

        return true;

    }

    @Override
    public boolean updateSsp(SmartSpace ssp) {
        log.debug("Updating ssp " + ssp.getId());

        boolean success = deleteSsp(ssp.getId());
        if( success ) {
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
        return true;
    }

    @Override
    public boolean registerSdev(SspRegInfo sdev) {

        Model sdevModel = HandlerUtils.generateModelFromSdev(sdev);

        storage.registerSdev(sdev.getSymId(), sdevModel );
//        storage.getTripleStore().printDataset();

        return true;
    }

    @Override
    public boolean updateSdev(SspRegInfo sdev) {
        log.debug("Updating sdev " + sdev.getSymId());

        boolean success = deleteSdev(sdev.getSymId());
        if( success ) {
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


    public void printStorage() {
        this.storage.getTripleStore().printDataset();
    }
}
