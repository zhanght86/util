package com.myccnice.util.net;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * https请求
 *
 * create in 2017年6月14日
 * @author wangpeng
 */
public class HttpsClient {

    private static final String UTF8 = "UTF-8";
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsClient.class);
    /** URL地址 */
    private String url;
    /** POST 数据 */
    private Map<String, String> postData;
    /** 返回值 */
    private String responseData;
    /** 请求头 */
    private Header[] headers;

    public HttpsClient(String url, Map<String, String> postData) {
        this.url = url;
        this.postData = postData;
    }

    public HttpsClient(String url) {
        this.url = url;
    }

    public void setHeaders(Header[] headers) {
        this.headers = headers;
    }

    public void doPost() {
        try(SSLClient httpClient = new SSLClient()) {
            HttpPost httpPost = new HttpPost(url);
            if (postData != null) {
                //设置参数
                List<NameValuePair> list = new ArrayList<NameValuePair>();
                Iterator<Entry<String, String>> iterator = postData.entrySet().iterator();
                while (iterator.hasNext()) {
                    Entry<String,String> elem = (Entry<String, String>) iterator.next();
                    list.add(new BasicNameValuePair(elem.getKey(),elem.getValue()));
                }
                if (list != null) {
                    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(list, UTF8);
                    httpPost.setEntity(entity);
                }
            }
            if (headers != null && headers.length > 0) {
                httpPost.setHeaders(headers);
            }
            setResponseData(httpClient.execute(httpPost));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void doGet() {
        try(SSLClient httpClient = new SSLClient()) {
            HttpGet httpGet = new HttpGet(url);
            if (headers != null && headers.length > 0) {
                httpGet.setHeaders(headers);
            }
            setResponseData(httpClient.execute(httpGet));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    private void setResponseData(HttpResponse response) {
        HttpEntity resEntity = response.getEntity();
        if (resEntity != null) {
            try {
                responseData = EntityUtils.toString(resEntity, UTF8);
            } catch (Exception e) {
                LOGGER.error("获取HTTP响应数据失败_" + e.getMessage(), e);
            }
        }
    }

    public String getResponseData() {
        return responseData;
    }

}
