package eu.h2020.symbiote.model;

/**
 * Created by Mael on 23/01/2017.
 */
public class QueryRequest {

    private String sparql;

    private String graphUri;

    public QueryRequest() {
    }

    public QueryRequest(String sparql, String graphUri) {
        this.sparql = sparql;
        this.graphUri = graphUri;
    }

    public String getGraphUri() {
        return graphUri;
    }

    public void setGraphUri(String graphUri) {
        this.graphUri = graphUri;
    }

    public String getSparql() {
        return sparql;
    }

    public void setSparql(String sparql) {
        this.sparql = sparql;
    }
}
