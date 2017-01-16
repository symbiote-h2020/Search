package eu.h2020.symbiote;

import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest({"eureka.client.enabled=false"})
public class SearchApplicationTests {

    private static final String PLATFORM1_ID = "1";
    private static final String TEST1_PRED = "http://xmlns.com/foaf/0.1/name";
    private static final String TEST1_OBJECT = "dev";

    private static final String PLATFORM1_URI = Ontology.PLATFORMS_GRAPH + "/" + PLATFORM1_ID;
    private static final String METAMODEL_PRED = Ontology.IS_A;
    private static final String METAMODEL_OBJECT = Ontology.PLATFORM;

    private static final String PLATFORM2_ID = "11111";
    private static final String PLATFORM2_URI = Ontology.PLATFORMS_GRAPH + "/" + PLATFORM2_ID;
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

        String modelId = "22222";

        try {
            String modelToSave = IOUtils.toString(this.getClass()
                    .getResource("/platformA.ttl"));
            registry.registerPlatform(PLATFORM2_ID, modelToSave, RDFFormat.Turtle, modelId);

            //Check if metainformation added correctly
            Model metaGraph = triplestore.getGraph(Ontology.PLATFORMS_GRAPH);
            assertModelEqualsSingleSPO(metaGraph,PLATFORM2_URI,METAMODEL_PRED,METAMODEL_OBJECT);

            //Check rdf added correctly
            Model graph = triplestore.getGraph(PLATFORM2_URI);
//            graph.write(System.out,"TURTLE");
            assertEquals("Size of saved graph should be " + 7l + " but is " + graph.size(), graph.size(),7l);


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

}