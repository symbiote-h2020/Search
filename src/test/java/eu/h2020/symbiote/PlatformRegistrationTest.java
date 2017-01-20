package eu.h2020.symbiote;

/**
 * Created by Mael on 11/01/2017.
 */

import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.handlers.PlatformHandler;
import eu.h2020.symbiote.model.Platform;
import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest({"eureka.client.enabled=false"})
public class PlatformRegistrationTest {

    private static final String PLATFORM1_ID = "1";
    private static final String TEST1_PRED = "http://xmlns.com/foaf/0.1/name";
    private static final String TEST1_OBJECT = "dev";

    private static final String PLATFORM1_URI = Ontology.PLATFORMS_GRAPH + "/" + PLATFORM1_ID;
    private static final String METAMODEL_PRED = Ontology.IS_A;
    private static final String METAMODEL_OBJECT = Ontology.PLATFORM;

    private static final String PLATFORM2_ID = "2";
    private static final String PLATFORM2_URI = Ontology.PLATFORMS_GRAPH + "/" + PLATFORM2_ID;
    private static final String PLATFORM2_MODEL_ID = "21";
    private static final String PLATFORM2_DESC_PRED = "http://www.symbiote-h2020.eu/ontology/meta.owl#hasDescription";
    private static final String PLATFORM2_DESC_VALUE = "Test platform";
    private static final String PLATFORM2_NAME_PRED = "http://www.symbiote-h2020.eu/ontology/meta.owl#hasName";
    private static final String PLATFORM2_NAME_VALUE = "Platform A";
    private static final String PLATFORM2_SERVICE_PRED = "http://www.symbiote-h2020.eu/ontology/meta.owl#hasService";
    private static final String PLATFORM2_SERVICE_INFOMODEL_PRED = "http://www.symbiote-h2020.eu/ontology/meta.owl#hasInformationModel";
    private static final String PLATFORM2_SERVICE_INFOMODEL_ID_PRED = "http://www.symbiote-h2020.eu/ontology/meta.owl#hasID";
    private static final String PLATFORM2_SERVICE_INFOMODEL_ID_VALUE = "22222";
    private static final String PLATFORM2_SERVICE_URL_PRED = "http://www.symbiote-h2020.eu/ontology/meta.owl#hasURL";
    private static final String PLATFORM2_SERVICE_URL_VALUE = "http://somehost.com/resourceAccessProxy";

    public static final String PLATFORM_ID = "1";
    public static final String PLATFORM_INFORMATION_MODEL_ID = "11";
    public static final String PLATFORM_DESCRIPTION = "Test platform A";
    public static final String PLATFORM_NAME = "Platform A";
    public static final String PLATFORM_URL = "http://somehost1.com/resourceAccessProxy";

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
        assertEquals("Created model should have " + 8l + " entries, but has " + model.size(), 8l, model.size() );

    }

    @Test
    public void testSavePlatformThroughSearchStorage() {
        SearchStorage searchStorage = SearchStorage.getInstance( SearchStorage.TESTCASE_STORAGE_NAME );
        PlatformHandler handler = new PlatformHandler(searchStorage);
        Platform platform = createPlatform();
        boolean result = handler.registerPlatform(platform);
        assert(result);


        try {
            executeQuery(searchStorage.getTripleStore(), "/q5.sparql");
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        Model graph = searchStorage.getTripleStore().getGraph(Ontology.getPlatformGraphURI(platform.getPlatformId()));
        assertNotNull(graph);


        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource("/platformA.ttl")));
            Model modelFromFile = ModelFactory.createDefaultModel();
            modelFromFile.read(modelToSave,null,"TURTLE");

            assertEquals("Number of statements must be the same, expected " + modelFromFile.size()
                    + ", actual " + graph.size(), modelFromFile.size(), graph.size());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void testTriplestoreGraphInsert() {
        TripleStore tripleStore = new TripleStore();
        try {
            String modelToSave = IOUtils.toString( this.getClass()
                    .getResource("/test1Insert.ttl"));
            tripleStore.insertGraph(PLATFORM1_URI, modelToSave, RDFFormat.Turtle);
            Model graph = tripleStore.getGraph(PLATFORM1_URI);
            assertModelEqualsSingleSPO(graph,PLATFORM1_URI,TEST1_PRED,TEST1_OBJECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegistryPlatformRegister() {
        TripleStore triplestore = new TripleStore();
        Registry registry = new Registry(triplestore);

        String modelId = "111";

        try {
            String modelToSave = IOUtils.toString(this.getClass()
                    .getResource("/test1Insert.ttl"));
            registry.registerPlatform(PLATFORM1_ID, modelToSave, RDFFormat.Turtle, modelId);

            //Check rdf added correctly
            Model graph = triplestore.getGraph(PLATFORM1_URI);
            assertModelEqualsSingleSPO(graph,PLATFORM1_URI,TEST1_PRED,TEST1_OBJECT);

            //Check if metainformation added correctly
            Model metaGraph = triplestore.getGraph(Ontology.PLATFORMS_GRAPH);
            assertModelEqualsSingleSPO(metaGraph,PLATFORM1_URI,METAMODEL_PRED,METAMODEL_OBJECT);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegistryNestedPlatformRegister() {
        TripleStore triplestore = new TripleStore();
        Registry registry = new Registry(triplestore);


        try {
            String modelToSave = IOUtils.toString(this.getClass()
                    .getResource("/platformA.ttl"));
            registry.registerPlatform(PLATFORM2_ID, modelToSave, RDFFormat.Turtle, PLATFORM2_MODEL_ID);

            //Check if metainformation added correctly
            Model metaGraph = triplestore.getGraph(Ontology.PLATFORMS_GRAPH);
            assertModelEqualsSingleSPO(metaGraph,PLATFORM2_URI,METAMODEL_PRED,METAMODEL_OBJECT);

            //Check rdf added correctly
            Model graph = triplestore.getGraph(PLATFORM2_URI);
            graph.write(System.out,"TURTLE");
            assertEquals("Size of saved graph should be " + 7l + " but is " + graph.size(), 8l, graph.size());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void assertModelEqualsSingleSPO(Model graph, String subject, String predicate, String object) {
        assertNotNull(graph);
        StmtIterator stmtIterator = graph.listStatements();
        assertTrue(stmtIterator.hasNext());
        Statement next = stmtIterator.next();
        assertNotNull(next);
        String readSubject = next.getSubject().toString();
        String readPredicate = next.getPredicate().toString();
        String readObject = next.getObject().toString();
        assertEquals("Subject should be the same",subject,readSubject);
        assertEquals("Predicate should be the same",predicate,readPredicate);
        assertEquals("Object should be the same",object,readObject);
        assertFalse(stmtIterator.hasNext());
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

    private void executeQuery( TripleStore store, String filename ) throws IOException {
        String query = IOUtils.toString(this.getClass()
                .getResource(filename));
        ResultSet resultSet = store.executeQuery(query);
        System.out.println("=============== Query " + filename + " Execution results ===============" );
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            Iterator<String> varNames = solution.varNames();
            String temp = "";
            while (varNames.hasNext()) {
                String var = varNames.next();
                if (!temp.isEmpty()) {
                    temp += ", ";
                }
                temp += var + " = " + solution.get(var).toString();
            }

            System.out.println( temp );

        }
        System.out.println("========================================================================" );
    }

}
