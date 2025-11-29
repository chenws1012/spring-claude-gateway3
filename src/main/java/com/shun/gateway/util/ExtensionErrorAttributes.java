package com.shun.gateway.util;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

/**
 * Created by chenwenshun on 2022/7/1
 */
@Component
public class ExtensionErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = super.getErrorAttributes(request, options);

        errorAttributes.put("code", errorAttributes.get("status"));
        errorAttributes.put("message", errorAttributes.get("error"));
        return errorAttributes;
    }
}
