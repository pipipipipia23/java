package com.sellso.Component.PhpExcutor;

import java.io.*;
import java.util.*;

public class PhpExecutorFull {

    private final String phpPath;

    public PhpExecutorFull(String phpPath) {
        this.phpPath = phpPath;
    }

    public static class PhpResponse {
        public int exitCode;
        public String output;
    }

    public PhpResponse ExecutePhp(
            String scriptPath,
            String method,
            Map<String, String> queryParams,
            Map<String, String> headers,
            Map<String, String> cookies,
            String body
    ) {
        List<String> command = new ArrayList<>();
        command.add(phpPath);
        command.add(scriptPath);

        ProcessBuilder builder = new ProcessBuilder(command);
        System.out.println(builder);
        Map<String, String> env = builder.environment();
        System.out.println(env);
        return null;
    }
}
