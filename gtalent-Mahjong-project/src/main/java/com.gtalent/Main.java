package com.gtalent;

import com.gtalent.view.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 初始化資料庫連線
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("無法載入MySQL驅動程式");
            e.printStackTrace();
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
