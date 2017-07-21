package com.myccnice.util.ebay;

import com.myccnice.util.BeanAttrInfo;

/**
 * eBay相关参数配置
 * create in 2017年7月19日
 * @author wangpeng
 * @see http://developer.ebay.com/devzone/rest/ebay-rest/content/oauth-gen-user-token.html#Getting4
 */
public class EbayApi {

    @BeanAttrInfo(value = "-jtongi-SBX-e8e01d5e3-9994d690")
    public static final int CLIENT_ID = 1;

    @BeanAttrInfo(value = "SBX-8e01d5e36a58-4089-49d8-9593-e172")
    public static final int CLIENT_SECRET = 2;

    @BeanAttrInfo(value = "--jtongi-SBX-e8e-ryluagcdk")
    public static final int REDIRECT_URI = 3;

    @BeanAttrInfo(value = "https://signin.sandbox.ebay.com/authorize")
    public static final int REDIRECT_HOST_AND_PATH = 4;

    @BeanAttrInfo(value = "https://api.sandbox.ebay.com/identity/v1/oauth2/token")
    public static final int OAUTH_TOKEN_REQUEST_ENDPOINT = 5;
}
