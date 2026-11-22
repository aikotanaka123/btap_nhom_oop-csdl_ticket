package com.nhom.datve.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Doc file cau hinh db.properties trong src/main/resources.
 * Neu khong co file -> chay che do mock (du lieu gia) de code giao dien khong can SQL Server.
 */
public final class AppConfig {
    private static final Properties PROPS = new Properties();
    private static final boolean CO_FILE;

    static {
        boolean coFile = false;
        try (InputStream in = AppConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                PROPS.load(in);
                coFile = true;
            }
        } catch (IOException e) {
            System.err.println("Khong doc duoc db.properties: " + e.getMessage());
        }
        CO_FILE = coFile;
    }

    private AppConfig() { }

    public static String get(String key) {
        return PROPS.getProperty(key);
    }

    /** true neu chay voi du lieu gia. Mac dinh la mock khi chua co db.properties. */
    public static boolean isMockMode() {
        return !CO_FILE || !"jdbc".equalsIgnoreCase(PROPS.getProperty("app.mode", "mock").trim());
    }

    public static void setMockMode(boolean mock) {
        PROPS.setProperty("app.mode", mock ? "mock" : "jdbc");
    }
}
