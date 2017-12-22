package org.xmlcml.norma.input.tei;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xmlcml.cproject.files.CTree;
import org.xmlcml.cproject.files.CTreeList;
import org.xmlcml.norma.Norma;
import org.xmlcml.norma.NormaFixtures;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by T Arrow on 22/12/17.
 * Designed to logically flow through PMR's suggestions for creating a new XSL based transformer
 */
public class TEI2TXTEndToEndTest {
    private static final Logger LOG = Logger.getLogger(TEI2TXTEndToEndTest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
    public void testCreateScholarlyHtmlFromNewStylesheet() throws IOException {
        File container0115884 = new File("target/plosone/0115884/");
        if (container0115884.exists()) FileUtils.forceDelete(container0115884);
        FileUtils.copyDirectory(NormaFixtures.F0115884_DIR, container0115884);
        String args = "-q "+container0115884.toString()+
                " --transform tom2html --input fulltext.xml --output scholarly.html --standalone true";
        Norma norma = new Norma();
        norma.run(args);
        CTreeList cTreeList = norma.getArgProcessor().getCTreeList();
        Assert.assertNotNull(cTreeList);
        Assert.assertEquals("CTree/s",  1,  cTreeList.size());
        CTree cTree = cTreeList.get(0);
        List<File> files = cTree.listFiles(true);
        LOG.trace(cTree+"; "+files);
        File htmlFile = new File(container0115884, "scholarly.html");
		Assert.assertTrue(""+htmlFile+" should exist", htmlFile.exists());
    }

   @Test
    public void testCreateTXTFromNewStylesheet() throws IOException {
        File container0115884 = new File("target/plosone/0115884/");
        if (container0115884.exists()) FileUtils.forceDelete(container0115884);
        FileUtils.copyDirectory(NormaFixtures.F0115884_DIR, container0115884);
        String args = "-q "+container0115884.toString()+
                " --transform tom2txt --input fulltext.xml --output fulltext.txt --standalone true";
        Norma norma = new Norma();
        norma.run(args);
        CTreeList cTreeList = norma.getArgProcessor().getCTreeList();
        Assert.assertNotNull(cTreeList);
        Assert.assertEquals("CTree/s",  1,  cTreeList.size());
        CTree cTree = cTreeList.get(0);
        List<File> files = cTree.listFiles(true);
        LOG.trace(cTree+"; "+files);
        File txtFile = new File(container0115884, "fulltext.txt");
		Assert.assertTrue(""+txtFile+" should exist", txtFile.exists());
    }

}
