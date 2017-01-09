/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model.model;

/**
 *
 * @author jab
 */
public class SearchEngine {

    // To be restored when implementing search
//    private static final Logger LOGGER = LoggerFactory.getLogger(SearchEngine.class);
//    private final TripleStore tripleStore;
//
//    public SearchEngine(TripleStore tripleStore) {
//        this.tripleStore = tripleStore;
//    }
//
//    public ResultSet search(String modelGraphUri, String query) {
//        List<ResultSet> partialResults = new ArrayList<>();
//        // 1. execute query normally
//        partialResults.add(tripleStore.executeQuery(query));
//
//        // 2. execute against all mappings
//        ResultSet mappedModels = tripleStore.executeQueryOnGraph(generateFindMappingsQuery(modelGraphUri), Ontology.MAPPING_GRAPH);
//        while (mappedModels.hasNext()) {
//            QuerySolution solution = mappedModels.next();
//            String mappingGraph = solution.get("?mapping").asNode().toString();
//            String destinationModelUri = solution.get("?mapDest").asNode().toString();
//            String mappingDefinition = tripleStore.getGraphAsString(mappingGraph);
//            // for each do query rewriting and the execute to store
//            Query translatedQuery = translateQuery(query, mappingDefinition);
//            // find all platforms that use model that is mapped to and query them all
//            ResultSet platformsForModel = tripleStore.executeQueryOnGraph(generateFindPlatformsForModelQuery(destinationModelUri), Ontology.PLATFORMS_GRAPH);
//            while (platformsForModel.hasNext()) {
//                QuerySolution platformSolution = platformsForModel.next();
//                String platformGraph = platformSolution.get("?platform").asNode().toString();
//                ResultSet partialResult = tripleStore.executeQueryOnGraph(translatedQuery, platformGraph);
//                partialResults.add(partialResult);
//            }
//        }
//        return new ResultSetMem(partialResults.toArray(new ResultSet[partialResults.size()]));
//    }
//
//    private Query translateQuery(String queryString, String mapping) {
//        AlignmentParser parser = new AlignmentParser(0);
//        parser.initAlignment(null);
//        Alignment alignment = null;
//        try {
//            alignment = EDOALMediator.mediate(parser.parseString(mapping));
//        } catch (AlignmentException e) {
//            LOGGER.error("Couldn't load the alignment:", e);
//        }
//        Query query = QueryFactory.create(queryString, Syntax.syntaxARQ);
//        Op op = Algebra.compile(query);
//        EntityTranslationService ets = new EntityTranslationServiceImpl();
//        Transform translation = new EntityTranslation(ets, alignment);
//        Op translated = Transformer.transform(translation, op);
//        Query queryTranslated = ExtendedOpAsQuery.asQuery(translated);
//        return queryTranslated;
//    }
//
//    private String generateFindMappingsQuery(String modelGraphUri) {
//        ParameterizedSparqlString query = new ParameterizedSparqlString();
//        query.setCommandText(String.join("\n",
//                "SELECT ?mapping ?mapDest ",
//                "WHERE {",
//                "	?mapping a ?mappingClass .",
//                "       ?mapping ?from ?modelGraphURI .",
//                "	?mapping ?to ?mapDest .",
//                "} "));
//        query.setIri("mappingClass", Ontology.MAPPING);
//        query.setIri("from", Ontology.FROM);
//        query.setIri("to", Ontology.TO);
//        query.setIri("modelGraphURI", modelGraphUri);
//        return query.toString();
//    }
//
//    private String generateFindPlatformsForModelQuery(String modelGraphURI) {
//        ParameterizedSparqlString query = new ParameterizedSparqlString();
//        query.setCommandText(String.join("\n",
//                "SELECT ?platform ",
//                "WHERE {",
//                "	?platform a ?platformClass .",
//                "	?platform ?uses ?modelGraphURI .",
//                "} "));
//        query.setIri("platformClass", Ontology.PLATFORM);
//        query.setIri("uses", Ontology.USES);
//        query.setIri("modelGraphURI", modelGraphURI);
//        return query.toString();
//    }
}
