package eu.h2020.symbiote;

import eu.h2020.symbiote.ontology.model.Ontology;
import eu.h2020.symbiote.ontology.model.RDFFormat;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.search.SearchStorage;
import org.apache.commons.io.IOUtils;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.junit.Assert.*;

//@RunWith(SpringRunner.class)
//@SpringBootTest({"eureka.client.enabled=false"})
public class SearchApplicationTests {

    private static final String QUERY_OBSERVEDPROERTY = "/queryByObservedProperty.sparql";

    private static final String PLATFORM_A_ID = "1";
    private static final String PLATFORM_A_MODEL_ID = "11";
    private static final String PLATFORM_A_FILENAME = "/platformA.ttl";
    private static final String PLATFORM_A_URI = "http://www.symbiote-h2020.eu/ontology/platforms/1";
    private static final String PLATFORM_A_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/1/service/somehost1.com/resourceAccessProxy";

    private static final String PLATFORM_B_ID = "2";
    private static final String PLATFORM_B_MODEL_ID = "21";
    private static final String PLATFORM_B_FILENAME = "/platformB.ttl";
    private static final String PLATFORM_B_URI = "http://www.symbiote-h2020.eu/ontology/platforms/2";
    private static final String PLATFORM_B_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/2/service/somehost2.com/resourceAccessProxy";

    private static final String PLATFORM_C_ID = "3";
    private static final String PLATFORM_C_MODEL_ID = "31";
    private static final String PLATFORM_C_FILENAME = "/platformC.ttl";
    private static final String PLATFORM_C_URI = "http://www.symbiote-h2020.eu/ontology/platforms/3";
    private static final String PLATFORM_C_SERVICE_URI = "http://www.symbiote-h2020.eu/ontology/platforms/3/service/somehost3.com/resourceAccessProxy";

    private static final String RESOURCE_PREDICATE = "https://www.symbiote-h2020.eu/ontology/resources/";

    private static final String RESOURCE_101_FILENAME = "/resource101.ttl";
    private static final String RESOURCE_101_URI = RESOURCE_PREDICATE + "101";
    private static final String RESOURCE_102_FILENAME = "/resource102.ttl";
    private static final String RESOURCE_102_URI = RESOURCE_PREDICATE + "102";
    private static final String RESOURCE_103_FILENAME = "/resource103.ttl";
    private static final String RESOURCE_103_URI = RESOURCE_PREDICATE + "103";
    private static final String RESOURCE_201_FILENAME = "/resource201.ttl";
    private static final String RESOURCE_201_URI = RESOURCE_PREDICATE + "201";
    private static final String RESOURCE_202_FILENAME = "/resource202.ttl";
    private static final String RESOURCE_202_URI = RESOURCE_PREDICATE + "202";
    private static final String RESOURCE_301_FILENAME = "/resource301.ttl";
    private static final String RESOURCE_301_URI = RESOURCE_PREDICATE + "301";


    @Test
    public void simplePlatformTest() {
        TripleStore triplestore = new TripleStore();
        Registry registry = new Registry(triplestore);
        try {
            String platformToSave = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformToSave, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);


            executeQuery(triplestore,"/q6.sparql");

        } catch( Exception e ) {

        }

    }


    @Test
    public void testSearchByObservedProperty() {
        TripleStore triplestore = new TripleStore();
        Registry registry = new Registry(triplestore);
        try {
            String platformToSave = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformToSave, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);


            Model res1Model = loadFileAsModel( RESOURCE_101_FILENAME );
            Model res2Model = loadFileAsModel( RESOURCE_102_FILENAME);
            Model res3Model = loadFileAsModel( RESOURCE_103_FILENAME);

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res1Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res2Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res3Model);

//            executeQuery(triplestore, "/queryAll.sparql"  );
            executeQuery(triplestore,"/q3.sparql");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSearchPlatformByObservedProperty() {
        TripleStore triplestore = new TripleStore();
        Registry registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle, PLATFORM_A_MODEL_ID);

            String platformB = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_B_FILENAME));
            registry.registerPlatform(PLATFORM_B_ID, platformB, RDFFormat.Turtle, PLATFORM_B_MODEL_ID);

            String platformC = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_C_FILENAME));
            registry.registerPlatform(PLATFORM_C_ID, platformC, RDFFormat.Turtle, PLATFORM_C_MODEL_ID);

            Model res101Model = loadFileAsModel( RESOURCE_101_FILENAME );
            Model res102Model = loadFileAsModel( RESOURCE_102_FILENAME);
            Model res103Model = loadFileAsModel( RESOURCE_103_FILENAME);
            Model res201Model = loadFileAsModel( RESOURCE_201_FILENAME );
            Model res202Model = loadFileAsModel( RESOURCE_202_FILENAME );
            Model res301Model = loadFileAsModel( RESOURCE_301_FILENAME );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res101Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res102Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res103Model);

            registry.registerResource(PLATFORM_B_URI,PLATFORM_A_SERVICE_URI, RESOURCE_201_URI,res201Model);
            registry.registerResource(PLATFORM_B_URI,PLATFORM_A_SERVICE_URI, RESOURCE_202_URI,res202Model);
            registry.registerResource(PLATFORM_C_URI,PLATFORM_A_SERVICE_URI, RESOURCE_301_URI,res301Model);

            executeQuery(triplestore,"/q4.sparql");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Model loadFileAsModel( String fileLocation ) {
        Model model = null;
        InputStream modelToSave = null;
        try {
            modelToSave = IOUtils.toInputStream(IOUtils.toString(this.getClass()
                    .getResource(fileLocation)));
            model = ModelFactory.createDefaultModel();
            model.read(modelToSave, null, "TURTLE");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (modelToSave != null) {
                try {
                    modelToSave.close();
                } catch (Exception e) {
                }
            }
        }
        return model;
    }

    private void executeQuery( TripleStore store, String filename ) throws IOException {
        String query = IOUtils.toString(this.getClass()
                .getResource(filename));
        ResultSet resultSet = store.executeQuery(query);
        System.out.println(">>>>>>>>>>>>> Executing query " + filename);
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
        System.out.println("<<<<<<<<<<<<< Query finished ");
    }

    private void executeQuery( TripleStore store, Query query ) throws IOException {
        ResultSet resultSet = store.executeQuery(query);
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

    }

}