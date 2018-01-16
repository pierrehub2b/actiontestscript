package com.ats.generator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.ats.generator.events.ScriptProcessedEvent;
import com.ats.generator.events.ScriptProcessedNotifier;
import com.ats.generator.parsers.Lexer;
import com.ats.script.ProjectData;
import com.ats.script.ScriptLoader;

public class Generator implements ScriptProcessedEvent{

	private static final Logger log = Logger.getLogger("ATS-Generator");

	private GeneratorReport genReport;
	private Lexer lexer;
	private ArrayList<File> filesList;

	private ProjectData projectData;

	private int remainingScripts = 0;
	
	private String charset = ScriptLoader.DEFAULT_CHARSET;

	public static void main(String[] args) {

		ATS arguments = new ATS(args);
		arguments.parse();

		ProjectData projectData = ProjectData.getProjectData(arguments.getSourceFolder(), arguments.getDestinationFolder(), arguments.getReportFolder());

		Generator generator = new Generator(projectData);
		GeneratorReport report = generator.launch();

		log.info(report.getGeneratedScriptsCount() + " java files generated in " + report.getGenerationEllapsedTime() + " ms");
	}

	public Generator(File atsFile){
		this(new ProjectData(atsFile));
	}

	public Generator(ProjectData project){

		projectData = project;
		projectData.initFolders();
		
		genReport = new GeneratorReport();

		if(projectData.getAtsRootFolder().exists()){

			filesList = new ArrayList<File>();

			if(projectData.getAtsRootFolder().isDirectory()){
				try {
					Files.find(projectData.getAtsRootFolder().toPath(), 99999, (p, bfa) -> bfa.isRegularFile()).forEach(p -> addAtsFile(p.toFile()));
				} catch (IOException e) {
					e.printStackTrace();
				}

			}else if(projectData.getAtsRootFolder().isFile()){
				addAtsFile(projectData.getAtsRootFolder());
			}

			remainingScripts = filesList.size();
			
			if(remainingScripts > 0){

				projectData.initFolders();
				genReport.startGenerator(remainingScripts);

				lexer = new Lexer(projectData, genReport, charset);

			}else{
				log.info("Nothing to be done (no ATS files found !)");
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