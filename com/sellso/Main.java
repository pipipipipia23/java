package com.sellso;

import com.sellso.Component.HttpServer;

public class Main {
    public static void main(String[] args) {
        try {
            HttpServer httpServer = new HttpServer(8080);

            httpServer.request("GET", "/{params1}/{params2}/{params3}", (request, response) -> {
                response.setStatus(200);
                response.setContentType("text/html; charset=utf-8");
                return "<!DOCTYPE html>\n" +
                        "<html lang=\"vi\">\n" +
                        "<head>\n" +
                        "    <meta charset=\"UTF-8\">\n" +
                        "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                        "    <title>Trang Web Đơn Giản</title>\n" +
                        "    <style>\n" +
                        "        body {\n" +
                        "            margin: 0;\n" +
                        "            font-family: 'Segoe UI', sans-serif;\n" +
                        "            background: #f4f4f4;\n" +
                        "            color: #333;\n" +
                        "        }\n" +
                        "\n" +
                        "        header {\n" +
                        "            background: #3498db;\n" +
                        "            color: white;\n" +
                        "            padding: 20px 0;\n" +
                        "            text-align: center;\n" +
                        "        }\n" +
                        "\n" +
                        "        nav {\n" +
                        "            background: #2980b9;\n" +
                        "            padding: 10px;\n" +
                        "            text-align: center;\n" +
                        "        }\n" +
                        "\n" +
                        "        nav a {\n" +
                        "            color: white;\n" +
                        "            text-decoration: none;\n" +
                        "            margin: 0 15px;\n" +
                        "            font-weight: bold;\n" +
                        "        }\n" +
                        "\n" +
                        "        .container {\n" +
                        "            max-width: 900px;\n" +
                        "            margin: 40px auto;\n" +
                        "            background: white;\n" +
                        "            padding: 30px;\n" +
                        "            border-radius: 10px;\n" +
                        "            box-shadow: 0 5px 15px rgba(0,0,0,0.1);\n" +
                        "        }\n" +
                        "\n" +
                        "        h1 {\n" +
                        "            color: #2c3e50;\n" +
                        "        }\n" +
                        "\n" +
                        "        footer {\n" +
                        "            text-align: center;\n" +
                        "            padding: 15px;\n" +
                        "            background: #ddd;\n" +
                        "            margin-top: 40px;\n" +
                        "        }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\n" +
                        "    <header>\n" +
                        "        <h1>Chào mừng đến với Trang Web</h1>\n" +
                        "        <p>Đơn giản, sạch sẽ, hiện đại</p>\n" +
                        "    </header>\n" +
                        "\n" +
                        "    <nav>\n" +
                        "        <a href=\"#\">Trang chủ</a>\n" +
                        "        <a href=\"#\">Giới thiệu</a>\n" +
                        "        <a href=\"#\">Liên hệ</a>\n" +
                        "    </nav>\n" +
                        "\n" +
                        "    <div class=\"container\">\n" +
                        "        <h2>Giới thiệu</h2>\n" +
                        "        <p>Đây là một ví dụ về trang web đơn giản sử dụng HTML và CSS thuần. Bạn có thể tùy biến thêm để biến nó thành blog, landing page, hay portfolio cá nhân.</p>\n" +
                        "\n" +
                        "        <h2>Khả năng mở rộng</h2>\n" +
                        "        <ul>\n" +
                        "            <li>Thêm hình ảnh, icon, hiệu ứng</li>\n" +
                        "            <li>Kết nối với backend hoặc form</li>\n" +
                        "            <li>Tích hợp Bootstrap, Tailwind hoặc JS</li>\n" +
                        "        </ul>\n" +
                        "    </div>\n" +
                        "\n" +
                        "    <footer>\n" +
                        "        &copy; 2025 Trang Web Đơn Giản. All rights reserved.\n" +
                        "    </footer>\n" +
                        "\n" +
                        "</body>\n" +
                        "</html>\n";
            });

            httpServer.request("GET", "/vaicalon/{params1}/{params2}/{params3}", (request, response) -> {
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