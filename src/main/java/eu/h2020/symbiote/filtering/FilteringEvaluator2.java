//package eu.h2020.symbiote.filtering;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.apache.http.auth.BasicUserPrincipal;
//import org.apache.jena.graph.Node;
//import org.apache.jena.graph.Triple;
//import org.apache.jena.permissions.SecurityEvaluator;
//import org.apache.jena.rdf.model.*;
//import org.apache.jena.shared.AuthenticationRequiredException;
//import org.apache.jena.vocabulary.RDF;
//
//import java.security.Principal;
//import java.util.Set;
//
///**
// * Created by Szymon Mueller on 16/10/2017.
// */
//public class FilteringEvaluator2 implements SecurityEvaluator {
//
//    private Log log = LogFactory.getLog(FilteringEvaluator2.class);
//
//    private Principal principal;
//    private Model model;
//    private RDFNode msgType = ResourceFactory.createResource("http://example.com/msg");
//    private Property pTo = ResourceFactory.createProperty("http://example.com/to");
//    private Property pFrom = ResourceFactory.createProperty("http://example.com/from");
//
//    /**
//     * @param model The graph we are going to evaluate against.
//     */
//    public FilteringEvaluator2(Model model) {
//        log.debug("EVALUATOR - > constructor");
//        this.model = model;
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Action action, Node graphIRI) {
//        // we allow any action on a graph.
//        log.debug("EVALUATOR - > accepting any action on graph" );
//        return true;
//    }
//
//    // not that in this implementation all permission checks flow through
//    // this method. We can do this because we have a simple permissions
//    // requirement. A more complex set of permissions requirement would
//    // require a different strategy.
//    private boolean evaluate(Object principalObj, Resource r) {
//        log.debug("EVALUATOR - > evaluate(principalObj,resource): principalObj: " + principalObj.toString() + "  ||xxxxxx||  resource: " + r.toString()  );
//        Principal principal = (Principal) principalObj;
//        // we do not allow anonymous (un-authenticated) reads of data.
//        // Another strategy would be to only require authentication if the
//        // data being requested was restricted -- but that is a more complex
//        // process and not suitable for this simple example.
//        if (principal == null) {
//            throw new AuthenticationRequiredException();
//        }
//
//        // a message is only available to sender or recipient
//        if (r.hasProperty(RDF.type, msgType)) {
//            return r.hasProperty(pTo, principal.getName()) ||
//                    r.hasProperty(pFrom, principal.getName());
//        }
//        return true;
//    }
//
//    // evaluate a node.
//    private boolean evaluate(Object principal, Node node) {
//        log.debug("EVALUATOR - > evaluate(principalObj,node): principalObj: " + principal.toString() + "  ||xxxxxx||  node: " + node.toString()  );
//        if (node.equals(Node.ANY)) {
//            // all wildcards are false. This forces each triple
//            // to be explicitly checked.
//            return false;
//        }
//
//        // if the node is a URI or a blank node evaluate it as a resource.
//        if (node.isURI() || node.isBlank()) {
//            Resource r = model.getRDFNode(node).asResource();
//            return evaluate(principal, r);
//        }
//
//        return true;
//    }
//
//    // evaluate the triple by evaluating the subject, predicate and object.
//    private boolean evaluate(Object principal, Triple triple) {
//        log.debug("EVALUATOR - > evaluate(principalObj,trips): principalObj: " + principal.toString() + "  ||xxxxxx||  trips: " + triple.toString()  );
//        return evaluate(principal, triple.getSubject()) &&
//                evaluate(principal, triple.getObject()) &&
//                evaluate(principal, triple.getPredicate());
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Action action, Node graphIRI, Triple triple) {
//        log.debug("EVALUATOR - > (gonna nest) evaluate(principal,action,node,triple): principal: " + principal.toString() + "  ||xxxxxx||  action: " + action.toString() + "  ||xxxxxx||  node(graphIRI): " + graphIRI.toString() + "  ||xxxxxx||  triple: " + triple.toString() );
//        return evaluate(principal, triple);
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI) {
//        log.debug("EVALUATOR - > evaluate(principalObj,actions,graphIRI): principalObj: " + principal.toString()  );
//        return true;
//    }
//
//    @Override
//    public boolean evaluate(Object principal, Set<Action> actions, Node graphIRI,
//                            Triple triple) {
//        log.debug("EVALUATOR - > evaluate(principalObj,actions,graphIRI,triple): principalObj: " + principal.toString()  );
//        return evaluate(principal, triple);
//    }
//
//    @Override
//    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI) {
//        log.debug("EVALUATOR - > evaluateAny(principalObj,actions,graphIRI): principalObj: " + principal.toString()  );
//        return true;
//    }
//
//    @Override
//    public boolean evaluateAny(Object principal, Set<Action> actions, Node graphIRI,
//                               Triple triple) {
//        log.debug("EVALUATOR - > evaluateAny(principalObj,actions,graphIRI,triple): principalObj: " + principal.toString()  );
//        return evaluate(principal, triple);
//    }
//
//    @Override
//    public boolean evaluateUpdate(Object principal, Node graphIRI, Triple from, Triple to) {
//        log.debug("EVALUATOR - > evaluateUpdate(principalObj,graphIRI,from,to): principalObj: " + principal.toString()  );
//        return evaluate(principal, from) && evaluate(principal, to);
//    }
//
//    public void setPrincipal(String userName) {
//        log.debug("EVALUATOR - > setPrincipal(userName): " + userName  );
//        if (userName == null) {
//            principal = null;
//        }
//        principal = new BasicUserPrincipal(userName);
//    }
//
//    @Override
//    public Principal getPrincipal() {
//        return principal;
//    }
//
//    @Override
//    public boolean isPrincipalAuthenticated(Object principal) {
//        return principal != null;
//    }
//
//
//}
