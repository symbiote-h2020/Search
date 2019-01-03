package eu.h2020.symbiote;

import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.filtering.SecurityManager;
import eu.h2020.symbiote.ontology.model.Registry;
import eu.h2020.symbiote.ontology.model.TripleStore;
import eu.h2020.symbiote.query.QueryGenerator;
import eu.h2020.symbiote.search.SearchStorage;
import eu.h2020.symbiote.security.communication.payloads.SecurityRequest;
import org.apache.commons.io.IOUtils;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static eu.h2020.symbiote.TestSetupConfig.*;
import static eu.h2020.symbiote.search.SearchStorage.TESTCASE_STORAGE_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Szymon Mueller on 16/10/2017.
 */
@RunWith(MockitoJUnitRunner.class)
public class FilteringTests {

//    private final String RESOURCE1_ID = "resource1";
//    private final String RESOURCE2_ID = "resource2";

    private TripleStore triplestore;
    private Registry registry;

    private SecurityRequest reqUser1;
    private SecurityRequest reqUser2;
    private SecurityRequest reqUser3;

    @Mock
    private SecurityManager securityManager;

    private boolean securityEnabled = true;
    private SearchStorage searchStorageInstance;

    @Before
    public void setUp() throws Exception {
        SearchStorage.clearStorage();
        searchStorageInstance = SearchStorage.getInstance(TESTCASE_STORAGE_NAME, securityManager, securityEnabled);

        triplestore = searchStorageInstance.getTripleStore();
        registry = new Registry(triplestore);

        setUpTwoResources();
    }

    public void setUpContentsGenerated() throws Exception {
        StressTestLoadGenerator generator = new StressTestLoadGenerator();
        Map<String, List<String>> platformResourcesMap = generator.generatePlatformsWithResources(searchStorageInstance, 2, 10);

        //Init policies


        reqUser1 = new SecurityRequest("test1");
        reqUser2 = new SecurityRequest("test2");
        reqUser3 = new SecurityRequest("test3");

        int i = 0;

        for (String platformId : platformResourcesMap.keySet()) {
            List<String> resources = platformResourcesMap.get(platformId);
            for (String resIri : resources) {
                i++;
                when(securityManager.checkPolicyByResourceIri(eq(resIri), eq(reqUser1),any())).thenReturn((i % 3) == 1 ? Boolean.FALSE : Boolean.TRUE);
                when(securityManager.checkPolicyByResourceIri(eq(resIri), eq(reqUser2),any())).thenReturn((i % 3) == 2 ? Boolean.FALSE : Boolean.TRUE);
                when(securityManager.checkPolicyByResourceIri(eq(resIri), eq(reqUser3),any())).thenReturn((i % 3) == 0 ? Boolean.FALSE : Boolean.TRUE);
            }
        }

    }

    public void setUpTwoResources() {
        try {
            String platformA = IOUtils.toString(this.getClass()
                    .getResource(PLATFORM_A_FILENAME));
            registry.registerPlatform(PLATFORM_A_ID, platformA, RDFFormat.Turtle);

            Model stationaryModel = loadFileAsModel(RESOURCE_STATIONARY_FILENAME, "JSONLD");
            Model mobileModel = loadFileAsModel(RESOURCE_MOBILE_FILENAME, "JSONLD");

            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_STATIONARY_URI, stationaryModel);
            registry.registerResource(PLATFORM_A_URI, PLATFORM_A_SERVICE_URI, RESOURCE_MOBILE_URI, mobileModel);

            //Print
//            OntModel defGraph = triplestore.getNamedOntModel(TripleStore.DEFAULT_GRAPH);
//            defGraph.write(System.out, "TURTLE");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initPolicies2() throws Exception {
        reqUser1 = new SecurityRequest("test1");
        reqUser2 = new SecurityRequest("test2");
//        when(securityManager.checkPolicyByResourceId(eq(RESOURCE_STATIONARY_ID),reqUser1)).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(eq(RESOURCE_STATIONARY_URI), eq(reqUser1),any())).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(eq(RESOURCE_STATIONARY_URI), eq(reqUser2),any())).thenReturn(Boolean.FALSE);
//        when(securityManager.checkPolicyByResourceId(eq(RESOURCE_MOBILE_ID),reqUser2)).thenReturn(Boolean.FALSE);
        when(securityManager.checkPolicyByResourceIri(eq(RESOURCE_MOBILE_URI), eq(reqUser1),any())).thenReturn(Boolean.TRUE);
        when(securityManager.checkPolicyByResourceIri(eq(RESOURCE_MOBILE_URI), eq(reqUser2),any())).thenReturn(Boolean.TRUE);
    }


