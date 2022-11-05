package com.nzc.pojo;

import lombok.Data;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月28日 12:18
 */
@Data
public class ThirdlyResult {

    /**
     * {
     * "access_token":"f7xxxb71fd491b",
     * "token_type":"bearer",
     * "expires_in":86400,
     * "refresh_token":"9f3098c7a8be09cdxxxxx53a2f69dccxxxx8e40f0800ced8",
     * "scope":"user_info",
     * "created_at":1661659283
     * }
     */
    private String accessToken;

    private String tokenType;

    private Long expiresIn;

    private String refreshToken;

    private String scope;

    private String createAt;
}
