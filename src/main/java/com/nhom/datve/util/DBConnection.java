package com.nhom.datve.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lop ket noi CSDL theo mau Singleton: ca chuong trinh chi co DUY NHAT 1 doi tuong DBConnection,
 * giu thong tin ket noi (url, user, password) doc 1 lan tu db.properties.
 *
 * Moi lan goi getConnection() tra ve 1 Connection moi; noi goi phai dong bang try-with-resources:
 * <pre>
 * try (Connection conn = DBConnection.getInstance().getConnection();
 *      PreparedStatement ps = conn.prepareStatement("SELECT ...")) { ... }
 * </pre>
 * Khong dung chung 1 Connection cho ca app vi 2 thao tac chay cung luc (vd 2 cua so) co the
 * lam roi transaction cua nhau.
 */
public final class DBConnection {
    private static volatile DBConnection instance;

    private final String url;
    private final String user;
    private final String password;

    /** Constructor private: ben ngoai khong the new DBConnection(). */
    private DBConnection() {
        this.url = AppConfig.get("db.url");
        this.user = AppConfig.get("db.user");
        this.password = AppConfig.get("db.password");
        if (url == null) {
            throw new IllegalStateException(
                "Chua cau hinh db.url. Hay copy src/main/resources/db.properties.example thanh db.properties.");
        }
    }

    /** Lay doi tuong duy nhat (double-checked locking, an toan khi nhieu luong). */
    public static DBConnection getInstance() {
        if (instance == null) {
            synchronized (DBConnection.class) {
                if (instance == null) {
                    instance = new DBConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Thu ket noi, dung cho man hinh khoi dong hoac kiem tra cau hinh. */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn.isValid(3);
        } catch (SQLException e) {
            System.err.println("Ket noi that bai: " + e.getMessage());
            return false;
        }
    }
}
