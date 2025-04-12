package com.sellso.Component.MotThuGiDo;

import java.util.Map;

public class Reponse {
    int Status;
    String ContentType;
    String Body;
    Map<String, String> Headers;

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
        return Headers;
    }

    @Override
    public String toString() {
        return "Reponse{" +
                "Status=" + Status +
                ", ContentType='" + ContentType + '\'' +
                ", Body='" + Body + '\'' +
                ", Headers=" + Headers +
                '}';
    }
}
