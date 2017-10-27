/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model;

import eu.h2020.symbiote.core.internal.RDFFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.rdf.model.Model;

import java.math.BigInteger;

/**
 *
 * @author jab
 */
public class Registry {

    private static Log log = LogFactory.getLog( Registry.class );
    private final TripleStore tripleStore;

    public Registry(TripleStore tripleStore) {

        this.tripleStore = tripleStore;
        log.info("Creating Registry, loading stored data");
//        List<String> data = tripleStore.loadDataFromDataset();
        log.info("Data loaded!" );
    }

    public void registerPlatform(String platformId, String rdf, RDFFormat format) {
        tripleStore.insertGraph(Ontology.getPlatformGraphURI(platformId), rdf, format);
//        tripleStore.insertGraph(Ontology.PLATFORMS_GRAPH, Ontology.getPlatformMetadata(platformId, modelId), format);
        log.debug(String.format("platform registered: platformId={}, format={}, rdf={}", platformId, format, rdf));
    }

    public void registerPlatform(String platformId, Model rdf) {
        tripleStore.insertGraph(Ontology.getPlatformGraphURI(platformId), rdf);
//        tripleStore.insertGraph(Ontology.PLATFORMS_GRAPH, Ontology.getPlatformMetadata(platformId, modelId), RDFFormat.Turtle);
        log.debug(String.format("platform registered: platformId={}, rdf={}", platformId,rdf));
    }

    /**
     * Registers resource of the platform described by specified Uri. Model will be stored in the platform's named graph.
     *
     * @param platformUri Uri of the platform for which resource is added.
     * @param resourceModel Model describiding the resource.
     */
    public void registerResource(String platformUri, String serviceURI, String resourceUri, Model resourceModel) {
        tripleStore.insertGraph(platformUri, resourceModel);
        tripleStore.insertGraph(platformUri, Ontology.getResourceMetadata(serviceURI,resourceUri), RDFFormat.Turtle);
        log.debug(String.format("Resource={%s} registered for platform: platformUri={%s}", resourceUri, platformUri));
    }

//    public void registerMapping(BigInteger mappingId, BigInteger modelId1, BigInteger modelId2, String mapping) throws UnsupportedEncodingException {
//        // use library to parse mapping file to RDF, then read RDFXML into store
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")), true)) {
//            AlignmentParser parser = new AlignmentParser(0);
//            parser.initAlignment(null);
//            try {
//                parser.parseString(mapping).render(new RDFRendererVisitor(writer));
//            } catch (AlignmentException e) {
//                log.error("Couldn't load the alignment:", e);
//            }
//            writer.flush();
//        }
//        String mappingRDF = out.toString();
//        Model model = ModelFactory.createDefaultModel();
//        model.read(new ByteArrayInputStream(out.toByteArray()), null, RDFFormat.RDFXML.toString());
//
//        tripleStore.insertGraph(Ontology.getMappingGraphURI(mappingId), model, RDFFormat.RDFXML);
//        tripleStore.insertGraph(Ontology.MAPPING_GRAPH, Ontology.getMappingMetadata(modelId1, modelId2, mappingId), RDFFormat.NTriples);
//        log.debug(String.format("mapping registered: modelId1={}, modelId2={}, mapping={}", modelId1, modelId2, mappingRDF));
//    }

}
