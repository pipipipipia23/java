package com.sellso;

import com.sellso.Component.HttpServer;
import com.sellso.Component.PhpExcutor.PhpExecutorFull;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            PhpExecutorFull phpExecutorFull = new PhpExecutorFull("C:\\xampp\\php\\php.exe");

            //example
//            HttpServer httpServer = new HttpServer(8080);
//
//            httpServer.request("GET", "/{params1}/{params2}/{params3}", (request, response) -> {
//                response.setStatus(200);
//                response.setContentType("text/html; charset=utf-8");
//                return "Hello, World!";
//            });
//
//            httpServer.request("GET", "/vaicalon/{params1}/{params2}/{params3}", (request, response) -> {
//                response.setStatus(200);
//                response.setContentType("application/json");
//                return "{\"name\": \"VaiCaLon\"}";
//            });
//
//            httpServer.request("POST", "/try", (request, response) -> {
//                return "VaiCaLon!";
//            });
//
//            httpServer.request("GET", "/session", (request, response) -> {
//                if (request.getCookies().get("sessionId") == null) {
//                    response.addCookie("sessionId", "123456");
//                }
//                return "Visit count: ";
//            });
//            httpServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
