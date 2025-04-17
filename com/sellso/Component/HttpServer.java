package com.sellso.Component;

import com.sellso.Component.Concac.Content;
import com.sellso.Component.Data.Box;
import com.sellso.Component.Enum.HttpStatus;
import com.sellso.Component.Method.GetMethod;
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

public class HttpServer {
    ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;
    private boolean running = true;
    private static final int DEFAULT_PORT = 8080;
    private final Set<SocketChannel> blockingKeys = ConcurrentHashMap.newKeySet();
    private final Content GetContent = new Content();
    private final GetMethod getMethod = new GetMethod();

    private final Map<String, Map<String, BiFunction<Request, Reponse, Object>>> pathMap = new ConcurrentHashMap<>();
    private final Map<String, Map<String, List<String>>> pathParams = new ConcurrentHashMap<>();

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
                        
                        key.interestOps(key.interestOps() & ~SelectionKey.OP_READ);
                        
                        final SelectionKey readKey = key;
                        executor.submit(() -> {
                            try {
                                handleReadRequest(readKey);
                            } catch (Exception e) {
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
    
    private void acceptConnection(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel socketChannel = server.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

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

    private List<String> formatString(String path) {
        List<String> allPath = new ArrayList<>();

        if (!path.equals("/")) {
            allPath.add(path);
        }

        String currentPath = path;
        while (!currentPath.equals("/")) {

            // lay dau gach cuoi cung nhe
            int lastSlashIndex = currentPath.lastIndexOf("/", currentPath.length() - 2);
            if (lastSlashIndex == -1) {
                break;
            }
            // Tu dau gach cuoi cung lay chuoi tu dau den do
            currentPath = currentPath.substring(0, lastSlashIndex + 1);
            if (currentPath.length() > 1) {
                // xoa dau gach
                allPath.add(currentPath.substring(0, currentPath.length() - 1));
            }
        }

        allPath.add("/");

        System.out.println(allPath);
        return allPath;
    }

    private void handleReadRequest(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        StringBuilder requestBuilder = new StringBuilder();
        ByteBuffer buffer = ByteBuffer.allocate(4096);

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

        // format get method, path
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
                Box saveBox = new Box(HttpStatus.MAX_LENGTH,"Max Length For Get");
                getMethod.Get(socketChannel,saveBox);
            }
        }

        // format get body, header

        String[] parts = requestBuilder.toString().split("(\r\n\r\n|\n\n)");

        Map<String, String> headers = new HashMap<>();

        // for header

        String headerdata[] = parts[0].split("\n");

        for (String header : headerdata) {
            String[] headerParts = header.split(": ", 2);
            if (headerParts.length == 2) {
                headers.put(headerParts[0], headerParts[1]);
            }
        }
        if (pathMap.get(method) == null && pathParams.get(method) == null) {
            System.out.println("here?");
            Box saveBox = new Box(HttpStatus.NO_HAVE_METHOD,"Method Not Allowed");
            getMethod.Get(socketChannel,saveBox);
        } else {
            List<String> pathFormat = formatString(path);
            String realPath = "";
            boolean isParams = false;
            int lenghtParams = 0;

            for (int i = 0; pathFormat.size() > i; i++) {
                if (pathMap.get(method).get(pathFormat.get(i)) != null) {
                    realPath = pathFormat.get(i);
                    break;
                }
                isParams = true;
                lenghtParams = i;
            }
            if (pathMap.get(method).get(realPath) == null && pathParams.get(method).get(realPath) == null) {
                Box saveBox = new Box(HttpStatus.NOT_FOUND,"NOT FOUND");
                getMethod.Get(socketChannel,saveBox);
                closeConnection(key);
                return;
            }
            Reponse reponse = new Reponse();
            Request request = new Request();

            switch (method) {
                case "GET" -> {
                    // request, reponse
                    reponse.setContentType(headers.get("Content-Type"));
                    reponse.setHeaders(headers);
                    if (parts.length > 1) {
                        request.setBody(parts[1]);
                    }
                    request.setHeaders(headers);
                    request.setMethod(method);
                    request.setPath(path);
                    if (isParams) {
                        pathFormat.getFirst();
                    }

                    var callback = pathMap.get(method).get(path).apply(request, reponse);
                    Box saveBox = new Box(HttpStatus.OK, callback.toString());
                    getMethod.Get(socketChannel, saveBox);
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
//                    body
                    JSONObject jsonObject = new JSONObject(body);
                    System.out.println(jsonObject);
                }
            }
        }
        closeConnection(key);
    }

    private void closeConnection(SelectionKey key) {
        try {
            key.channel().close();
            key.cancel();
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    private List<String> detectParams(String path) {
        String[] parts = path.split("\\{");
        List<String> paramNames = new ArrayList<>();
        paramNames.add(parts[0].replaceAll("/" , ""));
        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String cleanParam = parts[i].replaceAll("\\}.*", "");
                paramNames.add(cleanParam);
            }
        }
        return paramNames;
    }

    public void request(String Method,String path, BiFunction<Request, Reponse, Object> function) {
        List<String> listparam = detectParams(path);
        if (pathMap.get(Method) == null) {
            pathMap.put(Method, new HashMap<>());
            if (listparam.size() > 1) {
                pathParams.computeIfAbsent(Method, k -> new HashMap<>());
                listparam.removeFirst();
                pathParams.get(Method).put(path, listparam);
            }
        }
        pathMap.get(Method).put(path, function);
    }

    public void stop() throws Exception {
        running = false;
        selector.wakeup();
        selector.close();
        serverSocketChannel.close();
        executor.shutdown();
        System.out.println("HttpServer stopped.");
    }
}
