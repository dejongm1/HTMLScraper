package com.mcd.spider.main.entities.audit;

import com.mcd.spider.main.util.SpiderUtil;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class OfflineResponse implements Response {

	private int statusCode;
	private String url;
	private String contentType;
	private Map<String, String> headers = new HashMap<>();
	private Map<String,String> cookies = new HashMap<>();
	
	public OfflineResponse(int statusCode, String url) {
		this.statusCode = statusCode;
		this.url = url;
		this.headers.put(null, "HTTP/1.1 " + statusCode + " OK");
		this.cookies.put("__uzmc", "1150230450250");
		this.cookies.put("__uzmd", String.valueOf(Calendar.getInstance().getTimeInMillis()));
		this.cookies.put("PHPSESSID", "o538ftmf8fvepqt52n2bt088i2");
		this.cookies.put("views_session", "1");
		this.contentType = determineContentType();
	}
	
	public OfflineResponse(int statusCode, String url, Map<String,String> cookies) {
		this.statusCode = statusCode;
		this.url = url;
		this.headers.put(null, "HTTP/1.1 " + statusCode + " OK");
		this.cookies.put("__uzmc", "1150230450250");
		this.cookies.put("__uzmd", String.valueOf(Calendar.getInstance().getTimeInMillis()));
		this.cookies.put("PHPSESSID", "o538ftmf8fvepqt52n2bt088i2");
		this.cookies.put("views_session", String.valueOf(Integer.valueOf(cookies.get("views_session"))+1));
		this.contentType = determineContentType();
	}
	

	@Override
	public Document parse() throws IOException {
		return SpiderUtil.getOfflinePage(url);
	}

	@Override
	public int statusCode() {
		return this.statusCode;
	}

	@Override
	public String statusMessage() {
		return "Dummy reponse status for offline testing";
	}
	
	@Override
	public Map<String,String> headers() {
		return this.headers;
	}
	
	private String determineContentType() {
		if (url.endsWith("pdf")) {
			return "application/pdf";
		} else if (url.endsWith("xml")) {
			return "text/xml";
		} else if (url.endsWith("css")) {
			return "text/css";
		} else if (url.endsWith("js")) {
			return "application/javascript";
		} else {
			return "text/html";
		}
	}
	
	@Override
	public URL url() {
		try {
			return new URL(this.url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String cookie(String arg0) {
		return cookies.get(arg0);
	}

	@Override
	public Response cookie(String arg0, String arg1) {
		this.cookies.put(arg0, arg1);
		return this;
	}

	@Override
	public Map<String, String> cookies() {
		return cookies;
	}

	@Override
	public boolean hasCookie(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasHeader(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasHeaderWithValue(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String header(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response header(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Method method() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response method(Method arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response removeCookie(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response removeHeader(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response url(URL arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String body() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] bodyAsBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String charset() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response charset(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String contentType() {
		return contentType;
	}

}
