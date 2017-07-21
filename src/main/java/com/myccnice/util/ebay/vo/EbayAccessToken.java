package com.myccnice.util.ebay.vo;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * eBay获取到的token
 *
 * create in 2017年7月19日
 * @author wangpeng
 */
public class EbayAccessToken {

    @JSONField(name = "access_token")
    private String accessToken;

    @JSONField(name = "expires_in")
    private int expiresIn;

    @JSONField(name = "refresh_token")
    private String refreshToken;

    @JSONField(name = "refresh_token_expires_in")
    private int refreshTokenExpiresIn;

    @JSONField(name = "token_type")
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public int getRefreshTokenExpiresIn() {
        return refreshTokenExpiresIn;
    }

    public void setRefreshTokenExpiresIn(int refreshTokenExpiresIn) {
        this.refreshTokenExpiresIn = refreshTokenExpiresIn;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    @Override
    public String toString() {
        return "EbayAccessToken [accessToken=" + accessToken + ", expiresIn="
                + expiresIn + ", refreshToken=" + refreshToken
                + ", refreshTokenExpiresIn=" + refreshTokenExpiresIn
                + ", tokenType=" + tokenType + "]";
    }

}
