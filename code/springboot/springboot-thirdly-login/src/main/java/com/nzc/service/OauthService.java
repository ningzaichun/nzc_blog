package com.nzc.service;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月28日 12:42
 */
public interface OauthService {

    /**
     * 根据传入的所选择的第三方登录类型，返回认证的url
     * @param loginType 第三方登录类型
     * @return
     */
    String choiceLoginType(String loginType);

    /**
     *
     * 根据用户的授权码和登录类型，获取第三方应用的 access_token
     * @param loginType 第三方登录类型
     * @param code 用户授权码
     * @return
     */
    String getOauthToken(String loginType, String code);


    /**
     * 获取授权的用户信息
     * @param loginType 第三方登录类型
     * @param accessToken 认证通过的令牌
     * @return
     */
    String getUserInfo(String loginType,String accessToken);

}
