package com.nhom.datve.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Mau, font, nut va dinh dang dung chung cho MOI man hinh. Ca nhom dung cac ham o day thay vi
 * tu dat mau/font, de giao dien thong nhat.
 */
public final class Theme {
    public static final Color MAU_CHINH = new Color(0xB3261E);      // do rap phim: nut chinh, tieu de
    public static final Color MAU_CHINH_DAM = new Color(0x8C1D18);
    public static final Color MAU_NEN = new Color(0xF7F7F8);
    public static final Color MAU_THE = Color.WHITE;                 // nen o, the
    public static final Color MAU_CHU = new Color(0x1F2328);
    public static final Color MAU_CHU_PHU = new Color(0x6A7280);
    public static final Color MAU_VIEN = new Color(0xD9DCE1);
    public static final Color MAU_THANH_CONG = new Color(0x1E7F3E);
    public static final Color MAU_CANH_BAO = new Color(0xC0601A);

    // Mau trang thai ghe (Trung dung cho SoDoGhePanel)
    public static final Color GHE_TRONG = new Color(0xE9EEF5);
    public static final Color GHE_DANG_CHON = new Color(0x2E7D32);
    public static final Color GHE_DA_DAT = new Color(0x9AA0A6);
    public static final Color GHE_VIP = new Color(0xF3C969);
    public static final Color GHE_DOI = new Color(0xE7A6C9);

    private static final String HO_FONT = "Segoe UI";
    public static final Font FONT = new Font(HO_FONT, Font.PLAIN, 14);
    public static final Font FONT_DAM = FONT.deriveFont(Font.BOLD);
    public static final Font FONT_TIEU_DE = new Font(HO_FONT, Font.BOLD, 22);
    public static final Font FONT_TIEU_DE_NHO = new Font(HO_FONT, Font.BOLD, 16);
    public static final Font FONT_CODE = new Font(Font.MONOSPACED, Font.PLAIN, 13);

    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    private Theme() { }

    /** Goi 1 lan khi khoi dong (trong Main), truoc khi tao cua so nao. */
    public static void apDung() {
        try {
            // FlatLaf co trong pom.xml; dung ten lop de van chay duoc neu thieu thu vien
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatLightLaf");
            UIManager.put("Component.arc", 8);
            UIManager.put("Button.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("Component.focusColor", MAU_CHINH);
            UIManager.put("Table.selectionBackground", new Color(0xF6D5D2));
            UIManager.put("Table.selectionForeground", MAU_CHU);
        } catch (Exception khongCoFlatLaf) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // giu giao dien mac dinh cua Java
            }
        }
        UIManager.put("defaultFont", FONT);
        for (String k : new String[] {"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Table.font", "TableHeader.font", "Menu.font", "MenuItem.font",
                "CheckBox.font", "RadioButton.font", "Spinner.font", "TitledBorder.font", "TextArea.font",
                "OptionPane.messageFont", "OptionPane.buttonFont", "ToggleButton.font", "List.font"}) {
            UIManager.put(k, FONT);
        }
    }

    /** Nut hanh dong chinh (Tim, Thanh toan, Luu...). */
    public static JButton nutChinh(String text) {
        JButton b = new JButton(text);
        b.setBackground(MAU_CHINH);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_DAM);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.putClientProperty("JButton.buttonType", "default");
        return b;
    }

    /** Nut phu (Huy, Quay lai, Xoa loc...). */
    public static JButton nutPhu(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JLabel tieuDe(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TIEU_DE);
        l.setForeground(MAU_CHU);
        return l;
    }

    public static JLabel chuPhu(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MAU_CHU_PHU);
        return l;
    }

    /** Le trong deu quanh noi dung man hinh. */
    public static Border le(int px) {
        return BorderFactory.createEmptyBorder(px, px, px, px);
    }

    public static Border khung(String tieuDe) {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(MAU_VIEN), tieuDe),
                le(8));
    }

    /** Dinh dang chung cho moi JTable. */
    public static void dinhDangBang(JTable t) {
        t.setRowHeight(30);
        t.setFillsViewportHeight(true);
        t.setShowVerticalLines(false);
        t.getTableHeader().setFont(FONT_DAM);
        t.getTableHeader().setReorderingAllowed(false);
        t.setAutoCreateRowSorter(true);
    }

    /** 75000 -> "75.000 đ" */
    public static String tien(BigDecimal soTien) {
        if (soTien == null) {
            return "";
        }
        NumberFormat nf = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
        return nf.format(soTien) + " đ";
    }

    public static String ngayGio(LocalDateTime t) {
        return t == null ? "" : NGAY_GIO.format(t);
    }

    public static String gio(LocalDateTime t) {
        return t == null ? "" : GIO.format(t);
    }
}
