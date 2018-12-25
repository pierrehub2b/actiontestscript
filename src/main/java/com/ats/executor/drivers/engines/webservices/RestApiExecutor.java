package com.ats.executor.drivers.engines.webservices;

import java.io.IOException;
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.ats.executor.ActionStatus;
import com.ats.script.actions.ActionApi;

public class RestApiExecutor extends AbstractApiExecutor {

	private HttpRequestBase request;
	private CloseableHttpClient httpClient;

	public RestApiExecutor(CloseableHttpClient httpClient, String wsUrl) {
		super(wsUrl);
		this.httpClient = httpClient;
	}

	@Override
	public String execute(ActionStatus status, ActionApi api) {

		final URI fullUri = uri.resolve(api.getMethod().getCalculated());

		switch (api.getType()) {

		case ActionApi.POST:
			request = new HttpPost(fullUri);
			break;
		case ActionApi.DELETE:
			request = new HttpDelete(fullUri);
			break;
		case ActionApi.PUT:
			request = new HttpPut(fullUri);
			break;
		default:
			request = new HttpGet(fullUri);
			break;
		}

		try {
			final HttpResponse response = httpClient.execute(request);
			final Header[] contentType = response.getHeaders("Content-Type");

			if(contentType != null && contentType.length > 0) {

				final String data = EntityUtils.toString(response.getEntity());
				final String type = contentType[0].getValue();

				if(type.contains("application/json")){
					return parseJson(status, data);
				}else if(type.contains("text/xml")){
					return parseXml(status, data);
				}else {
					status.setData(data);
					return data;
				}
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	private String parseJson(ActionStatus status, String data) {
		status.setData(data);
		return data;
	}

	private String parseXml(ActionStatus status, String data) {
		status.setData(data);
		return data;
	}
}
