package com.ats.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import com.ats.generator.events.ScriptProcessedEvent;
import com.ats.generator.events.ScriptProcessedNotifier;
import com.ats.generator.parsers.Lexer;
import com.ats.script.ProjectData;
import com.ats.script.ScriptLoader;

public class Generator implements ScriptProcessedEvent{

	private GeneratorReport genReport;
	private Lexer lexer;
	private ArrayList<File> filesList;

	private ProjectData projectData;

	private int remainingScripts = 0;
	
	private String charset = ScriptLoader.DEFAULT_CHARSET;

	public static void main(String[] args) {

		log(Level.INFO, "Java version : " + System.getProperty("java.version"));
		log(Level.INFO, StringUtils.repeat("-", 72));
		
		ATS arguments = new ATS(args);
		arguments.parse();

		ProjectData projectData = ProjectData.getProjectData(arguments.getSourceFolder(), arguments.getDestinationFolder(), arguments.getReportFolder());

		Generator generator = new Generator(projectData);
		GeneratorReport report = generator.launch();
		
		log(Level.INFO, StringUtils.repeat("-", 72));
		log(Level.INFO, "ATS Generator finished :");
		log(Level.INFO, "- Java files generated -> " + report.getGeneratedScriptsCount());
		log(Level.INFO, "- Ellapsed time -> " + report.getGenerationEllapsedTime() + " ms");
		log(Level.INFO, StringUtils.repeat("-", 72));
	}
	
	public static void log(Level lvl, String mess) {
		if(lvl.equals(Level.SEVERE)) {
			System.err.println(mess);
		}else {
			System.out.println("[INFO] " + mess);
		}
	}

	public Generator(File atsFile){
		this(new ProjectData(atsFile));
	}

	public Generator(ProjectData project){

		projectData = project;
		projectData.initFolders();
		
		genReport = new GeneratorReport();

		File atsSourceFolder = projectData.getAtsSourceFolder().toFile();

		if(atsSourceFolder.exists()){

			filesList = new ArrayList<File>();

			if(atsSourceFolder.isDirectory()){
				try {
					Files.find(atsSourceFolder.toPath(), 99999, (p, bfa) -> bfa.isRegularFile()).forEach(p -> addAtsFile(p.toFile()));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}else if(atsSourceFolder.isFile()){
				addAtsFile(atsSourceFolder);
			}

			remainingScripts = filesList.size();

			if(remainingScripts > 0){

				projectData.initFolders();
				genReport.startGenerator(remainingScripts);

				lexer = new Lexer(projectData, genReport, charset);

			}else{
				log(Level.INFO, "Nothing to be done (no ATS files found !)");
			}
		}
	}

	private void addAtsFile(File f) {
		if(f.getName().toLowerCase().endsWith(ScriptLoader.ATS_FILE_EXTENSION) && f.getName().length() > ScriptLoader.ATS_FILE_EXTENSION.length() + 1) {
			filesList.add(f);
		}
	}

	public GeneratorReport launch(){
		
		Stream<File> stream = filesList.parallelStream();
		stream.forEach(f -> loadScript(f));
		stream.close();

		genReport.endGenerator();

		filesList.clear();
		lexer = null;

		//TODO copy assets and java files ....
		
		return genReport;
	}

	private void loadScript(File f){
		ScriptLoader sc = lexer.loadScript(f, new ScriptProcessedNotifier(this));
		sc.generateJavaFile();
	}

	@Override
	public void scriptProcessed() {
		remainingScripts--;

		//int percent = (int)(10000-(double)remainingScripts/(double)totalScript*10000)/100;
		//log.info("Generator in progress : " + percent + " % done");
	}
}