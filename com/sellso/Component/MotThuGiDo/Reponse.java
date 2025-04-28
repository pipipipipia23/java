package com.sellso.Component.MotThuGiDo;

import java.util.HashMap;
import java.util.Map;

public class Reponse {
    int Status;
    String ContentType;
    String Body;
    Map<String, String> Headers = new HashMap<>();
    private final Map<String, String> cookies = new HashMap<>();

    public int getStatus() {
        return Status;
    }
    public void setStatus(int status) {
        Status = status;
    }
    public String getContentType() {
        return ContentType;
    }
    public void setContentType(String contentType) {
        ContentType = contentType;
    }
    public void setBody(String body) {
        Body = body;
    }
    public void setHeaders(Map<String, String> headers) {
        Headers = headers;
    }

    public String getBody() {
        return Body;
    }
    public Map<String, String> getHeaders() {
        if (Headers == null) {
            Headers = new HashMap<>();
        }
        return Headers;
    }

    public void addCookie(String name, String value) {
        cookies.put(name, value);
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public String formatCookies() {
        StringBuilder cookieHeader = new StringBuilder();
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            // Return just one cookie per header (correct HTTP format)
            return entry.getKey() + "=" + entry.getValue();
        }
        return "";
    }

    public void setDefaultHeaders() {
        if (Headers == null) {
            Headers = new HashMap<>();
        }
        Headers.putIfAbsent("Content-Type", "text/html; charset=utf-8");
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "Status=" + Status +
                ", ContentType='" + ContentType + '\'' +
                ", Body='" + Body + '\'' +
                ", Headers=" + Headers +
                ", Cookies=" + cookies +
                '}';
    }
}
