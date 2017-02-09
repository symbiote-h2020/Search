package eu.h2020.symbiote;

import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.model.Location;
import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.model.Resource;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static eu.h2020.symbiote.TestSetupConfig.*;

/**
 * Created by Mael on 18/01/2017.
 */
public class ResourceRegistrationTest {

//    private static final String PLATFORM_A_ID = "1";
//    private static final String PLATFORM_A_URI = "http://www.symbiote-h2020.eu/ontology/platforms/1";
//    private static final String PLATFORM_A_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/1/service/somehost1.com/resourceAccessProxy";
//    private static final String PLATFORM_A_INFORMATION_MODEL_ID = "11";
//    private static final String PLATFORM_A_DESCRIPTION = "Test platform A";
//    private static final String PLATFORM_A_NAME = "Platform A";
//    private static final String PLATFORM_A_URL = "http://somehost1.com/resourceAccessProxy";


//    private static final String RESOURCE_PREDICATE = "https://www.symbiote-h2020.eu/ontology/resources/";


//    public static final String PLATFORM_ID = "1";
//    public static final String PLATFORM_INFORMATION_MODEL_ID = "11";

    @Test
    public void testReadingResourceModelFromFile() {
        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource(RESOURCE_101_FILENAME)));
            Model mFromFile = ModelFactory.createDefaultModel();
            mFromFile.read(modelToSave,null,"TURTLE");
            assertNotNull(mFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegisterResource() {
        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource(RESOURCE_101_FILENAME)));
            Model mFromFile = ModelFactory.createDefaultModel();
            mFromFile.read(modelToSave,null,"TURTLE");

            SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME );
            searchStorage.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_101_URI, mFromFile);

        } catch (IOException e ) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegisterHandler() {

        SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME );
        PlatformHandler handler = new PlatformHandler(searchStorage);
        Platform platform = generatePlatformA();
        boolean result = handler.registerPlatform(platform);
        assert(result);

        Resource res = generateResource();

        ResourceHandler resHandler = new ResourceHandler(searchStorage);
        result = resHandler.registerResource(res);
        assert(result);
    }

    private void fireQueryAll(SearchStorage storage) {
        Query informationServiceQuery;
        SelectBuilder sb = new SelectBuilder()
                .addVar( "*" )
                .addWhere( "?s", "?p", "?o" );

        Query q = sb.build() ;
        List<String> results = storage.query("", q);
        System.out.println( "SearchRequest results: ");
        System.out.println( results );
    }

}