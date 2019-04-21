package actiontestscript;


import com.ats.executor.ActionTestScript;
import com.ats.generator.variables.Variable;
import com.ats.script.actions.*;
import com.ats.script.actions.neoload.*;


public class Test extends ActionTestScript {

	private final String getProjectGav(){return "com.functional.ats_test(0.0.2)";}

	Variable newVar = var("newVar");
	
	public void testMain(){
		exec(0,new ActionChannelStart(this, "newChannel", clv("chromium"), "neoload, Basic:dxnlcjpwyxnz"));
		exec(1,new ActionCallscript(this, clv("Angular"), newVar));
		exec(2,new ActionNeoloadStart(this, "init", clv("myUserPierre")));
		exec(3,new ActionNeoloadStop(this, "", clv("myUserPierre"), ""));
		exec(4,new ActionNeoloadStart(this, "", null));
		exec(5,new ActionNeoloadStart(this, "init", null));
		exec(6,new ActionNeoloadStart(this, "", null));
		exec(7,new ActionNeoloadStop(this, "", clv("myUser"), ""));
		exec(8,new ActionNeoloadStop(this, "", clv("myUser"), "20, sharedcont, deleterec"));
		exec(9,new ActionNeoloadStop(this, "framework, generic", null, ""));
		exec(10,new ActionNeoloadStop(this, "framework, generic", clv("myUser"), ""));
		exec(11,new ActionNeoloadStop(this, "generic", clv("myUser"), "20, sharedcont, deleterec, includevar"));
		exec(12,new ActionNeoloadContainer(this, "myContainer"));
		exec(14,new ActionText(this, true, 0, null, clv(getProjectGav())));
		exec(15,new ActionNeoloadRecord(this, ActionNeoloadRecord.PAUSE));
		exec(16,new ActionNeoloadRecord(this, ActionNeoloadRecord.RESUME));
		exec(17,new ActionNeoloadRecord(this, ActionNeoloadRecord.SCREENSHOT));
		exec(18,new ActionChannelClose(this, ""));
		exec(19,new ActionChannelClose(this, "newChannel"));
	}
}
