/*
 * JVM agent to track memory allocations
 *
 * Copyright (C) 2019 Jesper Pedersen <jesper.pedersen@comcast.net>
 */
package com.yibo.common.monitor.endpoint;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Helper class for text files
 *
 * @author yibo
 * @date 2021-05-06
 */
public class TextFile {
    /**
     * Open a file
     *
     * @param p The path of the file
     * @return The file
     */
    public static BufferedWriter openFile(Path p) throws IOException {
        return Files.newBufferedWriter(p,
                StandardOpenOption.CREATE,
                StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    /**
     * Append data to a file
     *
     * @param bw The file
     * @param s  The string
     */
    public static void append(BufferedWriter bw, String s) throws IOException {
        bw.write(s, 0, s.length());
        bw.newLine();
    }

    /**
     * Close a file
     *
     * @param bw The file
     */
    public static void closeFile(BufferedWriter bw) throws IOException {
        bw.flush();
        bw.close();
    }
}
