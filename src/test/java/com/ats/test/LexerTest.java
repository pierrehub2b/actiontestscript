package com.ats.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.ats.script.Script;
import com.ats.script.actions.neoload.ActionNeoloadContainer;
import com.ats.script.actions.neoload.ActionNeoloadRecord;
import com.ats.script.actions.neoload.ActionNeoloadStart;
import com.ats.script.actions.neoload.ActionNeoloadStop;
import com.ats.generator.objects.mouse.Mouse;
import com.ats.generator.variables.CalculatedValue;
import com.ats.generator.variables.Variable;
import com.ats.script.actions.*;
import com.ats.tools.Utils;
import org.junit.Test;

import com.ats.generator.GeneratorReport;
import com.ats.generator.parsers.*;
import com.ats.script.ScriptLoader;

import static org.junit.Assert.assertEquals;

public class LexerTest {	

	@Test
	public void createActionTest() throws IOException {
		Lexer lexer = new Lexer(new GeneratorReport());
		ScriptLoader script = new ScriptLoader(lexer);
		
		String[] data = new String[] {
				"channel-start -> myFirstChannel -> chrome",
				"comment -> step -> <i>comment ...</i>",
				"comment-step -> <i>comment ...</i>",
				"comment -> <i>comment ...</i>",
				"comment -> script -> <i>comment ...</i>",
				"comment-script -> <i>comment ...</i>",
				"goto-url -> google.com",
				"keyboard -> automated testing$key(ENTER) -> INPUT [id = lst-ib]",
				"click -> A [text = Test automation - Wikipedia]",
				"scroll -> 300",
				"scroll -> 0 -> A [text = Graphical user interface testing]",
				"over -> IMG [src =~ .*Test_Automation_Interface.png]",
				"channel-close -> myFirstChannel",
				"channel-switch -> newChannel",
				"subscript -> ScriptATS",
				"check-property -> id = test",
				"check-count -> 1 -> INPUT [name = q]",
				"check-value -> test = azerty",
				"property -> text => propertyVar -> INPUT [name = q]",
				"window-resize -> x = 0, y = 0, width = 0, height = 0",
				"window-state -> restore",
				"window-switch [try = 2] -> 1",
				"select -> index = 0 -> div",
				"scripting -> alert(\"test\")",
				"neoload-start",
				"neoload-stop",
				"neoload-container -> myContainer",
				"neoload-record -> pause"
			};
		
		ScriptLoader expectedScript = new ScriptLoader(lexer);

		for (int i = 0; i <= 1; i++) {
			expectedScript.addAction(new ActionChannelStart(expectedScript, "myFirstChannel", new ArrayList<>(), new CalculatedValue(script, "chrome"), new ArrayList<>()), i == 0);
			expectedScript.addAction(new ActionComment(expectedScript, "comment", new ArrayList<String>(Arrays.asList("step", "<i>comment ...</i>"))), i == 0);
			expectedScript.addAction(new ActionComment(expectedScript, "comment-step", new ArrayList<String>(Arrays.asList("<i>comment ...</i>"))), i == 0);
			expectedScript.addAction(new ActionComment(expectedScript, "comment", new ArrayList<String>(Arrays.asList("<i>comment ...</i>"))), i == 0);
			expectedScript.addAction(new ActionComment(expectedScript, "comment", new ArrayList<String>(Arrays.asList("script", "<i>comment ...</i>"))), i == 0);
			expectedScript.addAction(new ActionComment(expectedScript, "comment-script", new ArrayList<String>(Arrays.asList("<i>comment ...</i>"))), i == 0);
			expectedScript.addAction(new ActionGotoUrl(expectedScript, 0, new CalculatedValue(script, "google.com")), i == 0);
			expectedScript.addAction(new ActionText(expectedScript, 0, new ArrayList<>(), "automated testing$key(ENTER)", new ArrayList<String>(Arrays.asList("INPUT [id = lst-ib]"))), i == 0);
			expectedScript.addAction(new ActionMouseKey(expectedScript, "click", 0, new ArrayList<>(), new ArrayList<String>(Arrays.asList("A [text = Test automation - Wikipedia]"))), i == 0);
			expectedScript.addAction(new ActionMouseScroll(expectedScript, "300", 0, new ArrayList<>(), new ArrayList<>()), i == 0);
			expectedScript.addAction(new ActionMouseScroll(expectedScript, "0", 0, new ArrayList<>(), new ArrayList<>(Arrays.asList("A [text = Graphical user interface testing]"))), i == 0);
			expectedScript.addAction(new ActionMouse(expectedScript, Mouse.OVER, 0, new ArrayList<>(), new ArrayList<>(Arrays.asList("IMG [src =~ .*Test_Automation_Interface.png]"))), i == 0);
			expectedScript.addAction(new ActionChannelClose(expectedScript, "myFirstChannel", false), i == 0);
			expectedScript.addAction(new ActionChannelSwitch(expectedScript, "newChannel"), i == 0);
			expectedScript.addAction(new ActionCallscript(expectedScript, new ArrayList<>(), "ScriptATS", new String[0], new String[0], null, null), i == 0);
			expectedScript.addAction(new ActionAssertProperty(expectedScript, 0, new ArrayList<>(), "id = test", new ArrayList<>()), i == 0);
			expectedScript.addAction(new ActionAssertCount(expectedScript, 0, new ArrayList<>(), "1", new ArrayList<>(Arrays.asList("INPUT [name = q]"))), i == 0);
			expectedScript.addAction(new ActionAssertValue(expectedScript, 0, "test = azerty"), i == 0);
			expectedScript.addAction(new ActionProperty(expectedScript, 0, new ArrayList<>(), "text", new Variable("propertyVar",new CalculatedValue(script, "")), new ArrayList<>(Arrays.asList("INPUT [name = q]"))), i == 0);
			expectedScript.addAction(new ActionWindowResize(expectedScript, "x = 0, y = 0, width = 0, height = 0"), i == 0);
			expectedScript.addAction(new ActionWindowState(expectedScript, "restore"), i == 0);
			expectedScript.addAction(new ActionWindowSwitch(expectedScript, Utils.string2Int("1"), new ArrayList<>(Arrays.asList("try = 2"))), i == 0);
			expectedScript.addAction(new ActionSelect(expectedScript, "index = 0", 0, new ArrayList<>(), new ArrayList<>(Arrays.asList("div"))), i == 0);
			expectedScript.addAction(new ActionScripting(expectedScript, 0, new ArrayList<>(), "alert(\"test\")", null, new ArrayList<>()), i == 0);
			expectedScript.addAction(new ActionNeoloadStart((Script) expectedScript, "", null), i == 0);
			expectedScript.addAction(new ActionNeoloadStop((Script) expectedScript, "", null, ""), i == 0);
			expectedScript.addAction(new ActionNeoloadContainer(expectedScript, "myContainer"), i == 0);
			expectedScript.addAction(new ActionNeoloadRecord(expectedScript, "pause"), i == 0);
		}

		for (int i = 0; i < data.length; i++) {
			lexer.createAction(script, data[i], true);
		}
		
		for (int i = 0; i < data.length; i++) {
			lexer.createAction(script, data[i], false);
		}

		Action[] allActions = script.getActions();
		for (int i = 0; i < allActions.length; i++) {
			assertEquals(expectedScript.getActions()[i].getJavaCode().toString() ,allActions[i].getJavaCode().toString());
		}
	}
}