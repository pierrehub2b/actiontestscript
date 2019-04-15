package actiontestscript;


import com.ats.executor.ActionTestScript;
import com.ats.script.actions.*;
import com.ats.script.actions.neoload.*;

public class Test extends ActionTestScript {


	public void testMain(){
		exec(0,new ActionChannelStart(this, "newChannel", clv("chromium"), true, "", ""));
		exec(1,new ActionNeoloadStart(this, "", clv("myUser")));
		exec(2,new ActionNeoloadStart(this, "init", clv("myUser")));
		exec(3,new ActionNeoloadStart(this, "init", null));
		exec(4,new ActionNeoloadStart(this, "", null));
		exec(5,new ActionNeoloadStop(this, "", null, ""));
		exec(6,new ActionNeoloadStop(this, "", clv("myUser"), ""));
		exec(7,new ActionNeoloadStop(this, "", clv("myUser"), "20, sharedcont, deleterec"));
		exec(8,new ActionNeoloadStop(this, "framework, generic", null, ""));
		exec(9,new ActionNeoloadStop(this, "framework, generic", clv("myUser"), ""));
		exec(10,new ActionNeoloadStop(this, "generic", clv("myUser"), "20, sharedcont, deleterec, includevar"));
		exec(11,new ActionNeoloadContainer(this, "myContainer"));
		exec(12,new ActionNeoloadRecord(this, ActionNeoloadRecord.PAUSE));
		exec(13,new ActionNeoloadRecord(this, ActionNeoloadRecord.RESUME));
		exec(14,new ActionNeoloadRecord(this, ActionNeoloadRecord.SCREENSHOT));
	}
}
