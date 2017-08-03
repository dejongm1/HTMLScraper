package com.mcd.spider.main.entities.site.service;

import com.mcd.spider.main.engine.record.iowa.DesMoinesRegisterComEngine;
import com.mcd.spider.main.entities.site.Url;
import com.mcd.spider.main.util.ConnectionUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DesMoinesRegisterComSite implements SiteService {

	private static final Url url = new Url("http://", "data.desmoinesregister.com/iowa-mugshots/index.php", new String[]{});
	private static final String name = "DesMoinesRegister.com";
	private String baseUrl;
	private static final int[] perRecordSleepRange = new int[]{1,2};
	private final int maxAttempts = 3;

	public DesMoinesRegisterComSite(String[] args) {
		setBaseUrl(args);
	}
	
	@Override
	public Url getUrl() {
		return url;
	}
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setBaseUrl(String[] args) {
		if (baseUrl==null) {
            Url url = getUrl();
            String builtUrl = url.getProtocol() + url.getDomain();
            if (args!=null && args.length>0) {
                builtUrl += "?co=" + args[0];
            }
            baseUrl = builtUrl.toLowerCase();
		}
	}
	@Override
	public String getBaseUrl() {
		return this.baseUrl;
	}
	@Override
    public String getHost() {
        return "data.desmoinesregister.com";
    }
    @Override
    public String getUserAgent() {
        return ConnectionUtil.getRandomUserAgent();
    }
    @Override
    public String getAcceptType() {
        return "application/json, text/javascript, */*; q=0.01";
    }
    @Override
    public String getAcceptLanguage() {
        return "en-US,en;q=0.5";
    }
    @Override
    public String getAcceptEncoding() {
        return "gzip, deflate";
    }
    @Override
    public String getReferer() {
        return null;
    }
    @Override
    public String getContentType() {
        return "application/x-www-form-urlencoded; charset=UTF-8";
    }
    public String getXRequestedWith() {
        return "XMLHttpRequest";
    }
    @Override
    public String getContentLength() {
        return "115";
    }
    @Override
    public String getCookie() {
        return "firefly_akamai=\"cLkvZkvH7cwea80CJ+rzt9cwX8wnDMCM3tE9MUVJmJncB7suSielvriXGBSA/LzsvfWsybxGAz0bAEe5HsGyc5881eOcBkc9PTGxZhwFaE4qAdmgKbnutW1vu+W7GDUvojNccx5/6hSyKNKuum5mpjKeDVoHoeD7NH4jm+2gvIE=\"; firefly_general=\"eyJlcG9jaER1cmF0aW9uIjogMjU5MjAwMH0=\"; cX_LSP=csst%3D1495388706788%26psst%3D1494931551541; cX_P=izn80nagz6zxtja1; gup_anonid=33e35a58-fc6f-11e6-82af-0637abe42581; AMCV_CF4957F555EE9B727F000101%40AdobeOrg=1999109931%7CMCMID%7C08137730111966913956488348982869344992%7CMCAID%7CNONE%7CMCAAMLH-1495536356%7C9%7CMCAAMB-1495536358%7Chmk_Lq6TPIBMW925SPhw3Q%7CMCCIDH%7C1030878072; __gads=ID=f129007cfb05976e:T=1488146523:S=ALNI_MbgVMj3U0Y9i4xuaGEQxmZ-gmyL3Q; __tbc=%7Bjzx%7D6l728p5gqYPl92Ljk9MJnavQmqycOnDQA7P4PjGvljC0S4Ja7_qwYsfr0v2pDJUqL-dKnmz0JkjN7l4cyX-M-Zv-s6M0lcJmLdQRzsJbMcxIC2l7TQs-1JgqUhypJ-GTM-NXNzZFl08uOR6k8Xqy8xZdOpogTGxRAvPLIR7NAcGEi78mgz6cCwnoS3RwNSexnFOAhlz154SzCpmbdIS_1tl-_J4FgZMSK38aniBC8-6KdPrw_DbgzXpmn4rlfkAf-UfADqN8iu8pxHpx_Q5GDMb1g4gZtUxvxgK4dHvJ9JtqLnVKcIQXgEmAfC2Dzun95HAD9W0u7O_ZSHRPD3Qdwxa7Ruy73FWyHklDUvFLyyq2Qf_J78yf5FVBxKoEWRNTEitDHSn92qlXJbo0pxFKVi2zlZSn6Ex_jXNongBP4GYgZwwlFefvdy6zTGmQ_BEZ1tCtO2_nQ7nxabApCFKxZIAxV1jZxPEeWPZnSY2BxV8z2zqhqEgIJeBXY2xqVISdRYN4Q_cNWKdfbo3eXJwq_KbdRi_FsP_Wk8oMHDYdWKugOYtLYbiFb0_2lv88hHY5QVG-N8_ztrIpB1HT5pK7r1rvat04E7xaAOpYaAgs0Xg; xbc=%7Bjzx%7DhSS_mImmxwyTEBn-JWL3J56lO1V8tJ8vACKyeB74jDLoqLcSoMInYOcDeWDxynYwwpZKSRjx4lH45T5yulzTfH3ZAt3KmroGyu-14RmEGsok7UDmNdViNaHi_hxs0_c_DmuPJPtBPajtn_XWl9qIVqoVamigWChh0BGJTcAJJ2SMIjZLq35Y8z-aurEUifjKQMuvZNTyoTe3h4z-Yxjhv7_08gtAIlRYcr55YLi55I3vZfLP8lSCOt45kbBKtVK1; __ibxl=1; firefly_akamai_meter=sFhqO6TsfghnsQlbvEwyxwipzHUVnHeTdhnU5LGKp2Y=; firefly_akamai_open=eyJ2aWV3Q291bnQiOjIsICJ2aWV3VGhyZXNob2xkIjo1LCAidmlld2VkQ29udGVudCI6IFsiMzIyNTcwMDAxIiwgIjMyNjc3ODAwMSJdfQ==; AMCV_desmoinesregister1=MCAID%7C2CB8A53D05035169-6000118B600048F7; s_fid=5DFCB905AD0E0E76-2BEEBBA11D693F61; _cb_ls=1; _cb=B8xhRmDu7dcwKEJNd; _chartbeat2=.1500596863238.1500679872411.11.D_ZA-CIlARAxXmALBQUhvsBHZfKX; s_cc=true; _cb_svref=null; _chartbeat5=742,770,%2Fiowa-mugshots%2F%3Fco%3DPolk%26id%3D105764,http%3A%2F%2Fdata.desmoinesregister.com%2Fiowa-mugshots%2F%3Fco%3DPolk%23next,CSVEL1D-pbMDD__nazB0lNNs727sr,,c,BMaHAeDS6HJPDID_-0DxsJ5lVMtJm,desmoinesregister.com,";
    }
    @Override
    public String getRequestBody(String[] args) {
        String county = args.length>0?args[0]:"Polk";
        String action =args.length>1?args[1]:"getmugs";
        String start = args.length>2?args[2]:"0";
        String limit = args.length>3?args[3]:"10000";
        return "county=" + county + "&sex=Both&age=All&height=All&weight=All&booking=60%20days&search=&all=0&action=" +action + "&start=" + start + "&limit=" + limit;
    }
    @Override
    public String getRequestType() {
        return "POST";
    }
    @Override
    public String getServiceUrl() {
        return "http://data.desmoinesregister.com/iowa-mugshots/includes/lib/MugshotsDB.php";
    }
    @Override
    public String getProxyConnection() {
        return "keep-alive";
    }

    public Document getDetailsDoc(String url, DesMoinesRegisterComEngine engine) throws IOException, JSONException {
        //build http post request
        URL obj = new URL(getServiceUrl());
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod(getRequestType());
        con.setRequestProperty("User-Agent", getUserAgent());
        con.setRequestProperty("Accept-Language", getAcceptLanguage());
        con.setRequestProperty("Accept-Encoding", getAcceptEncoding());
        con.setRequestProperty("Accept", getAcceptType());
        con.setRequestProperty("Content-Length", getContentLength());
        con.setRequestProperty("Content-Type", getContentType());
        con.setRequestProperty("Cookie", "firefly_akamai=\"cLkvZkvH7cwea80CJ+rzt9cwX8wnDMCM3tE9MUVJmJncB7suSielvriXGBSA/LzsvfWsybxGAz0bAEe5HsGyc5881eOcBkc9PTGxZhwFaE4qAdmgKbnutW1vu+W7GDUvojNccx5/6hSyKNKuum5mpjKeDVoHoeD7NH4jm+2gvIE=\"; firefly_general=\"eyJlcG9jaER1cmF0aW9uIjogMjU5MjAwMH0=\"; cX_LSP=csst%3D1495388706788%26psst%3D1494931551541; cX_P=izn80nagz6zxtja1; gup_anonid=33e35a58-fc6f-11e6-82af-0637abe42581; AMCV_CF4957F555EE9B727F000101%40AdobeOrg=1999109931%7CMCMID%7C08137730111966913956488348982869344992%7CMCAID%7CNONE%7CMCAAMLH-1495536356%7C9%7CMCAAMB-1495536358%7Chmk_Lq6TPIBMW925SPhw3Q%7CMCCIDH%7C1030878072; __gads=ID=f129007cfb05976e:T=1488146523:S=ALNI_MbgVMj3U0Y9i4xuaGEQxmZ-gmyL3Q; __tbc=%7Bjzx%7D6l728p5gqYPl92Ljk9MJnavQmqycOnDQA7P4PjGvljC0S4Ja7_qwYsfr0v2pDJUqL-dKnmz0JkjN7l4cyX-M-Zv-s6M0lcJmLdQRzsJbMcxIC2l7TQs-1JgqUhypJ-GTM-NXNzZFl08uOR6k8Xqy8xZdOpogTGxRAvPLIR7NAcGEi78mgz6cCwnoS3RwNSexnFOAhlz154SzCpmbdIS_1tl-_J4FgZMSK38aniBC8-6KdPrw_DbgzXpmn4rlfkAf-UfADqN8iu8pxHpx_Q5GDMb1g4gZtUxvxgK4dHvJ9JtqLnVKcIQXgEmAfC2Dzun95HAD9W0u7O_ZSHRPD3Qdwxa7Ruy73FWyHklDUvFLyyq2Qf_J78yf5FVBxKoEWRNTEitDHSn92qlXJbo0pxFKVi2zlZSn6Ex_jXNongBP4GYgZwwlFefvdy6zTGmQ_BEZ1tCtO2_nQ7nxabApCFKxZIAxV1jZxPEeWPZnSY2BxV8z2zqhqEgIJeBXY2xqVISdRYN4Q_cNWKdfbo3eXJwq_KbdRi_FsP_Wk8oMHDYdWKugOYtLYbiFb0_2lv88hHY5QVG-N8_ztrIpB1HT5pK7r1rvat04E7xaAOpYaAgs0Xg; xbc=%7Bjzx%7DhSS_mImmxwyTEBn-JWL3J56lO1V8tJ8vACKyeB74jDLoqLcSoMInYOcDeWDxynYwwpZKSRjx4lH45T5yulzTfH3ZAt3KmroGyu-14RmEGsok7UDmNdViNaHi_hxs0_c_DmuPJPtBPajtn_XWl9qIVqoVamigWChh0BGJTcAJJ2SMIjZLq35Y8z-aurEUifjKQMuvZNTyoTe3h4z-Yxjhv7_08gtAIlRYcr55YLi55I3vZfLP8lSCOt45kbBKtVK1; __ibxl=1; firefly_akamai_meter=sFhqO6TsfghnsQlbvEwyxwipzHUVnHeTdhnU5LGKp2Y=; firefly_akamai_open=eyJ2aWV3Q291bnQiOjIsICJ2aWV3VGhyZXNob2xkIjo1LCAidmlld2VkQ29udGVudCI6IFsiMzIyNTcwMDAxIiwgIjMyNjc3ODAwMSJdfQ==; AMCV_desmoinesregister1=MCAID%7C2CB8A53D05035169-6000118B600048F7; s_fid=5DFCB905AD0E0E76-2BEEBBA11D693F61; _cb_ls=1; _cb=B8xhRmDu7dcwKEJNd; _chartbeat2=.1500596863238.1500692560679.11.B52cMjDiqlMTCOilflC-ebfoBs2puL; s_cc=true; _cb_svref=https%3A%2F%2Fwww.google.com%2F; _chartbeat5=");
        //con.setRequestProperty("Referer", getReferer());
        con.setRequestProperty("XRequested-With", getXRequestedWith());
        con.setRequestProperty("Host", getHost());

        //iterate over counties and add to list
        //try something other than getmugs
        String urlParameters = "action=getdetails&id=" + this.generateRecordId(url);

        // Send post request
        StringBuffer response = engine.sendRequest(con, urlParameters);

        JSONObject detailsObj = new JSONObject(response.toString());
        Document detailsDoc = Jsoup.parseBodyFragment(detailsObj.getString("details"));

        return detailsDoc;
        //return Jsoup.parse(response.toString());
    }
	public String getRecordDetailDocUrl(Element record) {
		String pdId = record.attr("id");
		return baseUrl+ "&id=" + pdId;
	}
	public Map<String,String> getRecordDetailDocUrls(List<Document> resultsPageDocs) {
		return null;
	}
	public Elements getRecordDetailElements(Document doc) {
		return doc.select("h1, p");
	}
    public boolean isARecordDetailDoc(Document doc) {
        if (doc!=null) {
            return !doc.select("#msdb-mug-container").isEmpty() && doc.select("#permalink-url").hasText();
        } else {
            return false;
        }
    }
    @Override
	public String generateRecordId(String url) {
		return url.substring(url.indexOf("&id=")+4, url.length());
	}
	public int[] getPerRecordSleepRange() {
		return perRecordSleepRange;
	}
    public List<String> getCounties() {
        return Arrays.asList("Polk", "Johnson", "Story");
    }

	@Override
	public int getMaxAttempts() {
		return maxAttempts;
	}
}
