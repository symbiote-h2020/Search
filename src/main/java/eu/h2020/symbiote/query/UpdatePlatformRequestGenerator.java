//package eu.h2020.symbiote.query;
//
//import eu.h2020.symbiote.model.mim.Platform;
//import org.apache.jena.update.UpdateFactory;
//import org.apache.jena.update.UpdateRequest;
//
///**
// * Generator of the update (delete+insert) operation for platforms.
// *
// * Created by Mael on 26/01/2017.
// * @deprecated update of platform is handled by delete+insert (same as resources)
// */
//@Deprecated
//public class UpdatePlatformRequestGenerator {
//
//    private UpdateRequest request;
//
//    /**
//     * Constructor of the delete operation. Prepares SPARQL Update statements to delete the platform with specified id
//     * and connected statements.
//     * To generate the request use {@link #generateRequest()}.
//     *
//     * @param platform Platform containing updated values.
//     */
//    public UpdatePlatformRequestGenerator(Platform platform ) {
//        request = UpdateFactory.create();
//        request.add(generatePlatformUpdate(platform));
//        request.add(generateInformationServiceUpdate(platform));
//    }
//
//    private StringBuilder generateBaseQuery() {
//        StringBuilder query = new StringBuilder();
//        query.append("PREFIX cim: <http://www.symbiote-h2020.eu/ontology/core#> \n");
//        query.append("PREFIX mim: <http://www.symbiote-h2020.eu/ontology/meta#> \n");
//        query.append("PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n");
//
//        return query;
//    }
//
//    private String generatePlatformUpdate( Platform platform ) {
//        //TODO
//        StringBuilder q = generateBaseQuery();
////        q.append("DELETE { ?platform mim:hasName ?platformName ; \n" );
////        q.append("\t\tmim:hasDescription ?platformDesc . }\n");
////        q.append("INSERT { ?platform mim:hasName \"" + platform.getName() +"\" ; \n" );
////        q.append("\t\tmim:hasDescription \"" + platform.getDescription() +"\" . }\n");
////        q.append("WHERE {\n");
////        q.append("\t?platform cim:id \""+platform.getId()+"\" ;\n");
////        q.append("\t\tmim:hasName ?platformName ;\n");
////        q.append("\t\tmim:hasDescription ?platformDesc .\n");
////        q.append("}");
//        return q.toString();
//    }
//
//    private String generateInformationServiceUpdate( Platform platform ) {
//        //TODO
//        StringBuilder q = generateBaseQuery();
////        q.append("DELETE { ?service mim:hasURL ?serviceURL ; \n" );
////        q.append("\t\tmim:hasInformationModel ?imodel . \n " );
////        q.append("\t?imodel cim:id ?imodelid .  }\n");
////        q.append("INSERT { ?service mim:hasURL \"" + platform.getUrl() +"\" ; \n" );
////        q.append("\t\tmim:hasInformationModel ?imodel . \n " );
////        q.append("\t?imodel cim:id \"" + platform.getInformationModelId() +"\" .  }\n");
////        q.append("WHERE {\n");
////        q.append("\t?platform mim:hasService ?service ;\n");
////        q.append("\t\tcim:id \""+platform.getPlatformId()+"\" .\n");
////        q.append("\t?service mim:hasURL ?serviceURL ; \n");
////        q.append("\t\tmim:hasInformationModel ?imodel . \n " );
////        q.append("\t?imodel cim:id ?imodelid . \n");
////        q.append("}");
//        return q.toString();
//    }
//
//    /**
//     * Generates the update request, containing delete queries for resource and data linked to the resource.
//     *
//     * @return Update request which allows deletion of the resource and linked information.
//     */
//    public UpdateRequest generateRequest() {
//        return request;
//    }
//
//}
