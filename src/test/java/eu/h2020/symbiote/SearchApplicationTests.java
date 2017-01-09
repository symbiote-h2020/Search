package eu.h2020.symbiote;

import eu.h2020.symbiote.ontology.model.model.Ontology;
import eu.h2020.symbiote.ontology.model.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.model.Registry;
import eu.h2020.symbiote.ontology.model.model.TripleStore;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigInteger;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest({"eureka.client.enabled=false"})
public class SearchApplicationTests {

    private static final BigInteger PLATFORM1_ID = BigInteger.valueOf(1l);
//    private static final String TEST1_URI = "http://www.example.com/test1";
    private static final String TEST1_PRED = "http://xmlns.com/foaf/0.1/name";
    private static final String TEST1_OBJECT = "dev";

    private static final String PLATFORM1_URI = Ontology.PLATFORMS_GRAPH + "/" + PLATFORM1_ID;
    private static final String METAMODEL_PRED = Ontology.IS_A;
    private static final String METAMODEL_OBJECT = Ontology.PLATFORM;


	@Test
	public void testTriplestoreGraphInsert() {
		TripleStore tripleStore = new TripleStore();
        try {
            String modelToSave = IOUtils.toString( this.getClass()
                    .getResource("/test1Insert.ttl"));
            tripleStore.insertGraph(PLATFORM1_URI, modelToSave, RDFFormat.Turtle);
            Model graph = tripleStore.getGraph(PLATFORM1_URI);
            assertModelEqualsSPO(graph,PLATFORM1_URI,TEST1_PRED,TEST1_OBJECT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRegistryPlatformRegister() {
        TripleStore triplestore = new TripleStore();
        Registry registry = new Registry(triplestore);

        BigInteger modelId = BigInteger.valueOf(111l);

        try {
            String modelToSave = IOUtils.toString(this.getClass()
                    .getResource("/test1Insert.ttl"));
            registry.registerPlatform(PLATFORM1_ID, modelToSave, RDFFormat.Turtle, modelId);

            //Check rdf added correctly
            Model graph = triplestore.getGraph(PLATFORM1_URI);
            assertModelEqualsSPO(graph,PLATFORM1_URI,TEST1_PRED,TEST1_OBJECT);

            //Check if metainformation added correctly
            Model metaGraph = triplestore.getGraph(Ontology.PLATFORMS_GRAPH);
            assertModelEqualsSPO(metaGraph,PLATFORM1_URI,METAMODEL_PRED,METAMODEL_OBJECT);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void assertModelEqualsSPO(Model graph, String subject, String predicate, String object) {
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