package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import org.apache.commons.io.IOUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static eu.h2020.symbiote.TestSetupConfig.loadFileAsModel;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

//@RunWith(SpringRunner.class)
//@SpringBootTest({"eureka.client.enabled=false"})
@RunWith(MockitoJUnitRunner.class)
public class SearchApplicationTests {

    @Mock
    SecurityManager securityManager;

    @Test
    public void simplePlatformTest() throws Exception {
        when(securityManager.checkPolicyByResourceId(anyString(),any(),any())).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(anyString(),any(),any())).thenReturn(Boolean.TRUE);
        //In memory
        TripleStore triplestore = new TripleStore(securityManager,false);
        Registry registry = new Registry(triplestore);
        //Real
//        TripleStore triplestore = new TripleStore("f:\\coreSearchTriplestore",securityManager,false);

        try {
            String platformToSave = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformToSave, RDFFormat.Turtle);

//            StmtIterator stmtIterator = triplestore.getDefaultGraph().listStatements();
//            while( stmtIterator.hasNext() ) {
//                Statement next = stmtIterator.next();
//                System.out.println( next.getSubject().toString() + " - " + next.getPredicate().toString() + " - " + next.getObject().toString() );
//            }

            //Stationary res
//            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD");
//            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_STATIONARY_URI, stationaryModel);

            //Actuator res
            Model stationaryModel = loadFileAsModel(RESOURCE_ACTUATOR_FILENAME, "JSONLD");
            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_ACTUATOR_URI, stationaryModel);

            executeQuery(triplestore,"/q8.sparql");

        } catch( Exception e ) {
            e.printStackTrace();
            fail();
        }

    }


    @Test
    public void testSearchByObservedProperty() {
        TripleStore triplestore = new TripleStore(securityManager,false);
        Registry registry = new Registry(triplestore);
        try {
            String platformToSave = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformToSave, RDFFormat.Turtle);


            Model res1Model = loadFileAsModel( RESOURCE_101_FILENAME, "JSONLD" );
            Model res2Model = loadFileAsModel( RESOURCE_102_FILENAME, "TURTLE");
            Model res3Model = loadFileAsModel( RESOURCE_103_FILENAME, "TURTLE");

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res1Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res2Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res3Model);

            executeQuery(triplestore,"/q3.sparql");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testSearchPlatformByObservedProperty() {
        TripleStore triplestore = new TripleStore(securityManager,false);
        Registry registry = new Registry(triplestore);
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle);

            String platformB = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_B_FILENAME));
            registry.registerPlatform(PLATFORM_B_ID, platformB, RDFFormat.Turtle);

            String platformC = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_C_FILENAME));
            registry.registerPlatform(PLATFORM_C_ID, platformC, RDFFormat.Turtle);

            Model res101Model = loadFileAsModel( RESOURCE_101_FILENAME, "JSONLD" );
            Model res102Model = loadFileAsModel( RESOURCE_102_FILENAME, "TURTLE" );
            Model res103Model = loadFileAsModel( RESOURCE_103_FILENAME, "TURTLE");
            Model res201Model = loadFileAsModel( RESOURCE_201_FILENAME, "TURTLE" );
            Model res202Model = loadFileAsModel( RESOURCE_202_FILENAME, "TURTLE" );
            Model res301Model = loadFileAsModel( RESOURCE_301_FILENAME, "TURTLE" );

            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_101_URI, res101Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_102_URI, res102Model);
            registry.registerResource(PLATFORM_A_URI,PLATFORM_A_SERVICE_URI, RESOURCE_103_URI, res103Model);

            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI, RESOURCE_201_URI,res201Model);
            registry.registerResource(PLATFORM_B_URI,PLATFORM_B_SERVICE_URI, RESOURCE_202_URI,res202Model);
            registry.registerResource(PLATFORM_C_URI,PLATFORM_C_SERVICE_URI, RESOURCE_301_URI,res301Model);

            executeQuery(triplestore,"/q8.sparql");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private Model loadFileAsModel( String fileLocation, String format ) {
        Model model = null;
        InputStream modelToSave = null;
        try {
            modelToSave = IOUtils.toInputStream(IOUtils.toString(this.getClass()
                    .getResource(fileLocation)));
            model = ModelFactory.createDefaultModel();
            model.read(modelToSave, null, format);
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
        ResultSet resultSet = store.executeQuery(query,null,false);
        printResultSet(resultSet);
    }

//    private void executeQuery( TripleStore store, Query query ) throws IOException {
//        ResultSet resultSet = store.executeQuery(query,null,false);
//        printResultSet(resultSet);
//    }

    private void printResultSet( ResultSet resultSet ) {
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
        }
    }

}