package com.sellso.Component;

import com.sellso.Component.Concac.Content;
import com.sellso.Component.Data.Box;
import com.sellso.Component.Enum.HttpStatus;
import com.sellso.Component.Method.GetMethod;
import com.sellso.Component.Method.PostMethod;
import com.sellso.Component.MotThuGiDo.Reponse;
import com.sellso.Component.MotThuGiDo.Request;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.UUID;

public class HttpServer {
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private boolean running = true;
    private static final int DEFAULT_PORT = 8080;
    private final Set<SocketChannel> blockingKeys = ConcurrentHashMap.newKeySet();
    private final Content GetContent = new Content();
    private final GetMethod getMethod = new GetMethod();
    private final PostMethod postMethod = new PostMethod();

    private final Map<String, Map<String, BiFunction<Request, Reponse, Object>>> pathMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, List<String>>> pathParams = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    public HttpServer(int Port) throws Exception {
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        int port = Port == 0 ? DEFAULT_PORT : Port;
        serverSocketChannel.bind(new InetSocketAddress(port));

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws Exception {
        System.out.println("HttpServer starting..." + serverSocketChannel.getLocalAddress());
        while (running) {
            int readyChannels = selector.select();

            if (readyChannels == 0) {
                continue;
            }

            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

            // lay key tiep theo de xu ly
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (!key.isValid()) {
                    continue;
                }

                try {
                    if (key.isAcceptable()) {
                        System.out.println("Acceptable");
                        acceptConnection(key);
                    } else if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        if (blockingKeys.contains(channel)) {
                            continue;
                        }

                        blockingKeys.add(channel);

                        // khoa ngay cai OP_READ de no khong bi chay lien tuc vao acceptable hoac xu ly nua.
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);

                        final SelectionKey readKey = key;
                        executor.submit(() -> {
                            // doan nay don gian chi la khi xu ly xong roi chay tiep den phan de enable lai cai OP_READ thoi
                            try {
                                handleReadRequest(readKey);
                            } catch (Exception e) {
                                // co exception tra ve thi close thoi.
                                System.err.println("Error handling read request: " + e.getMessage());
                                System.err.println("Error handling read request: " + e.getMessage());
                                closeConnection(readKey);
                            } finally {
                                try {
                                    if (channel.isOpen() && readKey.isValid()) {
                                        synchronized (selector) {
                                            readKey.interestOps(readKey.interestOps() | SelectionKey.OP_READ);
                                            selector.wakeup();
                                        }
                                    }
                                    blockingKeys.remove(channel);
                                } catch (Exception e) {
                                    System.err.println("Error updating key after processing: " + e.getMessage());
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    System.err.println("Error processing key: " + e.getMessage());
                    closeConnection(key);
                }
            }
        }
    }

    // accept connection
    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    // doc body neu nhu khong doc het tu luc request
    private String readRequestBody(SocketChannel socketChannel, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int bytesRead = 0;
        while (bytesRead < length) {
            buffer.clear();
            int read = socketChannel.read(buffer);
            if (read == -1) break;
            buffer.flip();
            outputStream.write(buffer.array(), 0, read);
            bytesRead += read;
        }
        return outputStream.toString();
    }

    private void closeConnection(SelectionKey key) {
        try {
            key.cancel();
            key.channel().close();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private void handleReadRequest(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder requestBuilder = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(4096);

        // cai nay xu ly doc roi noi vao chuoi boi vi minh chi set cho capacity cua no la 4096
        while (true) {
            buffer.clear();
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead == -1) break;
            if (bytesRead == 0) break;
            buffer.flip();

            String chunk = new String(buffer.array(), 0, buffer.limit());
            requestBuilder.append(chunk);

            if (requestBuilder.toString().contains("\r\n\r\n")) {
                break;
            }
        }

        // format de lay ra method , path
        String[] requestLines = requestBuilder.toString().split("\r\n");
        String[] requestLine = requestLines[0].split(" ");
        String method = "";
        String path = "";
        if (requestLine.length > 2) {
            method = requestLine[0].trim();
            path = requestLine[1].trim();
        } else {
            return;
        }
        if (!method.equals("GET")) {
            while (true) {
                buffer.clear();
                int bytesRead = socketChannel.read(buffer);
                if (bytesRead == -1) break;
                if (bytesRead == 0) break;
                buffer.flip();
                String chunk = new String(buffer.array(), 0, buffer.limit());
                requestBuilder.append(chunk);
            }
        } else {
            int bytesRead = socketChannel.read(buffer);
            if (bytesRead != 0) {
                Box saveBox = new Box(HttpStatus.MAX_LENGTH,"Max Length For Get", "Content-Type: text/html");
                getMethod.Get(socketChannel,saveBox);
            }
        }

        // format de lay ra header, body

        String[] parts = requestBuilder.toString().split("(\r\n\r\n|\n\n)");

        Map<String, String> headers = new HashMap<>();

        // xu ly header de add vao reponse

        String headerdata[] = parts[0].split("\n");

        for (String header : headerdata) {
            String[] headerParts = header.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }

        if (pathMap.get(method) == null && pathParams.get(method) == null) {
            System.out.println("Method not supported");
            Box saveBox = new Box(HttpStatus.NO_HAVE_METHOD,"Method Not Allowed", "Content-Type: text/html");
            getMethod.Get(socketChannel,saveBox);
            closeConnection(key);
            return;
        }

        String matchedPath = null;
        Map<String, String> params = new HashMap<>();

        // cai nay neu khong co params thi se contanskey duoc luon.
        if (pathMap.get(method) != null && pathMap.get(method).containsKey(path)) {
            System.out.println(path);
            matchedPath = path;
        } else {
            // check xem params co ton tai khong
            if (pathParams.get(method) != null) {
                // lay het key set ra de so sanh voi path.
                for (String patternPath : pathParams.get(method).keySet()) {
                    Map<String, String> extractedParams = matchPathWithPattern(path, patternPath);
                    if (extractedParams != null) {
                        matchedPath = patternPath;
                        params = extractedParams;
                        break;
                    }
                }
            }
        }

        Reponse reponse = new Reponse();
        Request request = new Request();

        if (matchedPath == null) {
            System.out.println("Path not found: " + path);
            Box saveBox = new Box(HttpStatus.NOT_FOUND, "NOT FOUND", "Content-Type: text/html");
            sendResponse(socketChannel, saveBox, reponse);
            closeConnection(key);
            return;
        }

        if (headers.containsKey("Cookie")) {
            request.setCookies(parseCookies(headers.get("Cookie")));
        }

        // handleSession(request, reponse);

        switch (method) {
            case "GET" -> {
                // request, reponse
                if (parts.length > 1) {
                    request.setBody(parts[1]);
                }
                request.setHeaders(headers);
                request.setMethod(method);
                request.setPath(path);
                request.setParams(params);

                var callback = pathMap.get(method).get(matchedPath).apply(request, reponse);
                Box saveBox = new Box(HttpStatus.OK, callback.toString(), reponse.getContentType());
                sendResponse(socketChannel, saveBox, reponse);
            }
            case "POST", "DELETE", "PUT" -> {
                int contentLength = 0;
                if (headers.containsKey("Content-Length")) {
                    contentLength = Integer.parseInt(headers.get("Content-Length").trim());
                }

                String body;
                if (parts.length > 1) {
                    body = parts[1];
                } else if (contentLength > 0) {
                    body = readRequestBody(socketChannel, contentLength);
                } else {
                    System.out.println("No body in request");
                    body = "";
                }

                request.setHeaders(headers);
                request.setMethod(method);
                request.setPath(path);
                request.setParams(params);

                if (!body.isEmpty()) {
                    try {
                        JSONObject jsonObject = new JSONObject(body);
                        request.setBody(jsonObject.toString());
                    } catch (Exception e) {
                        request.setBody(body);
                    }
                }

                var callback = pathMap.get(method).get(matchedPath).apply(request, reponse);
                Box saveBox = new Box(HttpStatus.OK, callback.toString(), reponse.getContentType());
                sendResponse(socketChannel, saveBox, reponse);
            }
            default -> {
                System.out.println("Unsupported HTTP method: " + method);
                Box saveBox = new Box(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed", "Content-Type: text/html");
                sendResponse(socketChannel, saveBox, reponse);
            }
        }

        closeConnection(key);
    }

    private void sendResponse(SocketChannel socketChannel, Box saveBox, Reponse reponse) throws IOException {
        initResponseHeaders(reponse);
        
        if (!reponse.getCookies().isEmpty()) {
            for (Map.Entry<String, String> cookie : reponse.getCookies().entrySet()) {
                saveBox.addHeader("Set-Cookie", cookie.getKey() + "=" + cookie.getValue());
            }
        }
        
        if (reponse.getHeaders() != null) {
            for (Map.Entry<String, String> header : reponse.getHeaders().entrySet()) {
                if (!header.getKey().equalsIgnoreCase("Cookie")) {
                    saveBox.addHeader(header.getKey(), header.getValue());
                }
            }
        }
        
        if (reponse.getContentType() != null) {
            saveBox.setContentType(reponse.getContentType());
        }
        
        getMethod.Get(socketChannel, saveBox);
    }

    private void initResponseHeaders(Reponse response) {
        if (response.getContentType() == null) {
            response.setContentType("text/html; charset=utf-8");
        }
    }

    private Map<String, String> matchPathWithPattern(String actualPath, String patternPath) {
        String[] actualParts = actualPath.split("/");
        String[] patternParts = patternPath.split("/");
        // so sanh 2 chuoi giong nhau thi la ra path
        if (actualParts.length != patternParts.length) {
            return null;
        }

        Map<String, String> params = new HashMap<>();

        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String actualPart = actualParts[i];
            // format lay params o ben trong {params}
            if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                String paramName = patternPart.substring(1, patternPart.length() - 1);
                params.put(paramName, actualPart);
            }
            // neu khong co trong ngoac {} thi so sanh voi nhau thoi? neu khac thi tra null thoi
            else if (!patternPart.equals(actualPart)) {
                return null;
            }
        }

        return params;
    }

    // check xem co dung la path chua param khong? roi tra ve params
    private List<String> detectParams(String path) {
        String[] parts = path.split("/");
        List<String> paramNames = new ArrayList<>();

        for (String part : parts) {
            if (part.isEmpty()) continue;
            // lay phan tu params ben trong {params}
            if (part.startsWith("{") && part.endsWith("}")) {
                paramNames.add(part.substring(1, part.length() - 1));
            }
        }

        return paramNames;
    }

    // add request method, add path
    public void request(String Method, String path, BiFunction<Request, Reponse, Object> function) {
        List<String> paramNames = detectParams(path);

        if (pathMap.get(Method) == null) {
            pathMap.put(Method, new HashMap<>());
        }

        pathMap.get(Method).put(path, function);

        if (!paramNames.isEmpty()) {
            pathParams.computeIfAbsent(Method, k -> new HashMap<>());
            pathParams.get(Method).put(path, paramNames);
        }
    }

    public void stop() throws Exception {
        running = false;
        selector.wakeup();
        selector.close();
        serverSocketChannel.close();
        executor.shutdown();
        System.out.println("HttpServer stopped.");
    }

    private Map<String, String> parseCookies(String cookieHeader) {
        //format theo dang cookies ? don gian vi cookies no nam tren header va co dinh dang la
        // ex: SessionId=123456; Path=/; HttpOnly
        Map<String, String> cookies = new HashMap<>();
        if (cookieHeader != null) {
            String[] cookiePairs = cookieHeader.split("; ");
            for (String pair : cookiePairs) {
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    cookies.put(keyValue[0], keyValue[1]);
                }
            }
        }
        return cookies;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }

    private void BuildHeader() {

    }

    private void handleSession(Request request, Reponse response) {
        Map<String, String> cookies = request.getCookies();
        String sessionId = cookies.get("SESSIONID");

        if (sessionId == null || !sessions.containsKey(sessionId)) {
            sessionId = generateSessionId();
            sessions.put(sessionId, new ConcurrentHashMap<>());
            response.addCookie("SESSIONID", sessionId);
        }

        request.setSession(sessions.get(sessionId));
    }
}
