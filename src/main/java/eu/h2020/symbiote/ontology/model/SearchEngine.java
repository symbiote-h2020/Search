/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model;

import org.apache.jena.query.Query;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jab
 */
public class SearchEngine {

    // To be restored when implementing search
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchEngine.class);
    private final TripleStore tripleStore;

    public SearchEngine(TripleStore tripleStore) {
        this.tripleStore = tripleStore;
    }

    public ResultSet search(String modelGraphUri, String query) {
        List<ResultSet> partialResults = new ArrayList<>();
        partialResults.add(tripleStore.executeQuery(query, null, false));
        return new ResultSetMem(partialResults.toArray(new ResultSet[partialResults.size()]));
    }

//    public ResultSet search(Query query) {
//        ResultSet resultSet = tripleStore.executeQuery(query, null, false);
//        return resultSet;
//    }

}
