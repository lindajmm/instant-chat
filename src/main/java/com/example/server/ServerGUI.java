package com.example.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * @author: Linda
 * @date: 2026/3/18 17:03
 * @description:
 */
public class ServerGUI extends JFrame {
    private ChatServer server;
    private JTextArea logArea;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JButton startButton;
    private JButton stopButton;
    private JLabel statusLabel;

    public ServerGUI(ChatServer server) {
        this.server = server;
        initComponents();
    }

    private void initComponents() {
        setTitle("聊天服务器控制台");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(600, 400);

        // 日志区域
        logArea = new JTextArea();
        logArea.setEditable(false);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder("服务器日志"));

        // 用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("在线用户"));
        userScroll.setPreferredSize(new Dimension(150, 0));

        // 控制面板
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("控制"));

        startButton = new JButton("启动服务器");
        stopButton = new JButton("停止服务器");
        stopButton.setEnabled(false);

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        controlPanel.add(startButton);
        controlPanel.add(stopButton);

        // 状态栏
        statusLabel = new JLabel("服务器状态: 已停止");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        // 添加组件
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, logScroll, userScroll);
        splitPane.setResizeWeight(0.7);

        add(splitPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.NORTH);
        add(statusLabel, BorderLayout.SOUTH);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startServer() {
        startButton.setEnabled(false);
        stopButton.setEnabled(true);
        statusLabel.setText("服务器状态: 运行中");
        new Thread(() -> server.start()).start();
    }

    private void stopServer() {
        startButton.setEnabled(true);
        stopButton.setEnabled(false);
        statusLabel.setText("服务器状态: 已停止");
        server.stop();
    }

    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append("[" + new java.util.Date() + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void updateUserList(List<String> users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                userListModel.addElement(user);
            }
        });
    }
}
