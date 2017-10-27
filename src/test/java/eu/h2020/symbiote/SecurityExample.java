package eu.h2020.symbiote;

/**
 * Created by Szymon Mueller on 16/10/2017.
 */
import java.net.URL;

import eu.h2020.symbiote.filtering.FilteringEvaluator;
import eu.h2020.symbiote.filtering.FilteringEvaluator2;
import org.apache.jena.permissions.Factory;

import org.apache.jena.permissions.example.ExampleEvaluator;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.vocabulary.RDF;

public class SecurityExample {

    /**
     * @param args
     */

    public static void main(String[] args) {
        String[] names = { "alice", "bob", "chuck", "darla" };

        RDFNode msgType = ResourceFactory
                .createResource("http://example.com/msg");
        Property pTo = ResourceFactory.createProperty("http://example.com/to");
        Property pFrom = ResourceFactory
                .createProperty("http://example.com/from");
        Property pSubj = ResourceFactory
                .createProperty("http://example.com/subj");

        Model model = ModelFactory.createDefaultModel();
        URL url = SecurityExample.class.getClassLoader().getResource(
                "example.ttl");
        model.read(url.toExternalForm());
        ResIterator ri = model.listSubjectsWithProperty(RDF.type, msgType);
        System.out.println("All the messages");
        while (ri.hasNext()) {
            Resource msg = ri.next();
            Statement to = msg.getProperty(pTo);
            Statement from = msg.getProperty(pFrom);
            Statement subj = msg.getProperty(pSubj);
            System.out.println(String.format("%s to: %s  from: %s  subj: %s",
                    msg, to.getObject(), from.getObject(), subj.getObject()));
        }
        System.out.println();

        FilteringEvaluator2 evaluator = new FilteringEvaluator2(model);
        model = Factory.getInstance(evaluator,
                "http://example.com/SecuredModel", model);
        for (String userName : names) {
            evaluator.setPrincipal(userName);

            System.out.println("Messages " + userName + " can manipulate");
            ri = model.listSubjectsWithProperty(RDF.type, msgType);
            while (ri.hasNext()) {
                Resource msg = ri.next();
                Statement to = msg.getProperty(pTo);
                Statement from = msg.getProperty(pFrom);
                Statement subj = msg.getProperty(pSubj);
                System.out.println(String.format(
                        "%s to: %s  from: %s  subj: %s", msg, to.getObject(),
                        from.getObject(), subj.getObject()));
            }
            ri.close();
            for (String name : names)
            {
                System.out.println( String.format( "%s messages to %s", model.listSubjectsWithProperty( pTo, name ).toList().size(), name ) );
            }
            System.out.println();
        }
    }

}
