package com.mcd.spider.main.entities.audit;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

import com.mcd.spider.main.util.SpiderUtil;

public class OfflineResponse implements Response {

	private int statusCode;
	private String url;
	private Map<String, String> headers = new HashMap<>();
	
	public OfflineResponse(int statusCode, String url) {
		this.statusCode = statusCode;
		this.url = url;
		this.headers.put(null, "HTTP/1.1 " + statusCode + " OK");
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Response cookie(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> cookies() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return null;
	}

}
