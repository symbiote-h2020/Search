package eu.h2020.symbiote;

/**
 * Created by Mael on 11/01/2017.
 */

import eu.h2020.symbiote.handlers.HandlerUtils;
import eu.h2020.symbiote.model.mim.Platform;
import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;

import static eu.h2020.symbiote.TestSetupConfig.generatePlatformA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class PlatformRegistrationTest {


    @Test
    public void testReadingModelFromFile() {
        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource("/old_r2_models/platformA.ttl")));
            Model mFromFile = ModelFactory.createDefaultModel();
            mFromFile.read(modelToSave,null,"TURTLE");
            assertNotNull(mFromFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGeneratingModelFromPlatform() {
        Platform platform = generatePlatformA();

        Model model = HandlerUtils.generateModelFromPlatform(platform);
        assertNotNull(model);
        assertEquals("Created model should have " + 8l + " entries, but has " + model.size(), 8l, model.size() );

    }

    @Test
    public void testSavePlatformThroughSearchStorage() {
        Platform platform = generatePlatformA();
        Model graph = HandlerUtils.generateModelFromPlatform(platform);

        try {
            InputStream modelToSave = IOUtils.toInputStream( IOUtils.toString(this.getClass()
                    .getResource("/platformA.ttl")));
            Model modelFromFile = ModelFactory.createDefaultModel();
            modelFromFile.read(modelToSave,null,"TURTLE");

            assertEquals("Number of statements must be the same, expected " + modelFromFile.size()
                    + ", actual " + graph.size(), modelFromFile.size(), graph.size());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
