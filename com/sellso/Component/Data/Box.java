package com.sellso.Component.Data;

import com.sellso.Component.Enum.HttpStatus;

public class Box {
    private HttpStatus status;
    private String contentType;

    public Box(HttpStatus status, String contentType) {
        this.status = status;
        this.contentType = contentType;
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
}
