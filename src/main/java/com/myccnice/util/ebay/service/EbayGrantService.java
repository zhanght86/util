package com.myccnice.util.ebay.service;

import com.myccnice.util.ebay.vo.EbayAccessToken;

/**
 * eBay授权接口
 *
 * create in 2017年7月19日
 * @author wangpeng
 */
public interface EbayGrantService {

    /**
     * 获取用户允许并获取授权代码给应用
     * @param state 额外参数，eBay会原样返回
     * @return 组装获取授权码的连接地址
     */
    String getUsersAuthcodeUrl(String state);

    /**
     * 用第一步得到的授权码获取token
     * @param code 授权码
     * @return token对象
     */
    EbayAccessToken getUserToken(String code);

    /**
     * User Token过期后使用refreshToken刷新
     * @param refreshToken
     * @return user token
     */
    EbayAccessToken refreshUserToken(String refreshToken);

    /**
     * 获取Application token
     * @return Application token
     */
    EbayAccessToken getApplicationToken();
}