    private void searchAllForTwoUsers(int expectedForUser1, int expectedForUser2) {
        String query = new QueryGenerator().toString();
        ResultSet resultSet = executeQuery(query, reqUser1, securityEnabled);
        int size = countResultSetSizeByNextMethod(resultSet);
        assertEquals("Platform query should return " + expectedForUser1 + " but got " + size, expectedForUser1, size);

        resultSet = executeQuery(query, reqUser2, securityEnabled);
        size = countResultSetSizeByNextMethod(resultSet);
        assertEquals("Platform query should return " + expectedForUser2 + " but got " + size, expectedForUser2, size);
    }

    @Test
    public void queryForResName() throws Exception {
        initPolicies2();
//        setUpTwoResources();
        String query = new String("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#>\n" +
                "\n" +
                "SELECT ?resName WHERE {\n" +
                "\t?sensor cim:name ?resName .\n" +
                "}");

//        String query = new String("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#>\n" +
//                "\n" +
////                "SELECT ?s ?p ?o FROM <" + TripleStore.DEFAULT_GRAPH  + ">  WHERE {\n" +
//                "SELECT ?s ?p ?o  WHERE {\n" +
//
//                "\t?s ?p ?o .\n" +
//                "}");


        boolean foundPublic = false;
        boolean foundPrivate = false;
        ResultSet resultSet = executeQuery(query, reqUser2, securityEnabled);

//        //TODO
//        while( resultSet.hasNext() ) {
//            QuerySolution solution = resultSet.next();
//            String s = solution.get("s").toString();
//            String p = solution.get("p").toString();
//            String o = solution.get("o").toString();
//            System.out.println(s + " " + p + " " + o);
//        }
//        System.out.println("End");


        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String resName = solution.get("resName").toString();
            if (resName.equals(RESOURCE_MOBILE_LABEL)) {
                foundPublic = true;
            }
            if (resName.equals(RESOURCE_STATIONARY_LABEL)) {
                foundPrivate = true;
            }
        }
        assertTrue("Name of a public resource should be found", foundPublic);
        assertFalse("Name of a private resource should be not returned", foundPrivate);

        resultSet = executeQuery(query, reqUser1, securityEnabled);
        foundPublic = false;
        foundPrivate = false;
        while (resultSet.hasNext()) {
            QuerySolution solution = resultSet.next();
            String resName = solution.get("resName").toString();
            if (resName.equals(RESOURCE_MOBILE_LABEL)) {
                foundPublic = true;
            }
            if (resName.equals(RESOURCE_STATIONARY_LABEL)) {
                foundPrivate = true;
            }

        }
        assertTrue("Name of a public resource should be found", foundPublic);
        assertTrue("Name of a private resource should be returned", foundPrivate);
    }

    private ResultSet executeQuery(String query, SecurityRequest req, boolean useSecureModel) {
        long in = DateTimeUtils.currentTimeMillis();
//        ResultSet resultSet = triplestore.executeQueryOnGraph(query, TripleStore.UNION_GRAPH, req, useSecureModel);
        ResultSet resultSet = triplestore.executeQueryOnGraph(query, TripleStore.UNION_GRAPH, req, useSecureModel);
        long out = DateTimeUtils.currentTimeMillis();
        return resultSet;
    }

}
