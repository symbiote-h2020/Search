/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model;

import org.apache.jena.query.*;
import org.apache.jena.query.spatial.EntityDefinition;
import org.apache.jena.query.spatial.SpatialDatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.util.QueryExecUtils;
import org.apache.jena.tdb.TDBFactory;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Class representing a triplestore - connected to in-memory or disk (TDB) jena repository. Creates a spatial index
 * based on Lucene
 *
 * Contains useful methods to access datastore: inserting graphs (save), querying graphs (read).
 *
 * @author jab
 */
public class TripleStore {

    private final String currentDirectory;
    private final Dataset dataset;
    private static final String BASE_REPO = "base";
    private static final String SPATIAL_REPO = "spatial";


    //For tests only - in memory
    public TripleStore() {
        currentDirectory = null;

        EntityDefinition entDef = new EntityDefinition("entityField", "geoField");

//        File dir = new File("/spatialLucene");
//
//        deleteOldFiles(dir);
//        if( !dir.exists() ) {
//            dir.mkdirs();
//        }
        Dataset baseDataset = DatasetFactory.create();
//        Directory realDir = null;
//        try {
//            realDir = FSDirectory.open(dir);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Directory ramDir = new RAMDirectory();

        dataset = SpatialDatasetFactory.createLucene(baseDataset, ramDir, entDef);
    }



    public TripleStore( String directory ) {
        currentDirectory = directory;

        EntityDefinition entDef = new EntityDefinition("entityField", "geoField");

        File dir = new File( directory + (directory.endsWith("/")?"":"/") + SPATIAL_REPO );

        if( !dir.exists() ) {
            dir.mkdirs();
        }

        String baseRepoLocation = directory + (directory.endsWith("/") ? "" : "/") + BASE_REPO;
        File baseDir = new File( baseRepoLocation );
        baseDir.mkdirs();
        Dataset baseDataset= TDBFactory.createDataset(baseRepoLocation);
        Directory realDir = null;
        try {
            realDir = FSDirectory.open(dir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        dataset = SpatialDatasetFactory.createLucene(baseDataset, realDir, entDef);
    }

    private static void deleteOldFiles(File indexDir) {
        if (indexDir.exists())
            emptyAndDeleteDirectory(indexDir);
    }

    private static void emptyAndDeleteDirectory(File dir) {
        File[] contents = dir.listFiles() ;
        if (contents != null) {
            for (File content : contents) {
                if (content.isDirectory()) {
                    emptyAndDeleteDirectory(content) ;
                } else {
                    content.delete() ;
                }
            }
        }
        dir.delete() ;
    }

    public void executeQueryPP( String query ) {

        System.out.println("QUERY: ---------------");
        System.out.println(query);
        System.out.println("----------------------");

        Query q = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.create(q, dataset);
        QueryExecUtils.executeQuery(q, qexec);

    }


    public void insertGraph(String uri, String rdf, RDFFormat format) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(rdf.getBytes()), null, format.toString());
        insertGraph(uri, model);
    }

    public void insertGraph(String uri, Model model) {
//        dataset.begin(ReadWrite.WRITE);
//        if (!dataset.containsNamedModel(uri)) {
//            dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
//        }
//        dataset.getNamedModel(uri).add(model);
        dataset.begin(ReadWrite.WRITE);
        dataset.getDefaultModel().add(model);
        dataset.commit();
        dataset.end();
    }

    public ResultSet executeQuery(String queryString) {
        return executeQueryOnGraph(queryString, "urn:x-arq:UnionGraph");
    }

    public ResultSet executeQuery(Query query) {
        return executeQueryOnGraph(query, "urn:x-arq:UnionGraph");
    }

    public ResultSet executeQueryOnGraph(String queryString, String graphUri) {
        return executeQueryOnGraph(QueryFactory.create(queryString, Syntax.syntaxARQ), graphUri);
    }

    public ResultSet executeQueryOnGraph(Query query, String graphUri) {
        dataset.begin(ReadWrite.READ);
//        Model model = dataset.containsNamedModel(graphUri)
//                ? dataset.getNamedModel(graphUri)
//                : dataset.getDefaultModel();
//        Model model = dataset.getDefaultModel();
        QueryExecution qe = QueryExecutionFactory.create(query, dataset);
        ResultSet result = qe.execSelect();
        dataset.end();
        return result;
    }

    public Model getGraph(String graph) {
        dataset.begin(ReadWrite.READ);
        Model result = dataset.getNamedModel(graph);
        dataset.end();
        return result;
    }

    public String getGraphAsString(String graph, String syntax) {
        StringWriter out = new StringWriter();
        getGraph(graph).write(out, syntax);
        return out.toString();
    }

    public List<String> loadDataFromDataset() {
        List<String> data = new ArrayList<>();
        dataset.begin(ReadWrite.READ);
        Iterator<String> stringIterator = dataset.listNames();
        while (stringIterator.hasNext() ) {
            String s = stringIterator.next();
            System.out.println( "Loaded dataset: " + s);
            data.add(s);
        }
        dataset.end();
        return data;
    }


    public String getGraphAsString(String graph) {
        return getGraphAsString(graph, "RDF/XML-ABBREV");
    }
}
