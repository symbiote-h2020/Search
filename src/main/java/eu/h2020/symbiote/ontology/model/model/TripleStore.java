/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model.model;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.tdb.TDBFactory;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author jab
 */
public class TripleStore {

    private final String currentDirectory;
    private final Dataset dataset;

    //For tests only - in memory
    public TripleStore() {
        currentDirectory = null;
        dataset = TDBFactory.createDataset();
    }

    public TripleStore( String directory ) {
        currentDirectory = directory;
        dataset = TDBFactory.createDataset(currentDirectory);
    }


    public void insertGraph(String uri, String rdf, RDFFormat format) {
        Model model = ModelFactory.createDefaultModel();
        model.read(new ByteArrayInputStream(rdf.getBytes()), null, format.toString());
        insertGraph(uri, model, format);
    }

    public void insertGraph(String uri, Model model, RDFFormat format) {
        dataset.begin(ReadWrite.WRITE);
        if (!dataset.containsNamedModel(uri)) {
            dataset.addNamedModel(uri, ModelFactory.createDefaultModel());
        }
        dataset.getNamedModel(uri).add(model);
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
        Model model = dataset.containsNamedModel(graphUri)
                ? dataset.getNamedModel(graphUri)
                : dataset.getDefaultModel();
        QueryExecution qe = QueryExecutionFactory.create(query, model);
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
