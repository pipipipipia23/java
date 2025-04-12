package com.sellso;

import com.sellso.Component.HttpServer;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer httpServer = new HttpServer(8080);

            httpServer.request("GET", "/{params1}/{params2}/{params3}", (request, response) -> {
                response.setStatus(200);
                System.out.println(request.toString());
//                System.out.println(response.toString());
                return "Hello, World!";
            });

            httpServer.request("GET", "/vaicalon", (request, response) -> {
                return "VaiCaLon!";
            });

            httpServer.request("POST", "/try", (request, response) -> {
                return "VaiCaLon!";
            });

            httpServer.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}