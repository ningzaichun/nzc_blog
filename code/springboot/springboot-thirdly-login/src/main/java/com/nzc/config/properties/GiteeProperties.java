package com.nzc.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Yihui Wang
 * @date: 2022年08月28日 11:15
 */
@Data
@Component
@ConfigurationProperties(prefix = "gitee")
public class GiteeProperties {

    private String clientId;

    private String clientSecret;

    private String redirectUri;
}
