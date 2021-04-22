package com.ats.script;

import com.ats.generator.GeneratorReport;
import com.ats.generator.parsers.Lexer;
import com.ats.script.actions.Action;
import com.google.gson.Gson;
import org.ahocorasick.trie.Emit;
import org.ahocorasick.trie.Trie;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class ScriptFinder {
	
	private static final String ACTIONS_FILTER = "actions";
	private static final String DESCRIPTION_FILTER = "description";
	private static final String AUTHOR_FILTER = "author";
	private static final String PREREQUISITE_FILTER = "prerequisite";
	private static final String GROUPS_FILTER = "groups";
	private static final String CREATED_FILTER = "created";
	private static final String ID_FILTER = "id";
	private static final String SQUASH_ID_FILTER = "squashId";
	private static final String JIRA_ID_FILTER = "jiraId";
	
	/*
	private String path = "";
	private String projectPath = "";
	private String packageName = "";
	private String name = "";
	*/
	
	public static void main(String[] args) {
		final String projectFolderPath = args[0];
		final String[] keywords = args[1].split("\\+");
		final String[] filters = args[2].split("\\+");;
		
		final File projectFile = new File(projectFolderPath);
		final Project project = new Project(projectFile);
		final Lexer lexer = new Lexer(project, new GeneratorReport(), StandardCharsets.UTF_8);
		
		ArrayList<String> results = new ArrayList<>();
		
		Trie trie = Trie.builder().addKeywords(keywords).build();
		
		final ArrayList<File> filesList = project.getAtsScripts();
		final Stream<File> stream = filesList.parallelStream();
		stream.forEach(f -> searchInFile(f, trie, lexer, results, filters) );
		stream.close();
		
		System.out.print(new Gson().toJson(results.toArray()));
	}
	
	private static void searchInFile(File file, Trie trie, Lexer lexer, ArrayList<String> results, String[] filters) {
		ScriptLoader script = lexer.loadScript(file);
		
		ArrayList<String> values = new ArrayList<>();
		for (String filter:filters) {
			switch (filter) {
				case ACTIONS_FILTER:
					values.addAll(getActionsKeywords(script.getActions()));
					break;
				case DESCRIPTION_FILTER:
					values.add(script.getHeader().getDescription());
					break;
				case AUTHOR_FILTER:
					values.add(script.getHeader().getAuthor());
					break;
				case PREREQUISITE_FILTER:
					values.add(script.getHeader().getPrerequisite());
					break;
				case GROUPS_FILTER:
					values.addAll(script.getHeader().getGroups());
					break;
				case CREATED_FILTER:
					values.add(script.getHeader().getCreatedAt().toString());
					break;
				case ID_FILTER:
					values.add(script.getHeader().getId());
					break;
				case SQUASH_ID_FILTER:
					values.add(script.getHeader().getSquashId());
					break;
				case JIRA_ID_FILTER:
					values.add(script.getHeader().getJiraId());
					break;
			}
		}
		
		Collection<Emit> emits = trie.parseText(String.join(" ", values));
		if (!emits.isEmpty()) {
			results.add(file.getPath());
			// 	results.add(new ScriptInfo(lexer, file));
		}
	}
	
	private static ArrayList<String> getActionsKeywords(Action[] actions) {
		ArrayList<String> values = new ArrayList<>();
		for (Action action:actions) {
		
		}
		
		return values;
	}
}
