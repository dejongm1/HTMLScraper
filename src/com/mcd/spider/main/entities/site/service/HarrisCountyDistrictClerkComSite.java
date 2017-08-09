package com.mcd.spider.main.entities.site.service;

import java.util.Arrays;

import com.mcd.spider.main.entities.site.Site;
import com.mcd.spider.main.entities.site.Url;
import com.mcd.spider.main.util.ConnectionUtil;

public class HarrisCountyDistrictClerkComSite implements SiteService {
	
	private static final Url url = new Url("http://", "www.hcdistrictclerk.com", new String[]{"/Common", "/e-services", "/PublicDatasets.aspx"});
	private static final String name = "HarrisCountyDistrictClerkCom";
	private String baseUrl;
	private int maxAttempts = 3;
	private int[] perRecordSleepRange = new int[]{0,0};
    private ConnectionUtil connectionUtil = new ConnectionUtil();
		
	public HarrisCountyDistrictClerkComSite() {
		setBaseUrl(null);
	}

	@Override
	public String getBaseUrl() {
		return baseUrl;
	}

	@Override
	public void setBaseUrl(String[] arg) {
		this.baseUrl = url.getProtocol()+url.getDomain()+Arrays.toString(url.getExtensions());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Url getUrl() {
		return url;
	}

	@Override
	public int getMaxAttempts() {
		return maxAttempts;
	}

	@Override
	public String generateRecordId(String param) {
        //id is the 18th element in line
		String[] stringList = param.split("\t");
		return stringList[17];
	}

	@Override
	public int[] getPerRecordSleepRange() {
		return perRecordSleepRange;
	}

	@Override
	public String getHost() {
		return "www.hcdistrictclerk.com";
	}

	@Override
	public String getUserAgent() {
		return connectionUtil.getRandomUserAgent();
	}

	@Override
	public String getAcceptType() {
		return "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
	}

	@Override
	public String getAcceptLanguage() {
		return "en-US,en;q=0.8";
	}

	@Override
	public String getAcceptEncoding() {
		return "gzip, deflate";
	}

	@Override
	public String getReferer() {
		return "http://www.hcdistrictclerk.com/Common/e-services/PublicDatasets.aspx";
	}

	@Override
	public String getContentType() {
		return "application/x-www-form-urlencoded";
	}

	@Override
	public String getContentLength() {
		return "5508";
	}

	@Override
	public String getCookie() {
		return "ASP.NET_SessionId=qqqvwgubssbj55pxnhq2px3a; BNES_ASP.NET_SessionId=qANP5GDIjMxnCDU1fGDI6iVOski9LgI56tGQaEqu/Llf7YbhowCZamDS1BsDcDmXsui6gvcmDPfjRXB7hxZxN0zhaPDZ0IBkLRNKvLmQWKZCnTGOm9Vdbw==";
	}

	@Override
	public String getRequestBody(String[] args) {
		String hiddenDownloadFile = args[0];
		//hiddenDownloadFile=Criminal%5C2017-08-02+CrimFilingsDaily_withHeadings.txt
		String requestBody = "__lastTab=&__leftPos=0&__topPos=0&__EVENTTARGET=&__EVENTARGUMENT=&__VIEWSTATE=%2FwEPDwUKLTgxNjAwNjEwNw9kFgJmD2QWAmYPZBYCZg9kFgICAw9kFgoCAg9kFgICAQ8PFgIeC05hdmlnYXRlVXJsBVB%2BL2VEb2NzL1NlY3VyZS9Mb2dpbi5hc3B4P1JldHVyblVybD0lMmZDb21tb24lMmZlLXNlcnZpY2VzJTJmUHVibGljRGF0YXNldHMuYXNweGRkAgMPZBYCZg8WBB4Dc3JjBXJodHRwczovL3d3dy5oY2Rpc3RyaWN0Y2xlcmsuY29tL2VEb2NzL1NlY3VyZS9XaWRlTG9naW4uYXNweD9SZXR1cm5Vcmw9JTJmQ29tbW9uJTJmZS1zZXJ2aWNlcyUyZlB1YmxpY0RhdGFzZXRzLmFzcHgeBWNsYXNzBRNsb2dpbmFyZWFpZnJhbWV3aWRlZAIxDw8WAh4HVmlzaWJsZWdkZAJTD2QWAgICDxYEHgRUZXh0BacCPGEgaHJlZj0iaHR0cHM6Ly93d3cuZmFjZWJvb2suY29tL0hDRENPIiB0YXJnZXQ9Il9ibGFuayI%2BPGltZyBzcmM9Ii4uL0ltYWdlcy9JY29ucy9mYWNlYm9va19pY29uX3NtLnBuZyIgc3R5bGU9ImJvcmRlcjogMDsgdmVydGljYWwtYWxpZ246IGJvdHRvbTsiIGFsdD0iTGlrZSB1cyBvbiBGYWNlYm9vayIgb25lcnJvcj0idGhpcy5zdHlsZS5kaXNwbGF5PSdub25lJyIgLz48L2E%2BJm5ic3A7Jm5ic3A7Jm5ic3A7Jm5ic3A7IEhvdXN0b24sIFRleGFzJm5ic3A7Jm5ic3A7fCZuYnNwOyZuYnNwO0F1Z3VzdCAwMiwgMjAxNx8DZ2QCVA9kFgICBQ9kFgICAQ9kFgICAQ9kFgICAw8WAh4LXyFJdGVtQ291bnQCFBYoZg9kFgJmDxUEQjx0ciBjbGFzcz0ndHJQdWJsaWNSZXBvcnRzU3ViSGVhZGVyJz48dGQgY29sc3Bhbj00PkNpdmlsPC90ZD48L3RyPgA%2FMjAxNC0wMy0yNDwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5GSUVMRF9DT0RFUy54bHN4IkNpdmlsXFwyMDE0LTAzLTI0IEZJRUxEX0NPREVTLnhsc3hkAgEPZBYCZg8VBAk8dGQ%2BPC90ZD4OY2xhc3M9J3Jvd0FsdCc7MjAxNC0wMy0yNjwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5PVkVSVklFVy5wZGYeQ2l2aWxcXDIwMTQtMDMtMjYgT1ZFUlZJRVcucGRmZAICD2QWAmYPFQQJPHRkPjwvdGQ%2BAEIyMDE3LTA3LTA1PC90ZD48dGQgc3R5bGU9InZlcnRpY2FsLWFsaWduOnRvcDsiPkFjdE1vZHNfTW9udGhseS50eHQlQ2l2aWxcXDIwMTctMDctMDUgQWN0TW9kc19Nb250aGx5LnR4dGQCAw9kFgJmDxUECTx0ZD48L3RkPg5jbGFzcz0ncm93QWx0J0oyMDE3LTA3LTA1PC90ZD48dGQgc3R5bGU9InZlcnRpY2FsLWFsaWduOnRvcDsiPkNhc2VTZXR0aW5nTW9kc19Nb250aGx5LnR4dC1DaXZpbFxcMjAxNy0wNy0wNSBDYXNlU2V0dGluZ01vZHNfTW9udGhseS50eHRkAgQPZBYCZg8VBAk8dGQ%2BPC90ZD4ASjIwMTctMDctMDU8L3RkPjx0ZCBzdHlsZT0idmVydGljYWwtYWxpZ246dG9wOyI%2BQ2FzZVN1bW1hcnlNb2RzX01vbnRobHkudHh0LUNpdmlsXFwyMDE3LTA3LTA1IENhc2VTdW1tYXJ5TW9kc19Nb250aGx5LnR4dGQCBQ9kFgJmDxUECTx0ZD48L3RkPg5jbGFzcz0ncm93QWx0J0QyMDE3LTA3LTA1PC90ZD48dGQgc3R5bGU9InZlcnRpY2FsLWFsaWduOnRvcDsiPlBhcnR5TW9kc19Nb250aGx5LnR4dCdDaXZpbFxcMjAxNy0wNy0wNSBQYXJ0eU1vZHNfTW9udGhseS50eHRkAgYPZBYCZg8VBAk8dGQ%2BPC90ZD4ARjIwMTctMDctMDU8L3RkPjx0ZCBzdHlsZT0idmVydGljYWwtYWxpZ246dG9wOyI%2BU2VydmljZU1vZHNfTW9udGhseS50eHQpQ2l2aWxcXDIwMTctMDctMDUgU2VydmljZU1vZHNfTW9udGhseS50eHRkAgcPZBYCZg8VBAk8dGQ%2BPC90ZD4OY2xhc3M9J3Jvd0FsdCdAMjAxNy0wOC0wMjwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5BY3RNb2RzX0RhaWx5LnR4dCNDaXZpbFxcMjAxNy0wOC0wMiBBY3RNb2RzX0RhaWx5LnR4dGQCCA9kFgJmDxUECTx0ZD48L3RkPgBIMjAxNy0wOC0wMjwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5DYXNlU2V0dGluZ01vZHNfRGFpbHkudHh0K0NpdmlsXFwyMDE3LTA4LTAyIENhc2VTZXR0aW5nTW9kc19EYWlseS50eHRkAgkPZBYCZg8VBAk8dGQ%2BPC90ZD4OY2xhc3M9J3Jvd0FsdCdIMjAxNy0wOC0wMjwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5DYXNlU3VtbWFyeU1vZHNfRGFpbHkudHh0K0NpdmlsXFwyMDE3LTA4LTAyIENhc2VTdW1tYXJ5TW9kc19EYWlseS50eHRkAgoPZBYCZg8VBAk8dGQ%2BPC90ZD4AQjIwMTctMDgtMDI8L3RkPjx0ZCBzdHlsZT0idmVydGljYWwtYWxpZ246dG9wOyI%2BUGFydHlNb2RzX0RhaWx5LnR4dCVDaXZpbFxcMjAxNy0wOC0wMiBQYXJ0eU1vZHNfRGFpbHkudHh0ZAILD2QWAmYPFQQJPHRkPjwvdGQ%2BDmNsYXNzPSdyb3dBbHQnRDIwMTctMDgtMDI8L3RkPjx0ZCBzdHlsZT0idmVydGljYWwtYWxpZ246dG9wOyI%2BU2VydmljZU1vZHNfRGFpbHkudHh0J0NpdmlsXFwyMDE3LTA4LTAyIFNlcnZpY2VNb2RzX0RhaWx5LnR4dGQCDA9kFgJmDxUEeDx0cj48dGQgY29sc3Bhbj0nMycgc3R5bGU9J2hlaWdodDoyMHB4Oyc%2BPC90ZD48L3RyPjx0ciBjbGFzcz0ndHJQdWJsaWNSZXBvcnRzU3ViSGVhZGVyJz48dGQgY29sc3Bhbj00PkNyaW1pbmFsPC90ZD48L3RyPgA7MjAxMi0xMi0yMTwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5PVkVSVklFVy5wZGYhQ3JpbWluYWxcXDIwMTItMTItMjEgT1ZFUlZJRVcucGRmZAIND2QWAmYPFQQJPHRkPjwvdGQ%2BDmNsYXNzPSdyb3dBbHQnTjIwMTQtMDgtMTM8L3RkPjx0ZCBzdHlsZT0idmVydGljYWwtYWxpZ246dG9wOyI%2BUmVjb3JkTGF5b3V0c0FuZEZpZWxkTmFtZXMueGxzeDRDcmltaW5hbFxcMjAxNC0wOC0xMyBSZWNvcmRMYXlvdXRzQW5kRmllbGROYW1lcy54bHN4ZAIOD2QWAmYPFQQJPHRkPjwvdGQ%2BAE8yMDE3LTA4LTAyPC90ZD48dGQgc3R5bGU9InZlcnRpY2FsLWFsaWduOnRvcDsiPkNyaW1EaXNwb3NEYWlseV93aXRoSGVhZGluZ3MudHh0NUNyaW1pbmFsXFwyMDE3LTA4LTAyIENyaW1EaXNwb3NEYWlseV93aXRoSGVhZGluZ3MudHh0ZAIPD2QWAmYPFQQJPHRkPjwvdGQ%2BDmNsYXNzPSdyb3dBbHQnUTIwMTctMDgtMDI8L3RkPjx0ZCBzdHlsZT0idmVydGljYWwtYWxpZ246dG9wOyI%2BQ3JpbURpc3Bvc01vbnRobHlfd2l0aEhlYWRpbmdzLnR4dDdDcmltaW5hbFxcMjAxNy0wOC0wMiBDcmltRGlzcG9zTW9udGhseV93aXRoSGVhZGluZ3MudHh0ZAIQD2QWAmYPFQQJPHRkPjwvdGQ%2BAFAyMDE3LTA4LTAyPC90ZD48dGQgc3R5bGU9InZlcnRpY2FsLWFsaWduOnRvcDsiPkNyaW1GaWxpbmdzRGFpbHlfd2l0aEhlYWRpbmdzLnR4dDZDcmltaW5hbFxcMjAxNy0wOC0wMiBDcmltRmlsaW5nc0RhaWx5X3dpdGhIZWFkaW5ncy50eHRkAhEPZBYCZg8VBAk8dGQ%2BPC90ZD4OY2xhc3M9J3Jvd0FsdCdSMjAxNy0wOC0wMjwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5DcmltRmlsaW5nc01vbnRobHlfd2l0aEhlYWRpbmdzLnR4dDhDcmltaW5hbFxcMjAxNy0wOC0wMiBDcmltRmlsaW5nc01vbnRobHlfd2l0aEhlYWRpbmdzLnR4dGQCEg9kFgJmDxUECTx0ZD48L3RkPgBdMjAxNy0wOC0wMjwvdGQ%2BPHRkIHN0eWxlPSJ2ZXJ0aWNhbC1hbGlnbjp0b3A7Ij5DcmltRmlsaW5nc1dpdGhGdXR1cmVTZXR0aW5nc193aXRoSGVhZGluZ3MudHh0Q0NyaW1pbmFsXFwyMDE3LTA4LTAyIENyaW1GaWxpbmdzV2l0aEZ1dHVyZVNldHRpbmdzX3dpdGhIZWFkaW5ncy50eHRkAhMPZBYCZg8VBAk8dGQ%2BPC90ZD4OY2xhc3M9J3Jvd0FsdCc6KDIwMTctMDQtMTIpPC90ZD48dGQgc3R5bGU9InZlcnRpY2FsLWFsaWduOnRvcDsiPlRodW1icy5kYhNDcmltaW5hbFxcVGh1bWJzLmRiZGRsozz9D1c8mSc4U1dL2pCWTTefJQ%3D%3D&__VIEWSTATEGENERATOR=FE8E70D1&__EVENTVALIDATION=%2FwEdAAKGPK4sU%2BrAnudoqve4EVOnJqtxZyLu8zNmNl36rpQahb3m5gslhin8potjWepie4tPlJot&hiddenDownloadFile=" + hiddenDownloadFile + "&ctl00%24ctl00%24ctl00%24ContentPlaceHolder1%24ContentPlaceHolder2%24ContentPlaceHolder2%24buttonDownload=";
		return requestBody;
	}

	@Override
	public String getRequestType() {
		return "POST";
	}

	@Override
	public String getServiceUrl() {
		return "http://www.hcdistrictclerk.com/Common/e-services/PublicDatasets.aspx";
	}

	@Override
	public String getProxyConnection() {
		return "keep-alive";
	}
	
	public String getPragma() {
		return "no-cache";
	}
	
	public String getCacheControl() {
		return "no-cache";
	}
	
	public String getOrigin() {
		return "http://www.hcdistrictclerk.com";
	}
	
	public String getUpgradeInsecureRequests() {
		return "1";
	}
}
