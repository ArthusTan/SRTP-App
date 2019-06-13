package com.test.myapplication.objects;

import java.io.File;
import java.io.FileOutputStream;

public class Position {
    public static double LONGITUDE = 0;//经度
    public static double LATITUDE = 0;//纬度
    public static String strIMG = null;
    public static String strURL = null;
    public static String strLON = null;
    public static String strLAT = null;

    private static void init() {
        strLON = String.valueOf(LONGITUDE);
        strLAT = String.valueOf(LATITUDE);
    }

    public static void write() {
        init();
        String str = strLAT + "," + strLON + "," + strIMG + "," + strURL + "\n";
        File file = new File("/sdcard/flower-comf/mapInfo.txt");
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
