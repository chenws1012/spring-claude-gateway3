package com.shun.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chenwenshun on 2022/7/2
 */
@ConfigurationProperties("my-filter.config")
@Configuration
@RefreshScope
public class MyFilterConfiguration {
    private List<PathPattern> whitePatterns = List.of();

    public List<PathPattern> getWhitePatterns() {
        return whitePatterns;
    }

    private List<String> whiteList;

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
        PathPatternParser parser = new PathPatternParser();
        if (whiteList != null && !whiteList.isEmpty()) {
            this.whitePatterns = whiteList.stream()
                    .map(parser::parse)
                    .collect(Collectors.toList());
        } else {
            this.whitePatterns = List.of();
        }
    }
}
