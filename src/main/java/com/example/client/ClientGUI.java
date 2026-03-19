package com.example.client;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * @author: Linda
 * @date: 2026/3/18 17:05
 * @description:
 */
public class ClientGUI extends JFrame {
    private ChatClient client;
    private JTextArea messageArea;
    private JTextField inputField;
    private JList<String> userList;
    private DefaultListModel<String> userListModel;
    private JButton sendButton;
    private JButton fileButton;
    private JButton connectButton;
    private JButton disconnectButton;
    private JTextField serverField;
    private JTextField portField;
    private JTextField usernameField;
    private String currentPrivateChat = null;

    public ClientGUI() {
        initComponents();
    }

    private void initComponents() {
        setTitle("即时聊天客户端");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 500);

        // 连接面板
        JPanel connectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectPanel.setBorder(BorderFactory.createTitledBorder("连接设置"));

        connectPanel.add(new JLabel("服务器:"));
        serverField = new JTextField("localhost", 10);
        connectPanel.add(serverField);

        connectPanel.add(new JLabel("端口:"));
        portField = new JTextField("8888", 5);
        connectPanel.add(portField);

        connectPanel.add(new JLabel("用户名:"));
        usernameField = new JTextField(10);
        connectPanel.add(usernameField);

        connectButton = new JButton("连接");
        disconnectButton = new JButton("断开");
        disconnectButton.setEnabled(false);

        connectButton.addActionListener(e -> connect());
        disconnectButton.addActionListener(e -> disconnect());

        connectPanel.add(connectButton);
        connectPanel.add(disconnectButton);

        // 消息区域
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        messageScroll.setBorder(BorderFactory.createTitledBorder("消息"));

        // 用户列表
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);
        userList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selectedUser = userList.getSelectedValue();
                    if (selectedUser != null && !selectedUser.equals(client.getUsername())) {
                        currentPrivateChat = selectedUser;
                        setTitle("私聊 - " + selectedUser);
                    }
                }
            }
        });
        JScrollPane userScroll = new JScrollPane(userList);
        userScroll.setBorder(BorderFactory.createTitledBorder("在线用户"));
        userScroll.setPreferredSize(new Dimension(150, 0));

        // 输入面板
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("输入"));

        inputField = new JTextField();
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("发送");
        sendButton.addActionListener(e -> sendMessage());

        fileButton = new JButton("发送文件");
        fileButton.addActionListener(e -> sendFile());

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(sendButton);
        buttonPanel.add(fileButton);

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);

        // 布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messageScroll, userScroll);
        splitPane.setResizeWeight(0.8);

        add(connectPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);

        // 菜单栏
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        JMenuItem clearItem = new JMenuItem("清空消息");
        clearItem.addActionListener(e -> messageArea.setText(""));
        fileMenu.add(clearItem);

        JMenu chatMenu = new JMenu("聊天");
        JMenuItem groupChatItem = new JMenuItem("群聊");
        groupChatItem.addActionListener(e -> {
            currentPrivateChat = null;
            setTitle("即时聊天客户端 - 群聊");
        });
        chatMenu.add(groupChatItem);

        menuBar.add(fileMenu);
        menuBar.add(chatMenu);
        setJMenuBar(menuBar);

        setLocationRelativeTo(null);
    }

    private void connect() {
        String server = serverField.getText().trim();
        int port = Integer.parseInt(portField.getText().trim());
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入用户名");
            return;
        }

        client = new ChatClient(server, port, this);
        if (client.connect(username)) {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
            serverField.setEnabled(false);
            portField.setEnabled(false);
            usernameField.setEnabled(false);
            displaySystemMessage("已连接到服务器");
        }
    }

    private void disconnect() {
        if (client != null) {
            client.disconnect();
            client = null;
        }

        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        serverField.setEnabled(true);
        portField.setEnabled(true);
        usernameField.setEnabled(true);
        userListModel.clear();
        displaySystemMessage("已断开连接");
    }

    private void sendMessage() {
        if (client == null || !client.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器");
            return;
        }

        String content = inputField.getText().trim();
        if (content.isEmpty()) return;

        if (currentPrivateChat != null) {
            // 发送私聊消息
            client.sendPrivateMessage(currentPrivateChat, content);
            displayPrivateMessage("我 -> " + currentPrivateChat, content);
        } else {
            // 发送群聊消息
            client.sendTextMessage(content);
            displayMessage("我", content);
        }

        inputField.setText("");
    }

    private void sendFile() {
        if (client == null || !client.isConnected()) {
            JOptionPane.showMessageDialog(this, "请先连接到服务器");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            client.sendFile(file.getAbsolutePath(), currentPrivateChat);
        }
    }

    // 显示消息
    public void displayMessage(String sender, String content) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(String.format("[%s] %s: %s\n",
                    new java.util.Date().toString(), sender, content));
        });
    }

    public void displayPrivateMessage(String sender, String content) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(String.format("[私聊][%s] %s: %s\n",
                    new java.util.Date().toString(), sender, content));
        });
    }

    public void displaySystemMessage(String content) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(String.format("【系统消息】%s: %s\n",
                    new java.util.Date().toString(), content));
        });
    }

    public void updateUserList(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userListModel.clear();
            for (String user : users) {
                if (!user.isEmpty()) {
                    userListModel.addElement(user);
                }
            }
        });
    }

    public String chooseSaveLocation(String fileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(fileName));
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }
}
