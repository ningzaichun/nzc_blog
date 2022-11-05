package com.nzc.service.impl;

import cn.hutool.http.HttpUtil;
import com.nzc.config.ThirdlyLoginTypeConstant;
import com.nzc.config.properties.GiteeProperties;
import com.nzc.service.OauthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月28日 12:42
 */
@Service
public class OauthServiceImpl implements OauthService {

    @Autowired
    GiteeProperties giteeProperties;

    @Override
    public String choiceLoginType(String loginType) {
        String url = "";
        if (ThirdlyLoginTypeConstant.GITEE.equals(loginType)) {
            url = ThirdlyLoginTypeConstant.GITEE_URL
                    .replace("{client_id}", giteeProperties.getClientId())
                    .replace("{redirect_uri}", giteeProperties.getRedirectUri());
        }
        return url;
    }

    @Override
    public String getOauthToken(String loginType, String code) {
        Map<String, Object> map = new HashMap<>();
        String result = "";
        if (ThirdlyLoginTypeConstant.GITEE.equals(loginType)) {
            String url = ThirdlyLoginTypeConstant.GITEE_OAUTH_TOKEN_URL;
            map.put("grant_type", "authorization_code");
            map.put("code", code);
            map.put("client_id", giteeProperties.getClientId());
            map.put("client_secret", giteeProperties.getClientSecret());
            map.put("redirect_uri", giteeProperties.getRedirectUri());
            //发送get请求并接收响应数据
            result = HttpUtil.createPost(url).form(map).execute().body();
        }
        return result;
    }

    @Override
    public String getUserInfo(String loginType, String accessToken) {
        String userInfo = "";
        if (ThirdlyLoginTypeConstant.GITEE.equals(loginType)) {
            String userInfoUrl = "https://gitee.com/api/v5/user?access_token=" + accessToken;
            userInfo = HttpUtil.createGet(userInfoUrl).execute().body();
        }
        return userInfo;
    }
}
