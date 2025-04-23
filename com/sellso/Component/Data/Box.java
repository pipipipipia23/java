package com.sellso.Component.Data;

import com.sellso.Component.Enum.HttpStatus;

public class Box {
    private HttpStatus status;
    private String Content;
    private String contentType;

    public Box(HttpStatus status,String Content, String contentType) {
        this.status = status;
        this.Content = Content;
        this.contentType = contentType;
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
}
