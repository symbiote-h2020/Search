package eu.h2020.symbiote;

import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.handlers.InterworkingServiceInfo;
import eu.h2020.symbiote.handlers.InterworkingServiceInfoRepo;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.mim.InterworkingService;
import eu.h2020.symbiote.model.mim.Platform;
import eu.h2020.symbiote.search.SearchStorage;
import eu.h2020.symbiote.semantics.ModelHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Szymon Mueller on 23/06/2018.
 */
@RunWith(MockitoJUnitRunner.class)
public class InterworkingInterfaceRepoTests {

    @Mock
    private InterworkingServiceInfoRepo repo;
    private PlatformHandler platformHandler;

    private SearchStorage searchStorage;

    @Mock
    private SecurityManager secMan;

    private static final Platform platform1;

    private static final InterworkingService platform1_interworkingservice1;
    private static final String platform1_interworkingservice1_url = "https://www.platform1.com/interworking1";
//    private static final String platform1_interworkingservice1_iri;

    private static final InterworkingService platform1_interworkingservice2;
    private static final String platform1_interworkingservice2_url = "https://www.platform1.com/interworking2";

//    private static final SmartSpace ssp1;

    public static final String PLATFORM_1_ID = "PLATFORM1_ID";

    public static final String PLATFORM_1_NAME = "Platform_1_name";

    static {
        platform1_interworkingservice1 = new InterworkingService();
        platform1_interworkingservice1.setUrl(platform1_interworkingservice1_url);
        platform1_interworkingservice1.setInformationModelId("1");

        platform1_interworkingservice2 = new InterworkingService();
        platform1_interworkingservice2.setUrl(platform1_interworkingservice2_url);
        platform1_interworkingservice2.setInformationModelId("1");

        platform1 = new Platform();
        platform1.setEnabler(false);
        platform1.setId(PLATFORM_1_ID);
        platform1.setName(PLATFORM_1_NAME);
        platform1.setInterworkingServices(Arrays.asList(platform1_interworkingservice1,platform1_interworkingservice2));

    }

    @Before
    public void setUp() {
        searchStorage = SearchStorage.getInstance(SearchApplication.DIRECTORY,secMan,false);
        platformHandler = new PlatformHandler(searchStorage,repo);
    }

    @Test
    public void testLoadingInterworkingServicesFromTriplestore() {
        List<InterworkingServiceInfo> foundII = platformHandler.readInterworkingServicesFromTriplestore();
        assertNotNull(foundII);
    }

    @Test
    public void saveToRepoTest() {
        platformHandler.saveInterworkingServicesInfoForPlatform(platform1.getId(),
                ModelHelper.getPlatformURI(platform1.getId()),platform1.getInterworkingServices());

        //Check if two services are in repo

    }

}
