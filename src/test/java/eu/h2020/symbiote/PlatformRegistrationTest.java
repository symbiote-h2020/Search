package eu.h2020.symbiote;

/**
 * Created by Mael on 11/01/2017.
 */

import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

//@RunWith(SpringRunner.class)
//@SpringBootTest({"eureka.client.enabled=false"})
public class PlatformRegistrationTest {

    public static final String PLATFORM_ID = "11111";
    public static final String PLATFORM_INFORMATION_MODEL_ID = "22222";
    public static final String PLATFORM_DESCRIPTION = "Test platform";
    public static final String PLATFORM_NAME = "Platform A";
    public static final String PLATFORM_URL = "http://somehost.com/resourceAccessProxy";

    @Test
    public void testReadingModelFromFile() {
        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource("/platformA.ttl")));
            Model mFromFile = ModelFactory.createDefaultModel();
            mFromFile.read(modelToSave,null,"TURTLE");
            assertNotNull(mFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGeneratingModelFromPlatform() {
        Platform platform = createPlatform();

        Model model = HandlerUtils.generateModelFromPlatform(platform);
        assertNotNull(model);
        assertEquals("Created model should have " + 7 + " entries, but has " + model.size(), model.size(),7l);

    }

    @Test
    public void testSavePlatformThroughSearchStorage() {
        SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME );
        PlatformHandler handler = new PlatformHandler(searchStorage);
        Platform platform = createPlatform();
        boolean result = handler.registerPlatform(platform);
        assert(result);
        Model graph = searchStorage.getTripleStore().getGraph(Ontology.getPlatformGraphURI(platform.getPlatformId()));
        assertNotNull(graph);


        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource("/platformA.ttl")));
            Model modelFromFile = ModelFactory.createDefaultModel();
            modelFromFile.read(modelToSave,null,"TURTLE");

//            System.out.println( "FROM OBJECT PLATFORM: ");
//            graph.write(System.out,"TURTLE");
//            System.out.println( "==============================");
//            System.out.println( "FROM FILE PLATFORM: ");
//            modelFromFile.write(System.out,"TURTLE");
//            System.out.println( "==============================");
//            Model diff = modelFromFile.difference(graph);
//            System.out.println( "DIFF: ");
//            diff.write(System.out,"TURTLE");
//            System.out.println( "==============================");
//            Model same = modelFromFile.intersection(graph);
//            System.out.println( "INTERSECTION: ");
//            same.write(System.out,"TURTLE");

            assertEquals("Number of statements must be the same, expected " + modelFromFile.size()
                    + ", actual " + graph.size(), modelFromFile.size(), graph.size());
////            assert( graph.containsAll(modelFromFile) );
////            assert( modelFromFile.containsAll(graph) );
//            StmtIterator graphIterator = graph.listStatements();
//            while( graphIterator.hasNext() ) {
//                Statement graphSt = graphIterator.next();
//                Resource subject = graphSt.getSubject();
//                Property predicate = graphSt.getPredicate();
//                RDFNode object = graphSt.getObject();
//
//                System.out.println( "   " + subject.toString() + "  |  " + predicate.toString() + "  |  " + object.toString() );
//            }
//
//            System.out.println( "   MODEL FROM FILE" );
//            StmtIterator fileIterator = modelFromFile.listStatements();
//            while( fileIterator.hasNext() ) {
//                Statement graphSt = fileIterator.next();
//                Resource subject = graphSt.getSubject();
//                Property predicate = graphSt.getPredicate();
//                RDFNode object = graphSt.getObject();
//
//                System.out.println( "   " + subject.toString() + "  |  " + predicate.toString() + "  |  " + object.toString() );
//            }


        } catch (IOException e) {
            e.printStackTrace();
        }


//        search.registerPlatform();
    }


    private Platform createPlatform() {
        Platform platform = new Platform();
        platform.setPlatformId(PLATFORM_ID);
        platform.setInformationModelId(PLATFORM_INFORMATION_MODEL_ID);
        platform.setDescription(PLATFORM_DESCRIPTION);
        platform.setName(PLATFORM_NAME);
        platform.setUrl(PLATFORM_URL);
        return platform;
    }

}
