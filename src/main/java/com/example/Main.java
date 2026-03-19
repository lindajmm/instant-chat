package com.example;

import com.example.server.ChatServer;
import com.example.client.ClientGUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        /*SwingUtilities.invokeLater(() -> {
            String[] options = {"启动服务器", "启动客户端"};
            int choice = JOptionPane.showOptionDialog(null,
                    "选择启动模式",
                    "即时聊天系统",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (choice == 0) {
                // 启动服务器
                new ChatServer();
            } else if (choice == 1) {
                // 启动客户端
                new ClientGUI().setVisible(true);
            } else {
                System.exit(0);
            }
        });*/

        if (args.length > 0 && "server".equals(args[0])) {
            // 启动服务器
            new ChatServer().start();
        } else {
            // 启动客户端
            new ClientGUI().setVisible(true);
        }
//    }
    }

//    public static void main(String[] args) {
        // args[0] 就是 "server"

}