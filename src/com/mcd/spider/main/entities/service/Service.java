package com.mcd.spider.main.entities.service;

import com.mcd.spider.main.entities.site.Site;

public interface Service {

    String getHost();
    String getUserAgent();
    String getAcceptType();
    String getAcceptLanguage();
    String getAcceptEncoding();
    String getReferer();
    String getContentType();
    String getXRequestedWith();
    String getContentLength();
    String getCookie();
    String getRequestBody(String[] args);
    String getRequestType();
    String getUrl();

    String getConnection();

    Site getSite();
}
