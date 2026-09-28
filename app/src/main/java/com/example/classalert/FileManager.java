package com.example.classalert;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileManager {

    private static final String CACHE_DIR_NAME = "ClassAlert";
    private static final String FILE_NAME = "ClassAlert.docx";

    /**
     * Get the cache directory for storing downloaded files
     */
    public static File getCacheDirectory(Context context) {
        File cacheDir = new File(context.getFilesDir(), CACHE_DIR_NAME);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    /**
     * Get the full path to the cached docx file
     */
    public static File getCachedFile(Context context) {
        return new File(getCacheDirectory(context), FILE_NAME);
    }

    /**
     * Check if the file is already cached
     */
    public static boolean isCached(Context context) {
        return getCachedFile(context).exists();
    }

    /**
     * Save input stream to internal storage
     */
    public static void saveToInternalStorage(Context context, InputStream inputStream) throws IOException {
        File cacheDir = getCacheDirectory(context);
        File file = new File(cacheDir, FILE_NAME);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            fos.flush();
        }
    }

    /**
     * Read from cached file
     */
    public static InputStream readFromCache(Context context) throws IOException {
        File file = getCachedFile(context);
        return new FileInputStream(file);
    }

    /**
     * Delete cached file
     */
    public static boolean deleteCachedFile(Context context) {
        File file = getCachedFile(context);
        return file.exists() && file.delete();
    }

    /**
     * Get file size in MB
     */
    public static double getFileSizeInMB(Context context) {
        File file = getCachedFile(context);
        if (file.exists()) {
            return file.length() / (1024.0 * 1024.0);
        }
        return 0;
    }

    /**
     * Get file information
     */
    public static String getFileInfo(Context context) {
        File file = getCachedFile(context);
        if (file.exists()) {
            double sizeInMB = getFileSizeInMB(context);
            long lastModified = file.lastModified();
            return String.format("File: %s\nSize: %.2f MB\nPath: %s\nLast modified: %tc",
                    FILE_NAME, sizeInMB, file.getAbsolutePath(), lastModified);
        }
        return "No cached file found";
    }
}