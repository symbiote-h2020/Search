/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.h2020.symbiote.ontology.model;

import eu.h2020.symbiote.core.internal.RDFFormat;
import eu.h2020.symbiote.model.mim.InformationModel;
import eu.h2020.symbiote.semantics.ModelHelper;
import eu.h2020.symbiote.semantics.ontology.MIM;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;

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
        tripleStore.insertGraph(//ModelHelper.getPlatformURI(platformId),
                TripleStore.DEFAULT_GRAPH,
                rdf, format);
//        tripleStore.insertGraph(Ontology.PLATFORMS_GRAPH, Ontology.getPlatformMetadata(platformId, modelId), format);
        log.debug(String.format("platform registered: platformId={}, format={}, rdf={}", platformId, format, rdf));
    }

    public void registerPlatform(String platformId, Model rdf) {
        tripleStore.insertGraph(//ModelHelper.getPlatformURI(platformId),
                TripleStore.DEFAULT_GRAPH,
                rdf);
//        tripleStore.insertGraph(Ontology.PLATFORMS_GRAPH, Ontology.getPlatformMetadata(platformId, modelId), RDFFormat.Turtle);
        log.debug(String.format("platform registered: platformId={}, rdf={}", platformId,rdf));
    }

    public void registerSsp(String sspId, Model rdf) {
        tripleStore.insertGraph(//ModelHelper.getSspURI(sspId)
                TripleStore.DEFAULT_GRAPH,
                rdf);
//        tripleStore.insertGraph(Ontology.PLATFORMS_GRAPH, Ontology.getPlatformMetadata(platformId, modelId), RDFFormat.Turtle);
        log.debug(String.format("ssp registered: sspId={}, rdf={}",sspId,rdf));
    }

    public void registerSdev(String sdevId, Model rdf) {
        tripleStore.insertGraph(//ModelHelper.getSdevURI(sdevId),
                TripleStore.DEFAULT_GRAPH,
                rdf);
//        tripleStore.insertGraph(Ontology.PLATFORMS_GRAPH, Ontology.getPlatformMetadata(platformId, modelId), RDFFormat.Turtle);
        log.debug(String.format("sdev registered: sspId={}, rdf={}",sdevId,rdf));
    }

    /**
     * Registers resource of the platform described by specified Uri. Model will be stored in the platform's named graph.
     *
     * @param platformUri Uri of the platform for which resource is added.
     * @param resourceModel Model describing the resource.
     */
    public void registerResource(String platformUri, String serviceURI, String resourceUri, Model resourceModel) {
        log.debug("Inserting info to default graph");
        tripleStore.insertGraph(TripleStore.DEFAULT_GRAPH, resourceModel);

        tripleStore.insertGraph(TripleStore.DEFAULT_GRAPH, getResourceMetadata(serviceURI,resourceUri), RDFFormat.Turtle);
        log.debug(String.format("Resource={%s} registered for platform: platformUri={%s}", resourceUri, platformUri));
    }

    private void printModel(Model resourceModel) {
        log.debug("Printing model to be added..");
        log.debug("========================================================");
        StmtIterator stmtIterator = resourceModel.listStatements();
        while( stmtIterator.hasNext() ) {
            Statement next = stmtIterator.next();
            log.debug( " " + next.getSubject().toString() + "  |  " + next.getPredicate().toString() + "  |  " + next.getObject().toString() );
        }
        log.debug("========================================================");
    }

    public void registerSdevResourceLinkToInterworkingService( String sdevServiceURI, String resourceUri ) {
        tripleStore.insertGraph(TripleStore.DEFAULT_GRAPH, getResourceMetadata(sdevServiceURI,resourceUri), RDFFormat.Turtle);
    }

//    /**
//     * Registers ssp resource of the sdev. Model will be stored in the platform's named graph.
//     *
//     * @param sdevUri Uri of the platform for which resource is added.
//     * @param resourceModel Model describiding the resource.
//     */
//    public void registerSspResource(String sdevUri, String resourceUri, Model resourceModel) {
//        tripleStore.insertGraph("",resourceModel);
//        tripleStore.insertGraph("",getSspResourceMetadata(sdevUri,resourceUri), RDFFormat.Turtle);
//        log.debug(String.format("Resource={%s} registered for platform: sdevUri={%s}", resourceUri, sdevUri));
//    }

    private String getResourceMetadata( String serviceURI, String resourceUri ) {
        return "<" + serviceURI + "> <" + MIM.hasResource + "> <" + resourceUri + "> .";
    }

//    private String getSspResourceMetadata( String sdevURI, String resourceUri ) {
//        return "<" + serviceURI + "> <" + MIM.hasResource + "> <" +resourceUri + "> .";
//    }

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
