package com.roche.rss.lannister.utils;

import java.util.Random;

public class FileUtils {

    /**
     * get Base name from file name
     * @param fileName
     * @return
     */
    public static String getBaseName(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }


    /**
     * Check if file or directory exists
     * @param dirPath
     * @return
     */
    public static Boolean isDirectory(String dirPath){
        java.io.File f = new java.io.File(dirPath);
        return f.exists() || f.isDirectory();
    }

    public static Integer getRandomInt(){
        Random rand = new Random();
        return rand.nextInt();
    }
}
