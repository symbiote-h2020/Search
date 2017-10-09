package eu.h2020.symbiote.handlers;

import eu.h2020.symbiote.core.model.Platform;
import eu.h2020.symbiote.query.DeletePlatformRequestGenerator;
import eu.h2020.symbiote.query.UpdatePlatformRequestGenerator;
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
public class PlatformHandler implements IPlatformEvents {

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
//        UpdateRequest updateRequest = new UpdatePlatformRequestGenerator(platform).generateRequest();
//        this.storage.getTripleStore().executeUpdate(updateRequest);
        boolean success = deletePlatform(platform.getId());
        if( success ) {
            log.debug("Delete step of update performed successfully ");
            registerPlatform(platform);
        } else {
            log.error("Delete step of update failed ");
        }
        storage.getTripleStore().printDataset();
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
}
