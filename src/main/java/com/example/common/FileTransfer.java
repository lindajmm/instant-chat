package com.example.common;

import java.io.*;
import java.nio.file.*;
/**
 * @author: Linda
 * @date: 2026/3/18 17:01
 * @description:
 */
public class FileTransfer {

    public static byte[] fileToBytes(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    public static void bytesToFile(byte[] data, String savePath) throws IOException {
        Path path = Paths.get(savePath);
        Files.write(path, data);
    }

    public static String getFileName(String filePath) {
        return Paths.get(filePath).getFileName().toString();
    }

    public static long getFileSize(String filePath) throws IOException {
        return Files.size(Paths.get(filePath));
    }

    public static boolean isValidPath(String filePath) {
        return filePath != null && !filePath.trim().isEmpty();
    }
}
