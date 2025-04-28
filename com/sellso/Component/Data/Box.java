package com.sellso.Component.Data;

import com.sellso.Component.Enum.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class Box {
    private HttpStatus status;
    private String Content;
    private String contentType;
    private Map<String, String> headers = new HashMap<>();

    public Box(HttpStatus status, String Content, String contentType) {
        this.status = status;
        this.Content = Content;
        this.contentType = contentType;
    }
    
    public Box(HttpStatus status, String Content, String contentType, Map<String, String> headers) {
        this.status = status;
        this.Content = Content;
        this.contentType = contentType;
        if (headers != null) {
            this.headers = headers;
        }
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    public HttpStatus getStatus() {
        return status;
    }
    
    public void setStatus(HttpStatus status) {
        this.status = status;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers = headers;
        }
    }

    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }
}
