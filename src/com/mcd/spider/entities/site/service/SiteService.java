package com.mcd.spider.entities.site.service;

import com.mcd.spider.entities.site.Site;

public interface SiteService extends Site {

    String getHost();
    String getUserAgent();
    String getAcceptType();
    String getAcceptLanguage();
    String getAcceptEncoding();
    String getReferer();
    String getContentType();
    String getContentLength();
    String getCookie();
    String getRequestBody(String[] args);
    String getRequestType();
    String getServiceUrl();
    String getProxyConnection();
}
