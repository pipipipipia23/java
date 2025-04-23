package com.sellso.Component.Method;

import com.sellso.Component.Concac.PostContent;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PostMethod {
    private PostContent PostContent = new PostContent();

    public void Post(SocketChannel socketChannel, Object box) throws IOException {
        StringBuilder content = (StringBuilder) PostContent.ContentFrontEnd.apply(box);
        ByteBuffer responseBuffer = ByteBuffer.wrap(content.toString().getBytes());

        while (responseBuffer.hasRemaining()) {
            socketChannel.write(responseBuffer);
        }
    }
}

