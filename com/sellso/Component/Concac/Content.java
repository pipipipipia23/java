package com.sellso.Component.Concac;

import com.sellso.Component.Data.Box;

import java.util.function.Function;

public class Content {
    public Function<Object, Object> ContentFrontEnd = (something) -> {
        Box box = (Box) something;
        StringBuilder content = new StringBuilder();
        content.append("HTTP/1.1 " + box.getStatus().getCode() + " " + box.getStatus().getMessage() + "\r\n");
        content.append("Content-Type: " + box.getContentType() +"\r\n");
        content.append("Content-Length: " + box.getContent().length() + "\r\n");
        content.append("\r\n");
        content.append(box.getContent());
        return content;
    };
}
