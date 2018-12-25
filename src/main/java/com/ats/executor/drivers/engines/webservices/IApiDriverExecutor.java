package com.ats.executor.drivers.engines.webservices;

import com.ats.executor.ActionStatus;
import com.ats.script.actions.ActionApi;

public interface IApiDriverExecutor {
	public String execute(ActionStatus status, ActionApi api);
}
