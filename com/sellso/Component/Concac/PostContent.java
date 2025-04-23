package com.sellso.Component.Concac;

import com.sellso.Component.Data.Box;

import java.util.function.Function;

public class PostContent {
    public Function<Object, Object> ContentFrontEnd = (something) -> {
        Box box = (Box) something;
        String contentType = box.getContentType() == null ? "application/json" : box.getContentType();
        StringBuilder content = new StringBuilder();
        content.append("HTTP/1.1 " + box.getStatus().getCode() + " " + box.getStatus().getMessage() + "\r\n");
        content.append("Content-Type: " + contentType +"\r\n");
        content.append("Content-Length: " + box.getContent().length() + "\r\n");
        content.append("\r\n");
        content.append(box.getContent());
        return content;
    };
}
