package com.sellso.Component.Method;

import com.sellso.Component.Concac.Content;
import com.sellso.Component.Data.Box;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class GetMethod {
    private final Content GetContent = new Content();

    public void Get(SocketChannel socketChannel, Box box) throws IOException {
        StringBuilder content = (StringBuilder) GetContent.ContentFrontEnd.apply(box);
        ByteBuffer responseBuffer = ByteBuffer.wrap(content.toString().getBytes());

        while (responseBuffer.hasRemaining()) {
            socketChannel.write(responseBuffer);
        }
    }
}
