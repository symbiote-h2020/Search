package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.CoreResource;
import eu.h2020.symbiote.core.internal.CoreResourceRegisteredOrModifiedEventPayload;
import eu.h2020.symbiote.filtering.AccessPolicyRepo;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.InterworkingServiceInfo;
import eu.h2020.symbiote.handlers.InterworkingServiceInfoRepo;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.handlers.ResourceHandler;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Mael on 18/01/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class ResourceRegistrationTest {

    @Mock
    private SecurityManager securityManager;
    @Mock
    private AccessPolicyRepo accessPolicyRepo;
    @Mock
    private InterworkingServiceInfoRepo interworkingServiceInfoRepo;

//    private static final String PLATFORM_A_ID = "1";
//    private static final String PLATFORM_A_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/1";
//    private static final String PLATFORM_A_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/internal/platforms/1/service/somehost1.com/resourceAccessProxy";
//    private static final String PLATFORM_A_INFORMATION_MODEL_ID = "11";
//    private static final String PLATFORM_A_DESCRIPTION = "Test platform A";
//    private static final String PLATFORM_A_NAME = "Platform A";
//    private static final String PLATFORM_A_URL = "http://somehost1.com/resourceAccessProxy";


//    private static final String RESOURCE_PREDICATE = "https://www.symbiote-h2020.eu/ontology/internal/resources/";


//    public static final String PLATFORM_ID = "1";
//    public static final String PLATFORM_INFORMATION_MODEL_ID = "11";

    @Test
    public void testReadingResourceModelFromFile() {
        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource(RESOURCE_101_FILENAME)));
            Model mFromFile = ModelFactory.createDefaultModel();
            mFromFile.read(modelToSave,null,"JSONLD");
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
            mFromFile.read(modelToSave,null,"JSONLD");

            SearchStorage.clearStorage();
            SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME,  securityManager, false );
            searchStorage.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_101_URI, mFromFile);

        } catch (IOException e ) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    public void testRegisterHandler() {

        InterworkingServiceInfo infoToBeReturned = new InterworkingServiceInfo("http://iri","http://url",PLATFORM_A_ID,"http://modelIri");

        when(interworkingServiceInfoRepo.findByInterworkingServiceURL(anyString())).thenReturn(Arrays.asList(infoToBeReturned));

        SearchStorage.clearStorage();
        SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME, securityManager, false );
        PlatformHandler handler = new PlatformHandler(searchStorage,interworkingServiceInfoRepo);
        Platform platform = generatePlatformA();
        boolean result = handler.registerPlatform(platform);
        assert(result);

        CoreResource res = generateResource();

        ResourceHandler resHandler = new ResourceHandler(searchStorage,accessPolicyRepo,interworkingServiceInfoRepo);
        CoreResourceRegisteredOrModifiedEventPayload regReq = new CoreResourceRegisteredOrModifiedEventPayload();
        regReq.setPlatformId(PLATFORM_A_ID);

        regReq.setResources(Arrays.asList(res));
        result = resHandler.registerResource(regReq);
        assert(result);
    }

    @Test
    public void testAddingStationarySensorWithoutLocation() {
        try {

            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource(RESOURCE_STATIONARY_FILENAME)));
            Model mFromFile = ModelFactory.createDefaultModel();
            mFromFile.read(modelToSave,null,"JSONLD");
            SearchStorage.clearStorage();
            SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME, securityManager, false );
            searchStorage.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI,RESOURCE_STATIONARY_URI, mFromFile);
            searchStorage.getTripleStore().printDataset();
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

//    private void fireQueryAll(SearchStorage storage) {
//        Query informationServiceQuery;
//        SelectBuilder sb = new SelectBuilder()
//                .addVar( "*" )
//                .addWhere( "?s", "?p", "?o" );
//
//        Query q = sb.build() ;
//        List<String> results = storage.query("", q);
//        System.out.println( "SearchRequest results: ");
//        System.out.println( results );
//    }

}
