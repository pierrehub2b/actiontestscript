package com.ats.executor.drivers.engines.webservices;

import java.net.URI;
import java.net.URISyntaxException;

public abstract class AbstractApiExecutor implements IApiDriverExecutor {

	protected URI uri;
	
	public AbstractApiExecutor(String wsUrl) {
		try {
			this.uri = new URI(wsUrl);
		} catch (URISyntaxException e) {}
	}
}