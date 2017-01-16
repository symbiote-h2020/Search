package eu.h2020.symbiote.ontology.model;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

/**
 * Contains list of predicates used by symbIoTe Metainformation Model
 *
 * Created by Mael on 11/01/2017.
 */
public class MetaInformationModel {

    public static final String MIM_PREFIX = "http://www.symbiote-h2020.eu/ontology/meta.owl#";

    public static final String RDF_PREFIX = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";

    public static final String OWL_PREFIX = "http://www.w3.org/2002/07/owl#";

    public static final Property RDF_TYPE;

    public static final Property OWL_ONTOLOGY;

    public static final Property MIM_HASDESCRIPTION;

    public static final Property MIM_HASNAME;

    public static final Property MIM_HASSERVICE;

    public static final Property MIM_HASINFORMATIONMODEL;

    public static final Property MIM_HASID;

    public static final Property MIM_HASURL;

//    String MIM_HASDESCRIPTION = MIM_PREFIX + "hasDescription";
//
//    String MIM_HASNAME = MIM_PREFIX + "hasName";
//    String  = MIM_PREFIX + "";
//    String  = MIM_PREFIX + "";
//    String  = MIM_PREFIX + "";
//    String  = MIM_PREFIX + "";
//
//
//
//
//
//    mim:hasDescription "Test platform"^^xsd:string ;
//    mim:hasName "Platform A"^^xsd:string ;
//    mim:hasService [
//    mim:hasInformationModel [
//    mim:ID "22222"^^rdfs:long
//        ] ;
//    mim:hasURL "http://somehost.com/resourceAccessProxy"^^xsd:string
//     ] .


    static {
        Model m = ModelFactory.createDefaultModel();
        RDF_TYPE = m.createProperty( RDF_PREFIX + "type" );
        OWL_ONTOLOGY = m.createProperty( OWL_PREFIX + "Ontology" );
        MIM_HASDESCRIPTION = m.createProperty( MIM_PREFIX + "hasDescription");
        MIM_HASNAME = m.createProperty( MIM_PREFIX + "hasName");
        MIM_HASSERVICE = m.createProperty( MIM_PREFIX + "hasService");
        MIM_HASINFORMATIONMODEL = m.createProperty( MIM_PREFIX + "hasInformationModel");
        MIM_HASID = m.createProperty( MIM_PREFIX + "hasID");
        MIM_HASURL = m.createProperty( MIM_PREFIX + "hasURL");
    }

}
