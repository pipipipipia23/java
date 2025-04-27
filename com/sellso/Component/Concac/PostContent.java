package com.sellso.Component.Concac;

import com.sellso.Component.Data.Box;

import java.util.Map;
import java.util.function.Function;

public class PostContent {
    public Function<Object, Object> ContentFrontEnd = (something) -> {
        Box box = (Box) something;
        String contentType = box.getContentType() == null ? "application/json" : box.getContentType();
        StringBuilder content = new StringBuilder();
        content.append("HTTP/1.1 " + box.getStatus().getCode() + " " + box.getStatus().getMessage().split(" ", 2)[1] + "\r\n");
        
        if (box.getHeaders() != null && !box.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : box.getHeaders().entrySet()) {
                content.append(header.getKey() + ": " + header.getValue() + "\r\n");
            }
        } else {
            content.append("Content-Type: " + contentType + "\r\n");
        }
        
        content.append("Content-Length: " + box.getContent().length() + "\r\n");
        content.append("\r\n");
        content.append(box.getContent());
        return content;
    };
}
