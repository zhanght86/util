package com.myccnice.util.ebay.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.myccnice.util.ConfigManager;
import com.myccnice.util.ConstSymbol;
import com.myccnice.util.ebay.EbayApi;
import com.myccnice.util.ebay.service.EbayGrantService;
import com.myccnice.util.ebay.vo.EbayAccessToken;
import com.myccnice.util.encryption.Base64;
import com.myccnice.util.net.HttpsClient;

/**
 * eBay授权
 *
 * create in 2017年7月19日
 * @author wangpeng
 * @see <a href="https://developer.ebay.com/devzone/rest/ebay-rest/content/oauth-gen-user-token.html">Getting a User token</a>
 * @see <a href="http://www.ebay.cn/newcms/developer/APIs">eBay Buy & Sell APIs简介及更新说明</a>
 * @see <a href="http://www.ebay.cn/newcms/d_devdocs_apis/4">OAuth快速入门</a>
 * @see <a href="https://go.developer.ebay.com/api-documentation">API Document</a>
 */
public class EbayGrantServiceImpl implements EbayGrantService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EbayGrantServiceImpl.class);
    private static final ConfigManager CM = ConfigManager.getInstance();
    private static final String DEFAULT_ENC = "UTF-8";
    /**
     * eBay scope 清单
     */
    private static final List<String> SCOPE_LIST = new ArrayList<>();
    static {
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/buy.order.readonly");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/buy.guest.order");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.marketing.readonly");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.marketing");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.inventory.readonly");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.inventory");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.account.readonly");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.account");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.fulfillment.readonly");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.fulfillment");
        SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/sell.analytics.readonly");
        // 下面两个scope如果加入到scope参数中会导致eBay返回下面的错误：${Your auth accepted URL}/error=invalid_scope
        //SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/buy.item.feed");
        //SCOPE_LIST.add("https://api.ebay.com/oauth/api_scope/buy.marketing");
    }
    private static final String SCOPE_NAME = "scope";
    private static final String SCOPE_VALUE = getScope();
    /**
     * eBay api 参数名称
     */
    private static final String CLIENT_ID = "client_id";
    private static final String REDIRECT_URI = "redirect_uri";
    private static final String RESPONSE_TYPE = "response_type";
    private static final String STATE = "state";
    private static final String CODE = "code";
    private static final String GRANT_TYPE = "grant_type";
    private static final String REFRESH_TOKEN = "refresh_token";
    /**
     * eBay https 请求头
     */
    private static final Header[] HEADERS = new Header[2];
    static {
        HEADERS[0] = new BasicHeader("Content-Type", "application/x-www-form-urlencoded");
        HEADERS[1] = new BasicHeader("Authorization", getAuthorization());
    }

    @Override
    public String getUsersAuthcodeUrl(String state) {
        StringBuilder sb = new StringBuilder();
        sb.append(CM.getValue(EbayApi.class, EbayApi.REDIRECT_HOST_AND_PATH));
        sb.append(ConstSymbol.QUESTION);
        sb.append(CLIENT_ID).append(ConstSymbol.EQUAL).append(CM.getValue(EbayApi.class, EbayApi.CLIENT_ID)).append(ConstSymbol.ADDRESS);
        sb.append(REDIRECT_URI).append(ConstSymbol.EQUAL).append(CM.getValue(EbayApi.class, EbayApi.REDIRECT_URI)).append(ConstSymbol.ADDRESS);
        sb.append(RESPONSE_TYPE).append(ConstSymbol.EQUAL).append(CODE).append(ConstSymbol.ADDRESS);
        sb.append(STATE).append(ConstSymbol.EQUAL).append(state).append(ConstSymbol.ADDRESS);
        sb.append(SCOPE_NAME).append(ConstSymbol.EQUAL).append(SCOPE_VALUE);
        return sb.toString();
    }

    @Override
    public EbayAccessToken getUserToken(String code) {
        String url = CM.getValue(EbayApi.class, EbayApi.OAUTH_TOKEN_REQUEST_ENDPOINT);
        Map<String, String> postData = new HashMap<>();
        postData.put(GRANT_TYPE, "authorization_code");
        // 官方文档：This value must be URL encoded.
        // 实际上如果对code进行URL encoded，eBay会返回code错误提示
        postData.put(CODE, code);
        postData.put(REDIRECT_URI, CM.getValue(EbayApi.class, EbayApi.REDIRECT_URI));
        HttpsClient client = new HttpsClient(url, postData);
        client.setHeaders(HEADERS);
        client.doPost();
        return toToken(client.getResponseData());
    }

    @Override
    public EbayAccessToken refreshUserToken(String refreshToken) {
        String url = CM.getValue(EbayApi.class, EbayApi.OAUTH_TOKEN_REQUEST_ENDPOINT);
        Map<String, String> postData = new HashMap<>();
        postData.put(GRANT_TYPE, "refresh_token");
        // 官方文档：Set to the URL-encoded value of your refresh token that is returned by eBay
        // 实际上如果对refreshToken进行编码，eBay会返回refreshToken错误提示
        postData.put(REFRESH_TOKEN, refreshToken);
        // 根据官方文档，这里的scope需要和第一步获取用户授权码时一致或者可选，但实际设置任何值都会得到如下返回
        // {
        //  "error":"invalid_scope",
        //  "error_description":"The requested scope is invalid, unknown, malformed, or exceeds the scope granted to the client"
        // }
        //postData.put(SCOPE_NAME, SCOPE_VALUE);
        HttpsClient client = new HttpsClient(url, postData);
        client.setHeaders(HEADERS);
        client.doPost();
        return toToken(client.getResponseData());
    }

    private EbayAccessToken toToken(String response) {
        return JSONObject.toJavaObject(JSONObject.parseObject(response), EbayAccessToken.class);
    }

    /**
     * 放入URL请求中的scope需要编码处理；多个scope之间以空格隔开
     * @return 编码后的scope
     */
    private static String getScope() {
        StringBuilder sb = new StringBuilder();
        for (String scope : SCOPE_LIST) {
            sb.append(scope).append(ConstSymbol.SPACE);
        }
        if (sb.length() > 0) {
            sb.setLength(sb.length() -1);
        }
        try {
            return URLEncoder.encode(sb.toString(), DEFAULT_ENC);
        } catch (UnsupportedEncodingException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * Authorization 的值为：Basic <B64_encoded_oauth_credentials>
     * @return 请求头Authorization
     */
    private static String getAuthorization() {
        StringBuilder credentials = new StringBuilder();
        credentials.append(CM.getValue(EbayApi.class, EbayApi.CLIENT_ID));
        credentials.append(ConstSymbol.COLON);
        credentials.append(CM.getValue(EbayApi.class, EbayApi.CLIENT_SECRET));
        StringBuilder sb = new StringBuilder();
        sb.append("Basic").append(ConstSymbol.SPACE);
        sb.append(Base64.encode(credentials.toString()));
        return sb.toString();
    }

    @Override
    public EbayAccessToken getApplicationToken() {
        String url = CM.getValue(EbayApi.class, EbayApi.OAUTH_TOKEN_REQUEST_ENDPOINT);
        Map<String, String> postData = new HashMap<>();
        postData.put(GRANT_TYPE, "client_credentials");
        postData.put(REDIRECT_URI, CM.getValue(EbayApi.class, EbayApi.REDIRECT_URI));
        postData.put(SCOPE_NAME, "https://api.ebay.com/oauth/api_scope");
        HttpsClient client = new HttpsClient(url, postData);
        client.setHeaders(HEADERS);
        client.doPost();
        return toToken(client.getResponseData());
    }
}
