package com.mcd.spider.entities.site;

public class Url {

	private String protocol;
	private String domain;
	private String[] extensions;

	public Url(String protocol, String domain, String[] extensions) {
		this.protocol = protocol;
		this.domain = domain;
		this.extensions = extensions;
	}

	public String getProtocol() {
		return protocol;
	}
	public String getDomain() {
		return domain;
	}
	public String[] getExtensions() {
		return extensions;
	}
	public String toString() {
	    return domain + extensions.toString();
    }

}
