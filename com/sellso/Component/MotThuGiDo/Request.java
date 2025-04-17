package com.sellso.Component.MotThuGiDo;

import java.util.Map;

public class Request {
    String body;
    String method;
    String path;
    Map<String, String> headers;
    Map<String, String> params;

    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public String getMethod() {
        return method;
    }
    public void setMethod(String method) {
        this.method = method;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Map<String, String> getHeaders() {
        return headers;
    }
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "Request{" +
                "body='" + body + '\'' +
                ", method='" + method + '\'' +
                ", path='" + path + '\'' +
                ", headers=" + headers +
                '}';
    }
}
