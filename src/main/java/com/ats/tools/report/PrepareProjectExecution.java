package com.ats.tools.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ats.driver.AtsManager;
import com.ats.tools.Utils;

public class PrepareProjectExecution {

	public static void main(String[] args) throws URISyntaxException {

		String targetFolder = "target";
		if(args.length > 0) {
			targetFolder = args[0];
		}

		final Path pomFilePath = Paths.get("pom.xml");

		if(pomFilePath.toAbsolutePath().toFile().exists()) {
			try {
				final String content = new String(Files.readAllBytes(pomFilePath), StandardCharsets.UTF_8);

				final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				final Document doc = builder.parse(new InputSource(new StringReader(content)));

				final NodeList suites = doc.getElementsByTagName("suiteXmlFile");

				final Path targetPath = Paths.get(targetFolder);
				final File targetFile = targetPath.toFile();
				if(targetFile.exists()) {
					try {
						Utils.deleteRecursiveFiles(targetFile);
					}catch(Exception e) {}
					targetFile.mkdir();
				}

				final File suiteFile = targetPath.resolve("suites.xml").toFile();

				suiteFile.getParentFile().mkdirs();
				suiteFile.createNewFile();

				FileWriter fw = new FileWriter(suiteFile);
				fw.write("<!DOCTYPE suite SYSTEM \"https://testng.org/testng-1.0.dtd\">");
				fw.write("<suite name=\"allSuites\">");
				fw.write("<suite-files>");

				for (int i=0; i<suites.getLength(); i++) {
					fw.write("<suite-file path=\"" + (suites.item(i).getTextContent()) + "\"/>");
				}

				fw.write("</suite-files>");
				fw.write("</suite>");

				fw.close();

				final File buildFile = targetPath.resolve("build.xml").toFile();
				fw = new FileWriter(buildFile);
				fw.write("<project basedir=\".\" default=\"compile\">");
				fw.write("<copy todir=\"classes\"> ");
				fw.write("<fileset dir=\"..\\src\" includes='assets/**'/>");
				fw.write("</copy>");
				fw.write("<property name=\"lib.dir\" value=\"lib\"/>");
				fw.write("<path id=\"classpath\">");
				fw.write("<fileset dir=\"" + AtsManager.getAtsHomeFolder() + "\\libs\" includes=\"**/*.jar\"/>");
				fw.write("</path>");
				fw.write("<target name=\"compile\">");
				fw.write("<mkdir dir=\"classes\"/>");
				fw.write("<javac srcdir=\"generated\" destdir=\"classes\" classpathref=\"classpath\"/>");
				fw.write("</target>");
				fw.write("</project>");

				fw.close();

			} catch (IOException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
	}
}
