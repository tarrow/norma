package org.xmlcml.norma.pubstyle.tf;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlcml.cmine.args.DefaultArgProcessor;
import org.xmlcml.cmine.util.CMineTestFixtures;
import org.xmlcml.norma.NormaArgProcessor;
import org.xmlcml.norma.NormaFixtures;

@Ignore // have lost tf
public class TFTest {
	private static final Logger LOG = Logger.getLogger(TFTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	static String PUB0 = "tf";
	static String PUB = "tf";
	static String PUB1 = PUB+"/clean";
	static File TARGET = new File(NormaFixtures.TARGET_PUBSTYLE_DIR, PUB);
	static File TARGET1 = new File(NormaFixtures.TARGET_PUBSTYLE_DIR, PUB1);
	static File TEST = new File(NormaFixtures.TEST_PUBSTYLE_DIR, PUB);
	static File TEST1 = new File(TEST, "test");

	@Test
	public void testHtml2Scholarly() {
		NormaFixtures.htmlTidy(TEST1, TARGET); 
	}

	@Test
	public void testHtml2Scholarly2StepConversion() {
		NormaFixtures.tidyTransform(TEST1, TARGET, PUB0);
	}
	
	@Test
	public void testHtml2Scholarly2StepConversionClean() throws IOException {
		NormaFixtures.tidyTransformAndClean(TEST1, TARGET1, PUB);
	}
	
	
	@Test
	public void convertToc() {
		File targetDir = new File("target/tutorial/tf");
		CMineTestFixtures.cleanAndCopyDir(new File("src/test/resources/org/xmlcml/norma/pubstyle/tf/toc/"), targetDir);
		String args = "--project "+targetDir+" -i fulltext.html -o fulltext.xhtml --html jsoup";
		DefaultArgProcessor argProcessor = new NormaArgProcessor(args); 
		argProcessor.runAndOutput(); 
		args = "--project "+targetDir+" -i fulltext.xhtml -o scholarly.html --transform tf2html";
		argProcessor = new NormaArgProcessor(args); 
		argProcessor.runAndOutput(); 
			
	}
	
}